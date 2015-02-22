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
import android.text.format.DateUtils;
import android.util.Log;

import com.theartofdev.fastimageloader.adapter.IdentityAdapter;
import com.theartofdev.fastimageloader.impl.DecoderImpl;
import com.theartofdev.fastimageloader.impl.DiskCacheImpl;
import com.theartofdev.fastimageloader.impl.DownloaderImpl;
import com.theartofdev.fastimageloader.impl.LoaderHandler;
import com.theartofdev.fastimageloader.impl.MemoryPoolImpl;
import com.theartofdev.fastimageloader.impl.NativeHttpClient;
import com.theartofdev.fastimageloader.impl.OkHttpClient;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;
import com.theartofdev.fastimageloader.target.TargetHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    private ImageServiceAdapter mDefaultImageServiceAdapter;

    /**
     * The folder to use for disk caching.
     */
    private File mCacheFolder;

    /**
     * The max size of the cache (50MB)
     */
    private long mCacheMaxSize = 50 * 1024 * 1024;

    /**
     * The max time image is cached without use before delete
     */
    private long mCacheTtl = 8 * DateUtils.DAY_IN_MILLIS;

    /**
     * Used to decode images from the disk to bitmap.
     */
    private Decoder mDecoder;

    /**
     * the memory pool to use
     */
    private MemoryPool mMemoryPool;

    /**
     * the disk cache to use
     */
    private DiskCache mDiskCache;

    /**
     * the downloader to use
     */
    private Downloader mDownloader;

    /**
     * The HTTP client to be used to download images
     */
    private HttpClient mHttpClient;
    //endregion

    /**
     * Prevent init.
     */
    private FastImageLoader() {
    }

    /**
     * Initialize the image loader with given android application context.<br>
     * Image loader can be initialized only once where you can set all the configuration
     * properties:
     * {@link #setDefaultImageServiceAdapter(ImageServiceAdapter)},
     * {@link #setHttpClient(HttpClient)},
     * {@link #setDebugIndicator(boolean)}.
     *
     * @param application the android mApplication instance
     * @throws IllegalStateException already initialized
     */
    public static FastImageLoader init(Application application) {
        FILUtils.notNull(application, "context");

        if (INST.mLoaderHandler == null) {
            FILLogger.debug("Init fast image loader...");
            FILUtils.MainThreadId = application.getMainLooper().getThread().getId();
            TargetHelper.mDensity = application.getResources().getDisplayMetrics().density;
            INST.mApplication = application;
            return INST;
        } else {
            throw new IllegalStateException("Fast Image Loader is already initialized");
        }
    }

    /**
     * used to convert image URI by spec for image service (Thumbor\imgIX\etc.)
     */
    public FastImageLoader setDefaultImageServiceAdapter(ImageServiceAdapter imageServiceAdapter) {
        mDefaultImageServiceAdapter = imageServiceAdapter;
        return INST;
    }

    /**
     * Set the folder to use for disk caching.<br>
     * This setter is ignored if {@link #setDiskCache(DiskCache)} is used.
     */
    public FastImageLoader setCacheFolder(File cacheFolder) {
        mCacheFolder = cacheFolder;
        return INST;
    }

    /**
     * The max size of the disk cache (default 50MB).<br>
     * This setter is ignored if {@link #setDiskCache(DiskCache)} is used.
     */
    public FastImageLoader setCacheMaxSize(long cacheMaxSize) {
        mCacheMaxSize = cacheMaxSize;
        return INST;
    }

    /**
     * The max time image is cached without use before is delete (default 8 days).<br>
     * This setter is ignored if {@link #setDiskCache(DiskCache)} is used.
     */
    public FastImageLoader setCacheTtl(long cacheTtl) {
        mCacheTtl = cacheTtl;
        return INST;
    }

    /**
     * Used to decode images from the disk to bitmap.
     */
    public FastImageLoader setDecoder(Decoder decoder) {
        mDecoder = decoder;
        return INST;
    }

    /**
     * Set the memory pool handler to be used.
     */
    public FastImageLoader setMemoryPool(MemoryPool memoryPool) {
        mMemoryPool = memoryPool;
        return INST;
    }

    /**
     * Set the disk cache handler to be used.
     */
    public FastImageLoader setDiskCache(DiskCache diskCache) {
        mDiskCache = diskCache;
        return INST;
    }

    /**
     * Set the downloader handler to be used.
     */
    public FastImageLoader setDownloader(Downloader downloader) {
        mDownloader = downloader;
        return INST;
    }

    /**
     * The HTTP client to be used to download images
     * This setter is ignored if {@link #setDownloader(Downloader)} is used.
     */
    public FastImageLoader setHttpClient(HttpClient httpClient) {
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
     * The min log level to write logs at, logs below this level are ignored (Default: INFO).<br>
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
     * {@link com.theartofdev.fastimageloader.ImageLoadSpecBuilder}.<br>
     * <br><br>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @param key the unique key of the spec used for identification
     * @throws IllegalStateException NOT initialized
     * @throws IllegalArgumentException spec with the given key already defined
     */
    public static ImageLoadSpecBuilder buildSpec(String key) {
        FILUtils.notNullOrEmpty(key, "key");
        FILUtils.verifyOnMainThread();

        INST.finishInit();
        if (INST.mSpecs.containsKey(key)) {
            throw new IllegalArgumentException("Spec with the same key already exists");
        }

        FILLogger.debug("Create image load spec... [{}]", key);
        return new ImageLoadSpecBuilder(key, INST.mApplication, INST.mDefaultImageServiceAdapter);
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
     * Prefetch image (uri+spec) to be available in disk cache.<br>
     *
     * @param uri the URI of the image to prefetch
     * @param specKey the spec to prefetch the image by
     */
    public static void prefetchImage(String uri, String specKey) {
        FILUtils.notNullOrEmpty(specKey, "specKey");
        FILUtils.verifyOnMainThread();

        if (!TextUtils.isEmpty(uri)) {

            INST.finishInit();
            ImageLoadSpec spec = INST.mSpecs.get(specKey);
            if (spec == null) {
                throw new IllegalArgumentException("Invalid spec key, no spec defined for the given key: " + specKey);
            }

            FILLogger.debug("Prefetch image... [{}] [{}]", uri, spec);
            INST.mLoaderHandler.prefetchImage(uri, spec);
        }
    }

    /**
     * Load image by and to the given target.<br>
     * Handle transformation on the image, image dimension specification and dimension fallback.<br>
     * If the image of the requested dimensions is not found in memory cache we try to find the fallback dimension, if
     * found it will be set to the target, and the requested dimension image will be loaded async.<br>
     * <br><br>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @param target the target to load the image to, use it's URL and Spec
     * @param altSpecKey optional: alternative specification to load image from cache if primary is no available in
     * cache.
     * @throws IllegalStateException NOT initialized
     */
    public static void loadImage(Target target, String altSpecKey) {
        FILUtils.notNull(target, "target");
        FILUtils.verifyOnMainThread();

        INST.finishInit();
        ImageLoadSpec spec = INST.mSpecs.get(target.getSpecKey());
        ImageLoadSpec altSpec = altSpecKey != null ? INST.mSpecs.get(altSpecKey) : null;
        if (spec == null) {
            throw new IllegalArgumentException("Invalid spec key, no spec defined for the given key: " + target.getSpecKey());
        }
        if (altSpecKey != null && altSpec == null) {
            throw new IllegalArgumentException("Invalid alternative spec key, no spec defined for the given key: " + altSpecKey);
        }

        FILLogger.debug("Load image... [{}] [{}] [{}]", target, spec, altSpecKey);
        INST.mLoaderHandler.loadImage(target, spec, altSpec);
    }

    /**
     * Clear the disk image cache, deleting all cached images.
     * <br><br>
     * Must be initialized first using {@link #init(android.app.Application)}.
     *
     * @throws IllegalStateException NOT initialized.
     */
    public static void clearDiskCache() {
        INST.finishInit();

        FILLogger.debug("Clear image cache...");
        INST.mLoaderHandler.clearDiskCache();
    }

    /**
     * Add the given image load spec to the defined specs.
     */
    static void addSpec(ImageLoadSpec spec) {
        FILLogger.debug("Image load spec created... [{}]", spec);
        INST.mSpecs.put(spec.getKey(), spec);
    }

    //region: Private methods

    /**
     * Finish the initialization process.
     *
     * @throws IllegalStateException NOT initialized.
     */
    private void finishInit() {
        if (INST.mLoaderHandler == null) {
            if (INST.mApplication != null) {
                FILUtils.verifyOnMainThread();

                if (INST.mDefaultImageServiceAdapter == null) {
                    FILLogger.debug("Use default identity image service adapter...");
                    INST.mDefaultImageServiceAdapter = new IdentityAdapter();
                }
                if (mMemoryPool == null) {
                    FILLogger.debug("Use default memory pool...");
                    mMemoryPool = new MemoryPoolImpl();
                }
                if (mDecoder == null) {
                    FILLogger.debug("Use default decoder...");
                    mDecoder = new DecoderImpl();
                }
                if (mDiskCache == null) {
                    if (mCacheFolder == null) {
                        FILLogger.debug("Use default cache folder...");
                        mCacheFolder = new File(FILUtils.pathCombine(mApplication.getCacheDir().getPath(), "ImageCache"));
                    }
                    FILLogger.debug("Use default disk cache... [{}]", mCacheFolder);
                    mDiskCache = new DiskCacheImpl(mApplication, mCacheFolder, mCacheMaxSize, mCacheTtl);
                }
                if (mDownloader == null) {
                    initHttpClient();

                    FILLogger.debug("Use default downloader...");
                    mDownloader = new DownloaderImpl(mHttpClient);
                }

                FILLogger.debug("Create load handler... [{}] [{}] [{}]", mMemoryPool, mDiskCache, mDownloader);
                INST.mLoaderHandler = new LoaderHandler(mApplication, mMemoryPool, mDiskCache, mDownloader, mDecoder);
            } else {
                throw new IllegalStateException("Fast Image Loader is NOT initialized, call init(...)");
            }
        }
    }

    /**
     * Init HTTP client to be used by the downloader.<br>
     * 1. If one given externally use it.<br>
     * 2. Try using OK HTTP client, won't work if there is no OK HTTP dependency<br>
     * 3. Use native Android URL connection.<br>
     */
    private void initHttpClient() {
        if (INST.mHttpClient == null) {
            try {
                FILLogger.debug("Try create OK HTTP client...");
                INST.mHttpClient = new OkHttpClient();
            } catch (Throwable e) {
                if (e.getClass().isAssignableFrom(NoClassDefFoundError.class)) {
                    FILLogger.debug("OK HTTP dependency no found, use native Android URL Connection");
                } else {
                    FILLogger.warn("Failed to init OK HTTP client, use native Android URL Connection", e);
                }
                INST.mHttpClient = new NativeHttpClient();
            }
        }
    }
    //endregion
}