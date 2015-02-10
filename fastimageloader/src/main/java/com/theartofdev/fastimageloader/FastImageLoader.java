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
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.theartofdev.fastimageloader.adapter.IdentityAdapter;
import com.theartofdev.fastimageloader.impl.LoaderHandler;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;
import com.theartofdev.fastimageloader.target.TargetHelper;

import java.util.HashMap;
import java.util.Map;
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
     * The defined image loading specs
     */
    private final Map<String, ImageLoadSpec> mSpecs = new HashMap<>();

    /**
     * Handler for image loading logic
     */
    private LoaderHandler mLoaderHandler;

    /**
     * Android application to init by
     */
    private Application mApplication;

    /**
     * used to convert image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    private ImageServiceAdapter mImageServiceAdapter;

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
     * {@link #setDefaultUriEnhancer(ImageServiceAdapter)},
     * {@link #setHttpClient(com.squareup.okhttp.OkHttpClient)},
     * {@link #setDebugIndicator(boolean)}.
     *
     * @param application the android mApplication instance
     * @throws IllegalStateException already initialized
     */
    public static FastImageLoader init(Application application) {
        FILUtils.notNull(application, "context");

        if (INST.mLoaderHandler == null) {
            INST.mApplication = application;
            return INST;
        } else {
            throw new IllegalStateException("Fast Image Loader is already initialized");
        }
    }

    /**
     * used to convert image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    public FastImageLoader setDefaultUriEnhancer(ImageServiceAdapter imageServiceAdapter) {
        mImageServiceAdapter = imageServiceAdapter;
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
     * If to write logs to logcat (Default: false).
     */
    public FastImageLoader setWriteLogsToLogcat(boolean enable) {
        FILLogger.mLogcatEnabled = enable;
        return INST;
    }

    /**
     * The min log level to write logs at, logs below this level are ignored (Default: INFO).<br/>
     * Use: {@link Log#DEBUG}, {@link Log#INFO}, {@link Log#WARN}, {@link Log#ERROR}, {@link Log#ASSERT}.
     */
    public FastImageLoader setLogLevel(int level) {
        FILLogger.mLogLevel = level;
        return INST;
    }

    /**
     * Set appender to use to send logs to, allow client to log this library inner logs into custom framework.
     */
    public FastImageLoader setLogAppender(LogAppender appender) {
        FILLogger.mAppender = appender;
        return INST;
    }

    /**
     * Is to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    public FastImageLoader setDebugIndicator(boolean enable) {
        TargetHelper.debugIndicator = enable;
        return INST;
    }

    /**
     * Create {@link com.theartofdev.fastimageloader.ImageLoadSpec} using
     * {@link com.theartofdev.fastimageloader.ImageLoadSpecBuilder}.<br/>
     * <p/>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @param key the unique key of the spec used for identification
     * @throws IllegalStateException NOT initialized
     * @throws IllegalArgumentException spec with the given key already defined
     */
    public static ImageLoadSpecBuilder buildSpec(String key) {
        FILUtils.notNullOrEmpty(key, "key");
        if (INST.mLoaderHandler == null) {
            finishInit();
        }
        if (INST.mSpecs.containsKey(key)) {
            throw new IllegalArgumentException("Spec with the same key already exists");
        }
        return new ImageLoadSpecBuilder(key, INST.mApplication, INST.mImageServiceAdapter);
    }

    /**
     * Get {@link com.theartofdev.fastimageloader.ImageLoadSpec} for the given key if exists.
     *
     * @param key the unique key of the spec used for identification
     * @return spec instance or null if no matching spec found
     */
    public static ImageLoadSpec getSpec(String key) {
        FILUtils.notNullOrEmpty(key, "key");
        return INST.mSpecs.get(key);
    }

    /**
     * Prefetch image (uri+spec) to be available in disk cache.<br/>
     *
     * @param uri the URI of the image to prefetch
     * @param specKey the spec to prefetch the image by
     */
    public static void prefetchImage(String uri, String specKey) {
        FILUtils.notNullOrEmpty(specKey, "specKey");
        if (!TextUtils.isEmpty(uri)) {
            if (INST.mLoaderHandler == null) {
                finishInit();
            }
            ImageLoadSpec spec = INST.mSpecs.get(specKey);
            if (spec == null) {
                throw new IllegalArgumentException("Invalid spec key, no spec defined for the given key: " + specKey);
            }
            INST.mLoaderHandler.prefetchImage(uri, spec);
        }
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
     * @param altSpecKey optional: alternative specification to load image from cache if primary is no available in
     * cache.
     * @throws IllegalStateException NOT initialized
     */
    public static void loadImage(Target target, String altSpecKey) {
        FILUtils.notNull(target, "target");
        if (INST.mLoaderHandler == null) {
            finishInit();
        }
        ImageLoadSpec spec = INST.mSpecs.get(target.getSpecKey());
        ImageLoadSpec altSpec = altSpecKey != null ? INST.mSpecs.get(altSpecKey) : null;
        if (spec == null) {
            throw new IllegalArgumentException("Invalid spec key, no spec defined for the given key: " + target.getSpecKey());
        }
        if (altSpecKey != null && altSpec == null) {
            throw new IllegalArgumentException("Invalid alternative spec key, no spec defined for the given key: " + altSpecKey);
        }
        INST.mLoaderHandler.loadImage(target, spec, altSpec);
    }

    /**
     * Clear the disk image cache, deleting all cached images.
     * <p/>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @throws IllegalStateException NOT initialized.
     */
    public static void clearDiskCache() {
        if (INST.mLoaderHandler == null) {
            finishInit();
        }
        INST.mLoaderHandler.clearDiskCache();
    }

    /**
     * Add the given image load spec to the defined specs.
     */
    static void addSpec(ImageLoadSpec spec) {
        INST.mSpecs.put(spec.getKey(), spec);
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
            if (INST.mImageServiceAdapter == null) {
                INST.mImageServiceAdapter = new IdentityAdapter();
            }
            INST.mLoaderHandler = new LoaderHandler(INST.mApplication, INST.mHttpClient);
        } else {
            throw new IllegalStateException("Fast Image Loader is NOT initialized, call init(...)");
        }
    }
    //endregion
}