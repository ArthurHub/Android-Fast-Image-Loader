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

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.theartofdev.fastimageloader.enhancer.ImageServiceUriEnhancer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for image loading using memory/disk cache and other features.
 */
final class ImageHandler implements ImageDiskCache.GetCallback, ImageDownloader.Callback {

    //region: Fields and Consts

    /**
     * map of url to image request running it to reuse if same image is requested again
     */
    private final Map<String, ImageRequest> mLoadingRequests = new HashMap<>();

    /**
     * Enhance image loading URL with format/size/etc. parameters by image loading specification.
     */
    private ImageServiceUriEnhancer mUrlEnhancer;

    /**
     * The folder to save the cached images in
     */
    private final File mCacheFolder;

    /**
     * Memory cache for images loaded
     */
    private final ImageMemoryCache mMemoryCache;

    /**
     * Disk cache for images loaded
     */
    private final ImageDiskCache mDiskCache;

    /**
     * Downloader to download images from the web
     */
    private final ImageDownloader mDownloader;

    /**
     * stats on the number of memory cache hits
     */
    private int mMemoryHits;

    /**
     * stats on the number of memory cache hits for fallback dimension
     */
    private int mMemoryFallbackHits;

    /**
     * stats on the number of disk cache hits
     */
    private int mDiskHits;

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
     */
    public ImageHandler(Context context, ImageServiceUriEnhancer urlEnhancer) {

        mUrlEnhancer = urlEnhancer;

        mCacheFolder = new File(CommonUtils.pathCombine(context.getCacheDir().getPath(), "ImageCache"));

        //noinspection ResultOfMethodCallIgnored
        mCacheFolder.mkdirs();

        mMemoryCache = new ImageMemoryCache();

        Handler handler = new Handler();
        ImageLoader imageLoader = new ImageLoader(mMemoryCache);
        mDiskCache = new ImageDiskCache(context, handler, imageLoader, mCacheFolder);

        mDownloader = new ImageDownloader(handler, imageLoader);
    }

