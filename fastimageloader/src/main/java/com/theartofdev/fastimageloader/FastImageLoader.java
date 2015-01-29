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
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

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
    private ImageLoadHandler mImageLoadHandler;

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
     * {@link #setDefaultUriEnhancer(UriEnhancer)},
     * {@link #setHttpClient(com.squareup.okhttp.OkHttpClient)},
     * {@link #setDebugIndicator(boolean)}.
     *
     * @param application the android mApplication instance
     * @throws IllegalStateException already initialized
     */
    public static FastImageLoader init(Application application) {
        Utils.notNull(application, "context");

        if (INST.mImageLoadHandler == null) {
            INST.mApplication = application;

            Utils.density = application.getResources().getDisplayMetrics().density;

            Display display = ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getSize(Utils.displaySize);

            return INST;
        } else {
            throw new IllegalStateException("Fast Image Loader is already initialized");
        }
    }

    /**
     * used to enhance image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    public FastImageLoader setDefaultUriEnhancer(UriEnhancer uriEnhancer) {
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
     * Create {@link com.theartofdev.fastimageloader.ImageLoadSpec} using
     * {@link com.theartofdev.fastimageloader.ImageLoadSpecBuilder}.<br/>
     * <p/>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @throws IllegalStateException NOT initialized
     */
    public static ImageLoadSpecBuilder createSpec() {
        if (INST.mImageLoadHandler == null) {
            finishInit();
        }
        return new ImageLoadSpecBuilder(INST.mApplication, INST.mUriEnhancer);
    }

    /**
     * Load image by and to the given target.<br/>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br/>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.<br/>
     * <p/>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @param target the target to load the image to, use it's URL and Spec
     * @param altSpec optional: alternative specification to load image from cache if primary is no available in cache.
     * @throws IllegalStateException NOT initialized
     */
    public static void loadImage(Target target, ImageLoadSpec altSpec) {
        Utils.notNull(target, "target");
        if (INST.mImageLoadHandler == null) {
            finishInit();
        }
        INST.mImageLoadHandler.loadImage(target, altSpec);
    }

    /**
     * Clear the disk image cache, deleting all cached images.
     * <p/>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @throws IllegalStateException NOT initialized.
     */
    public static void clearDiskCache() {
        if (INST.mImageLoadHandler == null) {
            finishInit();
        }
        INST.mImageLoadHandler.clearDiskCache();
    }

    //region: Private methods

    /**
     * Finish the initialization process.
     *
     * @throws IllegalStateException NOT initialized.
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
            INST.mImageLoadHandler = new ImageLoadHandler(INST.mApplication, INST.mHttpClient);
        } else {
            throw new IllegalStateException("Fast Image Loader is NOT initialized, call init(...)");
        }
    }
    //endregion
}