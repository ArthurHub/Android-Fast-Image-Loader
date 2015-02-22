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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.theartofdev.fastimageloader.Decoder;
import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.MemoryPool;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Disk cache for image handler.<br>
 */
public class DiskCacheImpl implements com.theartofdev.fastimageloader.DiskCache {

    //region: Fields and Consts

    /**
     * The key to persist stats last scan data
     */
    protected static final String STATS_LAST_SCAN = "DiskImageCache_lastCheck";

    /**
     * The key to persist stats cache size data
     */
    protected static final String STATS_CACHE_SIZE = "DiskImageCache_size";

    /**
     * The max size of the cache (50MB)
     */
    protected final long mMaxSize;

    /**
     * The bound to delete cached data to this size
     */
    protected final long mMaxSizeLowerBound;

    /**
     * The max time image is cached without use before delete
     */
    protected final long mCacheTtl;

    /**
     * The interval to execute cache scan even when max size has not reached
     */
    protected final long SCAN_INTERVAL = 2 * DateUtils.DAY_IN_MILLIS;

    /**
     * the folder that the image cached on disk are located
     */
    protected final File mCacheFolder;

    /**
     * Application context
     */
    protected final Context mContext;

    /**
     * Threads service for all read operations.
     */
    protected final ThreadPoolExecutor mReadExecutorService;

    /**
     * Threads service for scan of cached folder operation.
     */
    protected final ThreadPoolExecutor mScanExecutorService;

    /**
     * The time of the last cache check
     */
    private long mLastCacheScanTime = -1;

    /**
     * the current size of the cache
     */
    private long mCurrentCacheSize;
    //endregion

