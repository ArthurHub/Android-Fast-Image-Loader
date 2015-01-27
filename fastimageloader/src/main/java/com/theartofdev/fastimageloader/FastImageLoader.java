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
    private ImageLoaderHandler mImageLoaderHandler;

    /**
     * Android application to init by
     */
    private Application mApplication;

    /**
     * used to enhance image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    private UriEnhancer mUriEnhancer;

    /**
     * The OK HTTP client to be used to download images
     */
    private OkHttpClient mHttpClient;
    //endregion

    /**
     * Prevent init.
     */
    private FastImageLoader() {
    }

    /**
     * Initialize the image loader with given android application context.<br/>
     * Image loader can be initialized only once where you can set all the configuration
     * properties:
     * {@link #setUriEnhancer(UriEnhancer)},
     * {@link #setHttpClient(com.squareup.okhttp.OkHttpClient)},
     * {@link #setDebugIndicator(boolean)}.
     *
     * @param application the android mApplication instance
     * @throws IllegalStateException already initialized
     */
    public static FastImageLoader init(Application application) {
        Utils.notNull(application, "context");

        if (INST.mImageLoaderHandler == null) {
            INST.mApplication = application;
            Utils.density = application.getResources().getDisplayMetrics().density;
            return INST;
        } else {
            throw new IllegalStateException("Fast Image Loader is already initialized");
        }
    }

    /**
     * used to enhance image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    public FastImageLoader setUriEnhancer(UriEnhancer uriEnhancer) {
        mUriEnhancer = uriEnhancer;
        return INST;
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
        Utils.debugIndicator = enable;
        return INST;
    }

    /**
     * Load image by and to the given target.<br/>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br/>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.
     *
     * @param target the target to load the image to, use it's URL and Spec
     * @param altSpec optional: alternative specification to load image from cache if primary is no available in cache.
     * @throws IllegalStateException NOT initialized
     */
    public static void loadImage(Target target, ImageLoadSpec altSpec) {
        Utils.notNull(target, "target");
        if (INST.mImageLoaderHandler == null) {
            finishInit();
        }
        INST.mImageLoaderHandler.loadImage(target, altSpec);
    }

    /**
     * Finish the initialization process.
     */
    private static void finishInit() {
        if (INST.mApplication != null) {
            if (INST.mHttpClient == null) {
                INST.mHttpClient = new OkHttpClient();
                INST.mHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
                INST.mHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
            }
            if (INST.mUriEnhancer == null) {
                INST.mUriEnhancer = new UriEnhancerIdentity();
            }
            INST.mImageLoaderHandler = new ImageLoaderHandler(INST.mApplication, INST.mHttpClient, INST.mUriEnhancer);
        } else {
            throw new IllegalStateException("Fast Image Loader is NOT initialized, call init(...)");
        }
    }
}