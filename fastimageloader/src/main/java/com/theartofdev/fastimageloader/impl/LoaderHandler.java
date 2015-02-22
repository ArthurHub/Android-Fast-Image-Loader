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

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;

import com.theartofdev.fastimageloader.Decoder;
import com.theartofdev.fastimageloader.DiskCache;
import com.theartofdev.fastimageloader.Downloader;
import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.MemoryPool;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.Target;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for image loading using memory/disk cache and other features.
 */
public final class LoaderHandler implements DiskCacheImpl.Callback, DownloaderImpl.Callback, ComponentCallbacks2 {

    //region: Fields and Consts

    /**
     * map of url to image request running it to reuse if same image is requested again
     */
    private final Map<String, ImageRequest> mLoadingRequests = new HashMap<>();

    /**
     * Memory cache for images loaded
     */
    private final MemoryPool mMemoryPool;

    /**
     * Disk cache for images loaded
     */
    private final DiskCache mDiskCache;

    /**
     * Downloader to download images from the web
     */
    private final Downloader mDownloader;

    /**
     * Used to decode images from the disk to bitmap.
     */
    private final Decoder mDecoder;

    /**
     * Used to post execution to main thread.
     */
    private final Handler mHandler;

    /**
     * stats on the number of memory cache hits
     */
    private int mMemoryHits;

    /**
     * stats on the number of memory cache hits for alternative sepc
     */
    private int mMemoryAltHits;

    /**
     * stats on the number of disk cache hits
     */
    private int mDiskHits;

    /**
     * stats on the number of disk cache hits for alternative sepc
     */
    private int mDiskAltHits;

    /**
     * stats on the number of cache miss, network begin request
     */
    private int mNetworkRequests;

    /**
     * stats on the number of cache miss, network loaded
     */
    private int mNetworkLoads;
    //endregion

    /**
     * Init.
     *
     * @param decoder Used to decode images from the disk to bitmap.
     */
    public LoaderHandler(Application application,
                         MemoryPool memoryPool,
                         DiskCache diskCache,
                         Downloader downloader,
                         Decoder decoder) {
        FILUtils.notNull(application, "application");
        FILUtils.notNull(memoryPool, "memoryPool");
        FILUtils.notNull(diskCache, "diskCache");
        FILUtils.notNull(downloader, "downloader");
        FILUtils.notNull(decoder, "decoder");

        mMemoryPool = memoryPool;
        mDiskCache = diskCache;
        mDownloader = downloader;
        mDecoder = decoder;

        mHandler = new Handler(application.getMainLooper());

        application.registerComponentCallbacks(this);
    }