    /**
     * @param context the application object to read config stuff
     * @param cacheFolder the folder to keep the cached image data
     * @param maxSize the max size of the disk cache in bytes
     * @param cacheTtl the max time a cached image remains in cache without use before deletion
     */
    public DiskCacheImpl(Context context, File cacheFolder, long maxSize, long cacheTtl) {
        FILUtils.notNull(context, "context");
        FILUtils.notNull(cacheFolder, "cacheFolder");

        mContext = context;
        mCacheFolder = cacheFolder;
        mMaxSize = maxSize;
        mCacheTtl = cacheTtl;
        mMaxSizeLowerBound = (long) (mMaxSize * .8);

        //noinspection ResultOfMethodCallIgnored
        mCacheFolder.mkdirs();

        mReadExecutorService = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), FILUtils.threadFactory("ImageCacheRead", true));

        mScanExecutorService = new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), FILUtils.threadFactory("ImageCacheScan", true));
    }

    @Override
    public File getCacheFile(String uri, ImageLoadSpec spec) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }
        String name = FILUtils.format("{}_{}_{}_{}",
                Integer.toHexString(uri.substring(0, lastSlash).hashCode()),
                Integer.toHexString(uri.substring(lastSlash + 1).hashCode()),
                uri.substring(Math.max(lastSlash + 1, uri.length() - 10)),
                spec.getKey());
        return new File(FILUtils.pathCombine(mCacheFolder.getAbsolutePath(), name));
    }

    @Override
    public void getAsync(final ImageRequest imageRequest,
                         final ImageLoadSpec altSpec,
                         final Decoder decoder,
                         final MemoryPool memoryPool,
                         final Callback callback) {

        File altFile = null;
        boolean exists = imageRequest.getFile().exists();
        if (!exists && altSpec != null) {
            // if primary spec file doesn't exist in cache but alternative does, load it
            altFile = getCacheFile(imageRequest.getUri(), altSpec);
        }

        if (exists || (altFile != null && altFile.exists())) {
            // use the primary or the alternative file and spec to decode the image
            final File file = exists ? imageRequest.getFile() : altFile;
            final ImageLoadSpec spec = exists ? imageRequest.getSpec() : altSpec;
            mReadExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    loadImageFromCache(imageRequest, file, spec, decoder, memoryPool, callback);
                }
            });
        } else {
            callback.loadImageDiskCacheCallback(imageRequest, false);
        }
    }

    @Override
    public void imageAdded(long size) {
        mCurrentCacheSize += size;
        if (mLastCacheScanTime < 1 || mLastCacheScanTime + SCAN_INTERVAL < System.currentTimeMillis() || mCurrentCacheSize > mMaxSize) {
            mScanExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    scanCache();
                }
            });
        }
    }

    @Override
    public void clear() {
        mReadExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                clearCache();
            }
        });
    }

    /**
     * Populate the given string builder with report on cache status.
     */
    public void report(StringBuilder sb) {
        sb.append("Disk Cache: ");
        if (mLastCacheScanTime > 0) {
            sb.append("Size: ").append(NumberFormat.getInstance().format(mCurrentCacheSize / 1024)).append("K\n");
            sb.append("Since Last Scan: ").append(NumberFormat.getInstance().format((System.currentTimeMillis() - mLastCacheScanTime) / 1000 / 60)).append(" Minutes\n");
        } else {
            sb.append("Not scanned");
        }
    }

    @Override
    public String toString() {
        return "ImageDiskCache{" +
                "mLastCacheScanTime=" + mLastCacheScanTime +
                ", mCurrentCacheSize=" + mCurrentCacheSize +
                '}';
    }

    //region: Private methods

    /**
     * Load the given cached image file into reusable bitmap, post result on given callback.<br>
     * This method is executed on a dedicated separate thread.
     */
    protected void loadImageFromCache(ImageRequest imageRequest,
                                      File file,
                                      ImageLoadSpec spec,
                                      Decoder decoder,
                                      MemoryPool memoryPool,
                                      Callback callback) {

        final boolean canceled = !imageRequest.isValid();
        if (!canceled) {
            //noinspection ResultOfMethodCallIgnored
            file.setLastModified(System.currentTimeMillis());
            decoder.decode(memoryPool, imageRequest, file, spec);
        }
        callback.loadImageDiskCacheCallback(imageRequest, canceled);
    }

    /**
     * Iterate over all the cached image files to delete LRU images.
     */
    protected void scanCache() {
        try {
            if (mLastCacheScanTime < 1) {
                loadStats();
            }
            if (mLastCacheScanTime + SCAN_INTERVAL < System.currentTimeMillis() || mCurrentCacheSize > mMaxSize) {
                long startTime = System.currentTimeMillis();
                try {
                    long totalSize = 0;
                    long totalSizeFull = 0;
                    int deleteByTTL = 0;
                    int deleteByMaxSize = 0;

                    // iterate over all cached files, delete stale and calculate current cache size
                    File[] allImages = mCacheFolder.listFiles();
                    for (int i = 0; i < allImages.length; i++) {
                        long fileSize = allImages[i].length();
                        totalSizeFull += fileSize;
                        if (allImages[i].lastModified() + mCacheTtl < System.currentTimeMillis()) {
                            if (allImages[i].delete()) {
                                deleteByTTL++;
                                allImages[i] = null;
                            }
                        } else {
                            totalSize += fileSize;
                        }
                    }

                    // if cache max size reached, need to delete LRU images
                    if (totalSize > mMaxSize) {

                        // sort all cached files by last access date
                        Arrays.sort(allImages, new Comparator<File>() {
                            @Override
                            public int compare(File lhs, File rhs) {
                                long l = lhs != null ? lhs.lastModified() : 0;
                                long r = rhs != null ? rhs.lastModified() : 0;
                                return l < r ? -1 : (l == r ? 0 : 1);
                            }
                        });

                        // delete cached images until cache size is reduced to 90% of max
                        for (int i = 0; i < allImages.length && totalSize > mMaxSizeLowerBound; i++) {
                            if (allImages[i] != null) {
                                long length = allImages[i].length();
                                if (allImages[i].delete()) {
                                    deleteByMaxSize++;
                                    totalSize -= length;
                                    allImages[i] = null;
                                }
                            }
                        }
                    }

                    mLastCacheScanTime = System.currentTimeMillis();
                    mCurrentCacheSize = totalSize;

                    saveStats();

                    FILLogger.info("Image disk cache scan complete [Before: {} / {}K] [After: {} / {}K] [Delete TTL: {}] [Delete size: {}]",
                            allImages.length, totalSizeFull / 1024, allImages.length - deleteByTTL - deleteByMaxSize, totalSize / 1024, deleteByTTL, deleteByMaxSize);
                } finally {
                    FILLogger.info("ImageCacheScan [{}]", System.currentTimeMillis() - startTime);
                }
            }
        } catch (Exception e) {
            FILLogger.critical("Error in image disk cache scan", e);
        }
    }

    /**
     * Delete all cached images and update the cache scan data.
     */
    protected void clearCache() {
        String[] list = mCacheFolder.list();
        for (String filePath : list) {
            try {
                File file = new File(FILUtils.pathCombine(mCacheFolder.getAbsolutePath(), filePath));
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            } catch (Exception e) {
                FILLogger.warn("Failed to delete disk cached image", e);
            }
        }
        mCurrentCacheSize = 0;
        mLastCacheScanTime = System.currentTimeMillis();
        saveStats();
    }

    /**
     * Load stats used for cache operation: last cache scan, total cache size.<br>
     * The states are persisted so cache scan won't happen unless really required.
     */
    protected void loadStats() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLastCacheScanTime = prefs.getLong(STATS_LAST_SCAN, 0);
        mCurrentCacheSize += prefs.getLong(STATS_CACHE_SIZE, 0);
    }

    /**
     * Save stats used for cache operation: last cache scan, total cache size.<br>
     * The states are persisted so cache scan won't happen unless really required.
     */
    protected void saveStats() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(STATS_LAST_SCAN, mLastCacheScanTime);
        editor.putLong(STATS_CACHE_SIZE, mCurrentCacheSize);
        editor.apply();
    }
    //endregion
}