    /**
     * Create image report for analyses.
     */
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Image handler report:");
        sb.append('\n');
        sb.append("Memory Hit: ").append(mMemoryHits).append('\n');
        sb.append("Memory fallback Hit: ").append(mMemoryFallbackHits).append('\n');
        sb.append("Disk Hit: ").append(mDiskHits).append('\n');
        sb.append("Network Requests: ").append(mNetworkRequests).append('\n');
        sb.append("Network Loaded: ").append(mNetworkLoads).append('\n');
        sb.append('\n');
        mMemoryCache.report(sb);
        sb.append('\n');
        mDiskCache.report(sb);
        return sb.toString();
    }

    /**
     * Load image by given URL to the given target.<br/>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br/>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.
     */
    public void loadImage(Target target, ImageLoadSpec altSpec) {
        try {
            String url = target.getUrl();
            ImageLoadSpec spec = target.getSpec();
            if (!TextUtils.isEmpty(url)) {

                String enhancedUrl = mUrlEnhancer.enhance(url, spec);

                RecycleBitmapImpl image = mMemoryCache.get(url, spec);
                if (image != null) {
                    mMemoryHits++;
                    target.onBitmapLoaded(image, LoadedFrom.MEMORY);
                } else {

                    // try fallback memory cached image
                    if (altSpec != null) {
                        image = mMemoryCache.get(url, altSpec);
                        if (image != null) {
                            mMemoryFallbackHits++;
                            target.onBitmapLoaded(image, LoadedFrom.MEMORY);
                        }
                    }

                    ImageRequest imageRequest = mLoadingRequests.get(enhancedUrl);
                    if (imageRequest != null) {
                        Logger.debug("Memory cache miss, image already requested, add target to request... [{}] [{}]", imageRequest, target);
                        imageRequest.addTarget(target);
                    } else {
                        // start async process of loading image from disk cache or network
                        imageRequest = new ImageRequest(target, url, enhancedUrl, spec, getCacheFile(enhancedUrl));
                        mLoadingRequests.put(enhancedUrl, imageRequest);

                        Logger.debug("Memory cache miss, start image request handling... [{}]", imageRequest);
                        mDiskCache.getAsync(imageRequest, this);
                    }
                }
            }
        } catch (Exception e) {
            Logger.critical("Error in load image [{}]", e, target);
            target.onBitmapFailed();
        }
    }

    //region: Private methods

    /**
     * Callback after the disk cache loaded the image or returned cache miss.<br/>
     * Hit - set the loaded image on the requesting target.<br/>
     * Miss - pass the request to image downloader.<br/>
     */
    @Override
    public void loadImageGetDiskCacheCallback(ImageRequest imageRequest, boolean canceled) {
        try {
            Logger.debug("Get image from disk cache callback... [{}] [Canceled: {}]", imageRequest, canceled);

            // if image object was loaded - add it to memory cache
            if (imageRequest.getBitmap() != null) {
                mMemoryCache.set(imageRequest.getBitmap());
            }

            if (imageRequest.isValid()) {
                if (imageRequest.getBitmap() != null) {
                    mDiskHits++;
                    mLoadingRequests.remove(imageRequest.getThumborUrl());
                    for (Target target : imageRequest.getValidTargets()) {
                        target.onBitmapLoaded(imageRequest.getBitmap(), LoadedFrom.DISK);
                    }
                } else {
                    if (canceled) {
                        // race-condition, canceled request that add valid target (run again)
                        mDiskCache.getAsync(imageRequest, this);
                    } else {
                        mNetworkRequests++;
                        mDownloader.downloadAsync(imageRequest, this);
                    }
                }
            } else {
                mLoadingRequests.remove(imageRequest.getThumborUrl());
            }
        } catch (Exception e) {
            mLoadingRequests.remove(imageRequest.getThumborUrl());
            Logger.critical("Error in load image disk callback", e);
        }
    }

    /**
     * Callback after image downloader downloaded the image and loaded from disk, failed or canceled.<br/>
     * Success - set the loaded image on the requesting target.<br/>
     * Failed - set failure on the requesting target.<br/>
     */
    @Override
    public void loadImageDownloaderCallback(ImageRequest imageRequest, boolean downloaded, boolean canceled) {
        try {
            Logger.debug("Load image from network callback... [{}] [Downloaded: {}] [Canceled: {}]", imageRequest, downloaded, canceled);

            // if image was downloaded - notify disk cache
            if (downloaded) {
                mDiskCache.imageAdded(imageRequest.getFileSize());
            }

            // if image object was loaded - add it to memory cache
            if (imageRequest.getBitmap() != null) {
                mMemoryCache.set(imageRequest.getBitmap());
            }

            if (imageRequest.isValid()) {
                if (imageRequest.getBitmap() != null) {
                    mNetworkLoads++;
                    mLoadingRequests.remove(imageRequest.getThumborUrl());
                    for (Target target : imageRequest.getValidTargets()) {
                        target.onBitmapLoaded(imageRequest.getBitmap(), LoadedFrom.NETWORK);
                    }
                } else {
                    if (canceled) {
                        // race-condition, canceled request that add valid target (run again)
                        mDiskCache.getAsync(imageRequest, this);
                    } else {
                        mLoadingRequests.remove(imageRequest.getThumborUrl());
                        for (Target target : imageRequest.getValidTargets()) {
                            target.onBitmapFailed();
                        }
                    }
                }
            } else {
                mLoadingRequests.remove(imageRequest.getThumborUrl());
                imageRequest.setBitmap(null);
            }
        } catch (Exception e) {
            mLoadingRequests.remove(imageRequest.getThumborUrl());
            Logger.critical("Error in load image downloader callback", e);
        }
    }

    /**
     * Gets the representation of the online uri on the local disk.
     *
     * @param uri The online image uri
     * @return The path of the file on the disk
     */
    private File getCacheFile(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }
        String name = Integer.toHexString(uri.substring(0, lastSlash).hashCode()) + "_" + uri.substring(lastSlash + 1).hashCode();
        return new File(CommonUtils.pathCombine(mCacheFolder.getAbsolutePath(), name));
    }
    //endregion
}