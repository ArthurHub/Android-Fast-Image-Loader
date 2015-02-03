// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.fastimageloader;

import android.os.Handler;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO:a add doc
 */
final class Downloader {

    //region: Fields and Consts

    /**
     * queue of pre-fetch requests to download if not already
     */
    private final ConcurrentLinkedQueue<ImageRequest> mPrefetchRequests = new ConcurrentLinkedQueue<>();

    /**
     * The HTTP client used to execute download image requests
     */
    private final OkHttpClient mClient;

    /**
     * Used to post execution to main thread.
     */
    public final Handler mHandler;

    /**
     * Used to load images from the disk.
     */
    private final DiskHandler mDiskHandler;

    /**
     * The callback to execute on async requests to the downloader
     */
    private final Callback mCallback;

    /**
     * Threads service for all read operations.
     */
    private final ThreadPoolExecutor mExecutorService;

    /**
     * the buffers used to download image
     */
    private final byte[][] mBuffers = new byte[4][];

    /**
     * Is prefetch is currently running on the list
     */
    private boolean mPrefetchRunning;
    //endregion

    /**
     * @param client the OkHttp client to use to download the images.
     * @param handler Used to post execution to main thread.
     * @param diskHandler Handler for loading image bitmap object from file on disk.
     * @param callback The callback to execute on async requests to the downloader
     */
    public Downloader(OkHttpClient client, Handler handler, DiskHandler diskHandler, Callback callback) {
        Utils.notNull(client, "client");
        Utils.notNull(handler, "handler");
        Utils.notNull(diskHandler, "imageLoader");
        Utils.notNull(callback, "callback");

        mClient = client;
        mHandler = handler;
        mDiskHandler = diskHandler;
        mCallback = callback;

        mExecutorService = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), Util.threadFactory("ImageDownloader", true));
        mExecutorService.allowCoreThreadTimeOut(true);
    }

    /**
     * @param imageRequest
     */
    public void prefetchAsync(ImageRequest imageRequest) {
        mPrefetchRequests.add(imageRequest);
        if (!mPrefetchRunning) {
            mPrefetchRunning = true;
            Logger.debug("Init prefetch executor run... [{}]", mPrefetchRequests.size());
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    prefetch();
                }
            });
        }
    }

    /**
     * Download
     */
    public void downloadAsync(final ImageRequest imageRequest) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                final boolean canceled = download(imageRequest);
                final boolean downloaded = imageRequest.getFileSize() > 0;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.loadImageDownloaderCallback(imageRequest, downloaded, canceled);
                    }
                });
            }
        });
    }

    //region: Private methods

    /**
     * Prefetch the image at the given URI.<br/>
     * Check if the image was already downloaded, if not execute download.
     */
    private void prefetch() {
        Logger.debug("Start prefetch executor run... [{}]", mPrefetchRequests.size());
        while (!mPrefetchRequests.isEmpty()) {
            ImageRequest request = mPrefetchRequests.remove();
            if (!request.getFile().exists() && request.isPrefetch()) {
                download(request);
            }
        }
        Logger.debug("Prefetch executor run complete [{}]", mPrefetchRequests.size());
        mPrefetchRunning = false;

    }

    /**
     * Download
     *
     * @return 0: downloaded, canceled - false, 10: downloaded
     */
    private boolean download(final ImageRequest imageRequest) {
        int responseCode = 0;
        Exception error = null;
        boolean canceled = false;
        boolean downloaded = false;
        long start = System.currentTimeMillis();
        try {
            canceled = !imageRequest.isValid();
            if (!canceled) {

                // start image download request
                Request request = new Request.Builder().url(imageRequest.getThumborUrl()).build();
                Response response = mClient.newCall(request).execute();

                // check handshake
                responseCode = response.code();
                if (responseCode < 300) {
                    canceled = !imageRequest.isValid();
                    if (!canceled) {

                        // download data
                        downloaded = download(imageRequest, response);
                        canceled = !imageRequest.isValid();
                    }
                } else {
                    error = new ConnectException(response.code() + ": " + response.message());
                    Logger.error("Failed to download image... [{}] [{}] [{}]", response.code(), response.message(), imageRequest);
                }
            }
        } catch (Exception e) {
            error = e;
            Logger.error("Failed to download image [{}]", e, imageRequest);
        }

        if (downloaded || error != null) {
            Logger.operation(imageRequest.getThumborUrl(), imageRequest.getSpec().getKey(), responseCode, System.currentTimeMillis() - start, imageRequest.getFileSize(), error);
        }

        // if downloaded and request is still valid - load the image object
        if (downloaded) {
            canceled = !imageRequest.isValid();
            if (!canceled) {
                canceled = false;
                mDiskHandler.loadImageObject(imageRequest);
            }
        }

        return canceled;
    }

    /**
     * Download image data from the given web response.<br/>
     * Download to temp file so if error occurred it won't result in corrupted cached file and handle
     * smart cancelling, if request is no longer valid but more than 50% has been downloaded, finish it but
     * don't load the image object.
     *
     * @return true - download successful, false - otherwise.
     */
    private boolean download(ImageRequest imageRequest, Response response) throws IOException {

        byte[] buffer = null;
        InputStream in = null;
        OutputStream out = null;
        File tmpFile = new File(imageRequest.getFile().getAbsolutePath() + "_tmp");
        try {
            in = response.body().byteStream();
            out = new FileOutputStream(tmpFile);

            int len = 0;
            int size = 0;
            buffer = getBuffer();

            // don't cancel download if passed 50%
            long contentLength = Utils.parseLong(response.header("content-length"), -1);
            while ((contentLength < 0 || contentLength * .5f < size || imageRequest.isValid()) && (len = in.read(buffer)) != -1) {
                size += len;
                out.write(buffer, 0, len);
            }

            // if we finished download
            if (len == -1) {
                if (tmpFile.renameTo(imageRequest.getFile())) {
                    imageRequest.setFileSize(size);
                    return true;
                } else {
                    Logger.warn("Failed to rename temp download file to target file");
                }
            }
        } finally {
            Utils.closeSafe(out);
            Utils.closeSafe(in);
            Utils.deleteSafe(tmpFile);
            returnBuffer(buffer);
        }
        return false;
    }

    /**
     * Get buffer to be used for image download, use recycled if possible.
     */
    private byte[] getBuffer() {
        byte[] buffer = null;
        synchronized (mBuffers) {
            for (int i = 0; i < mBuffers.length; i++) {
                if (mBuffers[i] != null) {
                    buffer = mBuffers[i];
                    mBuffers[i] = null;
                    break;
                }
            }
        }
        if (buffer == null) {
            buffer = new byte[2048];
        }
        return buffer;
    }

    /**
     * Return buffer to be used for image download to recycled collection.
     */
    private void returnBuffer(byte[] buffer) {
        if (buffer != null) {
            synchronized (mBuffers) {
                for (int i = 0; i < mBuffers.length; i++) {
                    if (mBuffers[i] == null) {
                        mBuffers[i] = buffer;
                        break;
                    }
                }
            }
        }
    }
    //endregion

    //region: Inner class: Callback

    /**
     * Callback for getting cached image.
     */
    static interface Callback {

        /**
         * Callback for getting cached image, if not cached will have null.
         */
        public void loadImageDownloaderCallback(ImageRequest imageRequest, boolean downloaded, boolean canceled);
    }
    //endregion
}