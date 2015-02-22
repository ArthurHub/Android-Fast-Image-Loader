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

package com.theartofdev.fastimageloader.impl;

import com.theartofdev.fastimageloader.HttpClient;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO:a add doc
 */
public final class DownloaderImpl implements com.theartofdev.fastimageloader.Downloader {

    //region: Fields and Consts

    /**
     * The HTTP client used to execute download image requests
     */
    private final HttpClient mClient;

    /**
     * Threads service for download operations.
     */
    private final ThreadPoolExecutor mExecutor;

    /**
     * Threads service for pre-fetch download operations.
     */
    private final ThreadPoolExecutor mPrefetchExecutor;

    /**
     * the buffers used to download image
     */
    private final byte[][] mBuffers;

    private int mExecutorThreads = 2;
    //endregion

    /**
     * @param client the OkHttp client to use to download the images.
     */
    public DownloaderImpl(HttpClient client) {
        FILUtils.notNull(client, "client");

        mClient = client;

        mBuffers = new byte[mExecutorThreads + 1][];

        mExecutor = new ThreadPoolExecutor(mExecutorThreads, mExecutorThreads, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), FILUtils.threadFactory("ImageDownloader", true));
        mExecutor.allowCoreThreadTimeOut(true);

        mPrefetchExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), FILUtils.threadFactory("ImagePrefetchDownloader", true));
        mPrefetchExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void downloadAsync(final ImageRequest imageRequest, final boolean prefetch, final Callback callback) {
        Executor executor = prefetch ? mPrefetchExecutor : mExecutor;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                handleExecutorDownload(imageRequest, prefetch, callback);

            }
        });
    }

    //region: Private methods

    /**
     * Handle starting download execution on executor worker thread.<br>
     * Request can be executed twice, on prefetch and regular executor so it must safeguard
     * from handling the same request twice.<br>
     * Raise the given callback when download return with flags if the request was downloaded/canceled in
     * any combination possible.
     */
    protected void handleExecutorDownload(ImageRequest imageRequest, boolean prefetch, Callback callback) {
        // mark start download, the first to do this will win (sync between prefetch and load)
        if ((prefetch || !imageRequest.isPrefetch()) && imageRequest.startDownload()) {
            FILLogger.debug("Start image request download... [{}]", imageRequest);
            boolean canceled = downloadByClient(imageRequest);
            boolean downloaded = imageRequest.getFileSize() > 0;
            callback.loadImageDownloaderCallback(imageRequest, downloaded, canceled);
        } else {
            FILLogger.debug("Image request download already handled [{}]", imageRequest);
        }
    }

    /**
     * Execute client request to download the file, if valid response is returned use
     * {@link #downloadToFile(ImageRequest, com.theartofdev.fastimageloader.HttpClient.HttpResponse)}
     * to download the image from response body.
     *
     * @return true - download was canceled before finishing, false - otherwise.
     */
    protected boolean downloadByClient(ImageRequest imageRequest) {
        int responseCode = 0;
        Exception error = null;
        boolean canceled = false;
        long start = System.currentTimeMillis();
        try {
            canceled = !imageRequest.isValid();
            if (!canceled) {

                // start image download request
                HttpClient.HttpResponse httpResponse = mClient.execute(imageRequest.getEnhancedUri());

                // check handshake
                responseCode = httpResponse.getCode();
                if (responseCode < 300) {
                    canceled = !imageRequest.isValid();
                    if (!canceled) {
                        // download data
                        canceled = downloadToFile(imageRequest, httpResponse);
                    }
                } else {
                    error = new ConnectException(httpResponse.getCode() + ": " + httpResponse.getErrorMessage());
                    FILLogger.error("Failed to download image... [{}] [{}] [{}]", httpResponse.getCode(), httpResponse.getErrorMessage(), imageRequest);
                }
            }
        } catch (Exception e) {
            error = e;
            FILLogger.error("Failed to download image [{}]", e, imageRequest);
        }

        // if downloaded or error occurred - report operation, don't report cancelled
        if (imageRequest.getFileSize() > 0 || error != null) {
            FILLogger.operation(imageRequest.getEnhancedUri(), imageRequest.getSpec().getKey(), responseCode, System.currentTimeMillis() - start, imageRequest.getFileSize(), error);
        }

        return canceled;
    }

    /**
     * Download image data from the given web response.<br>
     * Download to temp file so if error occurred it won't result in corrupted cached file and handle
     * smart cancelling, if request is no longer valid but more than 50% has been downloaded, finish it but
     * don't load the image object.
     *
     * @return true - download was canceled before finishing, false - otherwise.
     */
    protected boolean downloadToFile(ImageRequest imageRequest, HttpClient.HttpResponse response) throws IOException {
        byte[] buffer = null;
        InputStream in = null;
        OutputStream out = null;
        boolean canceled = false;
        File tmpFile = new File(imageRequest.getFile().getAbsolutePath() + "_tmp");
        try {
            in = response.getBodyStream();
            out = new FileOutputStream(tmpFile);

            int len = 0;
            int size = 0;
            buffer = getBuffer();

            // don't cancel download if passed 50%
            long contentLength = response.getContentLength();
            while ((contentLength < 0 || contentLength * .5f < size || imageRequest.isValid()) && (len = in.read(buffer)) != -1) {
                size += len;
                out.write(buffer, 0, len);
                imageRequest.updateDownloading(size, contentLength);
            }

            // if we finished download
            if (len == -1) {
                if (tmpFile.renameTo(imageRequest.getFile())) {
                    imageRequest.setFileSize(size);
                } else {
                    FILLogger.warn("Failed to rename temp download file to target file");
                }
            } else {
                canceled = true;
            }
        } finally {
            FILUtils.closeSafe(out);
            FILUtils.closeSafe(in);
            FILUtils.deleteSafe(tmpFile);
            returnBuffer(buffer);
        }
        return canceled;
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
}