    /**
     * Create image report for analyses.
     */
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Image handler report:");
        sb.append('\n');
        sb.append("Memory Hit: ").append(mMemoryHits).append('\n');
        sb.append("Memory alt Hit: ").append(mMemoryAltHits).append('\n');
        sb.append("Disk Hit: ").append(mDiskHits).append('\n');
        sb.append("Disk alt Hit: ").append(mDiskAltHits).append('\n');
        sb.append("Network Requests: ").append(mNetworkRequests).append('\n');
        sb.append("Network Loaded: ").append(mNetworkLoads).append('\n');
        sb.append('\n');
        //mMemoryPool.report(sb);
        sb.append('\n');
        //mDiskCache.report(sb);
        return sb.toString();
    }

    /**
     * Prefetch image (uri+spec) to be available in disk cache.<br>
     *
     * @param uri the URI of the image to prefetch
     * @param spec the spec to prefetch the image by
     */
    public void prefetchImage(String uri, ImageLoadSpec spec) {
        try {
            String imageKey = ImageRequest.getUriUniqueKey(spec, uri);
            ImageRequest request = mLoadingRequests.get(imageKey);
            if (request == null) {
                File file = mDiskCache.getCacheFile(uri, spec);
                if (!file.exists()) {
                    request = new ImageRequest(uri, spec, file);
                    mLoadingRequests.put(imageKey, request);

                    FILLogger.debug("Add prefetch request... [{}]", request);
                    mDownloader.downloadAsync(request, true, this);
                }
            }
        } catch (Exception e) {
            FILLogger.critical("Error in prefetch image [{}] [{}]", e, uri, spec);
        }
    }

    /**
     * Load image by given URL to the given target.<br>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.
     */
    public void loadImage(Target target, ImageLoadSpec spec, ImageLoadSpec altSpec) {
        try {
            String uri = target.getUri();
            if (!TextUtils.isEmpty(uri)) {

                ReusableBitmap image = mMemoryPool.get(uri, spec, altSpec);
                if (image != null) {
                    mMemoryHits++;
                    if (image.getSpec() != spec)
                        mMemoryAltHits++;
                    target.onBitmapLoaded(image, LoadedFrom.MEMORY);
                }

                // not found or loaded alternative spec
                if (image == null || image.getSpec() != spec) {
                    String imageKey = ImageRequest.getUriUniqueKey(spec, uri);
                    ImageRequest request = mLoadingRequests.get(imageKey);
                    if (request != null) {
                        FILLogger.debug("Memory cache miss, image already requested, add target to request... [{}] [{}]", request, target);
                        if (request.addTargetAndCheck(target)) {
                            mDownloader.downloadAsync(request, false, this);
                        }
                    } else {
                        // start async process of loading image from disk cache or network
                        request = new ImageRequest(target, uri, spec, mDiskCache.getCacheFile(uri, spec));
                        mLoadingRequests.put(imageKey, request);

                        FILLogger.debug("Memory cache miss, start request handling... [{}]", request);
                        // don't use alternative spec if image was loaded from memory cache
                        mDiskCache.getAsync(request, image == null ? altSpec : null, mDecoder, mMemoryPool, this);
                    }
                }
            }
        } catch (Exception e) {
            FILLogger.critical("Error in load image [{}]", e, target);
            target.onBitmapFailed();
        }
    }

    /**
     * Clear the disk image cache, deleting all cached images.
     */
    public void clearDiskCache() {
        mDiskCache.clear();
    }

    //region: Private methods

    @Override
    public void loadImageDiskCacheCallback(final ImageRequest imageRequest, final boolean canceled) {
        if (FILUtils.isOnMainThread()) {
            onLoadImageDiskCacheCallback(imageRequest, canceled);
        } else
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadImageDiskCacheCallback(imageRequest, canceled);
                }
            });
    }

    @Override
    public void loadImageDownloaderCallback(final ImageRequest imageRequest, final boolean downloaded, final boolean canceled) {

        // if downloaded and request is still valid - load the image object
        if (downloaded && !canceled && !imageRequest.isPrefetch()) {
            mDecoder.decode(mMemoryPool, imageRequest, imageRequest.getFile(), imageRequest.getSpec());
        }

        if (FILUtils.isOnMainThread()) {
            onLoadImageDownloaderCallback(imageRequest, downloaded, canceled);
        } else
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadImageDownloaderCallback(imageRequest, downloaded, canceled);
                }
            });
    }

    /**
     * Callback after the disk cache loaded the image or returned cache miss.<br>
     * Hit - set the loaded image on the requesting target.<br>
     * Miss - pass the request to image downloader.<br>
     */
    private void onLoadImageDiskCacheCallback(ImageRequest imageRequest, boolean canceled) {
        try {
            boolean loaded = imageRequest.getBitmap() != null;
            boolean loadedAlt = loaded && imageRequest.getBitmap().getSpec() != imageRequest.getSpec();
            FILLogger.debug("Get image from disk cache callback... [Loaded: {}, Alt:{}] [Canceled: {}] [{}]", loaded, loadedAlt, canceled, imageRequest);

            if (loaded) {
                // if image object was loaded - add it to memory cache
                mMemoryPool.set(imageRequest.getBitmap());
            }

            if (imageRequest.isValid()) {
                if (loaded) {
                    // if some image was loaded set it to targets
                    if (loadedAlt) {
                        mDiskAltHits++;
                    } else {
                        mDiskHits++;
                    }
                    for (Target target : imageRequest.getValidTargets()) {
                        target.onBitmapLoaded(imageRequest.getBitmap(), LoadedFrom.DISK);
                    }
                }
                if (loaded && !loadedAlt) {
                    // if primary loaded we are done
                    mLoadingRequests.remove(imageRequest.getUniqueKey());
                } else {
                    // need to download primary
                    if (canceled) {
                        // race-condition, canceled request that add valid target (run again)
                        mDiskCache.getAsync(imageRequest, null, mDecoder, mMemoryPool, this);
                    } else {
                        mNetworkRequests++;
                        mDownloader.downloadAsync(imageRequest, false, this);
                    }
                }
            } else {
                mLoadingRequests.remove(imageRequest.getUniqueKey());
            }
        } catch (Exception e) {
            mLoadingRequests.remove(imageRequest.getUniqueKey());
            FILLogger.critical("Error in load image disk callback", e);
        }
    }

    /**
     * Callback after image downloader downloaded the image and loaded from disk, failed or canceled.<br>
     * Success - set the loaded image on the requesting target.<br>
     * Failed - set failure on the requesting target.<br>
     */
    private void onLoadImageDownloaderCallback(ImageRequest imageRequest, boolean downloaded, boolean canceled) {
        try {
            FILLogger.debug("Load image from network callback... [{}] [Downloaded: {}] [Canceled: {}]", imageRequest, downloaded, canceled);

            // if image was downloaded - notify disk cache
            if (downloaded) {
                mDiskCache.imageAdded(imageRequest.getFileSize());
            }

            // if image object was loaded - add it to memory cache
            if (imageRequest.getBitmap() != null) {
                mMemoryPool.set(imageRequest.getBitmap());
            }

            // request are valid if there is target or prefetch, but here we don't care for prefetch
            if (imageRequest.isValid() && !imageRequest.isPrefetch()) {
                if (imageRequest.getBitmap() != null) {
                    mNetworkLoads++;
                    mLoadingRequests.remove(imageRequest.getUniqueKey());
                    for (Target target : imageRequest.getValidTargets()) {
                        target.onBitmapLoaded(imageRequest.getBitmap(), LoadedFrom.NETWORK);
                    }
                } else {
                    if (canceled) {
                        // race-condition, canceled request that add valid target (run again)
                        mDiskCache.getAsync(imageRequest, null, mDecoder, mMemoryPool, this);
                    } else {
                        mLoadingRequests.remove(imageRequest.getUniqueKey());
                        for (Target target : imageRequest.getValidTargets()) {
                            target.onBitmapFailed();
                        }
                    }
                }
            } else {
                mLoadingRequests.remove(imageRequest.getUniqueKey());
                imageRequest.setBitmap(null);
            }
        } catch (Exception e) {
            mLoadingRequests.remove(imageRequest.getUniqueKey());
            FILLogger.critical("Error in load image downloader callback", e);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        mMemoryPool.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {
        mMemoryPool.onTrimMemory(0);
    }
    //endregion
}