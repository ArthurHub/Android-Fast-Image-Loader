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

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.theartofdev.fastimageloader.enhancer.ImageServiceUriEnhancer;

import java.util.concurrent.TimeUnit;

/**
 * TODO:a add doc
 */
public final class FastImageLoader {

    //region: Fields and Consts

    /**
     * Single instance of the class
     */
    private static final FastImageLoader INST = new FastImageLoader();

    /**
     * Handler for image loading logic
     */
    private ImageHandler mImageHandler;

    /**
     * The OK HTTP client to be used to download images
     */
    private OkHttpClient mHttpClient;

    /**
     * Is to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    private boolean mDebugIndicator;
    //endregion

    /**
     * Prevent init.
     */
    private FastImageLoader() {
    }

    /**
     * Is to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    public static boolean getDebugIndicator() {
        return INST.mDebugIndicator;
    }

    /**
     * The OK HTTP client to be used to download images
     */
    public FastImageLoader setHttpClient(OkHttpClient httpClient) {
        mHttpClient = httpClient;
        return INST;
    }

    /**
     * Is to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    public FastImageLoader setDebugIndicator(boolean enable) {
        mDebugIndicator = enable;
        return INST;
    }

    /**
     * Initialize the image loader with given android application context.
     *
     * @param context the android application instance
     * @param urlEnhancer Enhancer to use for image loading URL with format/size/etc. parameters by image loading
     * specification.
     * @throws IllegalStateException already initialized
     */
    public static FastImageLoader init(Application context, ImageServiceUriEnhancer urlEnhancer) {
        CommonUtils.notNull(context, "context");
        CommonUtils.notNull(urlEnhancer, "urlEnhancer");

        if (INST.mImageHandler == null) {
            CommonUtils.density = context.getResources().getDisplayMetrics().density;

            if (INST.mHttpClient == null) {
                INST.mHttpClient = new OkHttpClient();
                INST.mHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
                INST.mHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
            }

            INST.mImageHandler = new ImageHandler(context, INST.mHttpClient, urlEnhancer);
            return INST;
        } else {
            throw new IllegalStateException("Fast Image Loader is already initialized");
        }
    }

    /**
     * Load image by and to the given target.<br/>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br/>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.
     *
     * @param target the target to load the image to, use it's URL and Spec
     * @param altSpec optional: alternative specification to load image from cache if primary is no available in cache.
     * @throws IllegalStateException already initialized
     */
    public static void loadImage(Target target, ImageLoadSpec altSpec) {
        CommonUtils.notNull(target, "target");
        if (INST.mImageHandler != null) {
            INST.mImageHandler.loadImage(target, altSpec);
        } else {
            throw new IllegalStateException("Fast Image Loader is NOT initialized, call init(...)");
        }
    }
}