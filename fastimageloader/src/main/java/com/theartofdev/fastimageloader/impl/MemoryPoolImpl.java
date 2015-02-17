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

import android.content.ComponentCallbacks2;

import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.MemoryPool;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.impl.util.FILLogger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Memory cache for image handler.<br>
 * Holds the images loaded two caches: large for images larger than 300px (width+height)
 * and small for smaller.<br>
 * Caches may be evicted when memory pressure is detected.
 */
public class MemoryPoolImpl implements MemoryPool {

    //region: Fields and Consts

    /**
     * Cache and pool of reusable bitmaps.
     */
    private final Map<ImageLoadSpec, LinkedList<ReusableBitmap>> mBitmapsCachePool = new LinkedHashMap<>();

    /**
     * stats on the number of cache hit
     */
    private int mCacheHit;

    /**
     * stats on the number of cache miss
     */
    private int mCacheMiss;

    /**
     * stats on the number of bitmaps used from recycled instance
     */
    private int mReUsed;

    /**
     * stats on the number of recycled images returned after failed to use
     */
    private int mReturned;

    /**
     * stats on the number of recycled images thrown because of limit
     */
    private int mThrown;
    //endregion

    @Override
    public ReusableBitmap get(String url, ImageLoadSpec spec, ImageLoadSpec altSpec) {
        synchronized (mBitmapsCachePool) {
            ReusableBitmap bitmap = getUnusedBitmapBySpec(url, spec);
            if (bitmap == null && altSpec != null) {
                bitmap = getUnusedBitmapBySpec(url, altSpec);
            }
            if (bitmap != null) {
                mCacheHit++;
            } else {
                mCacheMiss++;
            }
            return bitmap;
        }
    }

    @Override
    public void set(ReusableBitmap bitmap) {
        synchronized (mBitmapsCachePool) {
            if (bitmap != null) {
                LinkedList<ReusableBitmap> list = mBitmapsCachePool.get(bitmap.getSpec());
                if (list == null) {
                    list = new LinkedList<>();
                    mBitmapsCachePool.put(bitmap.getSpec(), list);
                }
                list.addFirst(bitmap);
            }
        }
    }

    @Override
    public ReusableBitmap getUnused(ImageLoadSpec spec) {
        synchronized (mBitmapsCachePool) {
            LinkedList<ReusableBitmap> list = mBitmapsCachePool.get(spec);
            if (list != null) {
                Iterator<ReusableBitmap> iter = list.iterator();
                if (spec.isSizeBounded()) {
                    while (iter.hasNext()) {
                        ReusableBitmap bitmap = iter.next();
                        if (!bitmap.isInUse()) {
                            iter.remove();
                            mReUsed++;
                            bitmap.setInLoadUse(true);
                            return bitmap;
                        }
                    }
                } else {
                    // don't keep unbounded bitmaps (only 2 to be nice on quick return)
                    int grace = 2;
                    while (iter.hasNext()) {
                        ReusableBitmap bitmap = iter.next();
                        if (!bitmap.isInUse() && grace-- < 1) {
                            mThrown++;
                            iter.remove();
                            bitmap.close();
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void returnUnused(ReusableBitmap bitmap) {
        synchronized (mBitmapsCachePool) {
            mReUsed--;
            mReturned++;
            bitmap.setInLoadUse(false);
            LinkedList<ReusableBitmap> list = mBitmapsCachePool.get(bitmap.getSpec());
            if (list != null) {
                list.addFirst(bitmap);
            } else {
                mThrown++;
                bitmap.close();
            }
        }
    }

    @Override
    public void clear() {
        releaseUnUsedBitmaps(0);
    }

    /**
     * Populate the given string builder with report on cache status.
     */
    public void report(StringBuilder sb) {
        sb.append("Memory Cache: ").append(mCacheHit + mCacheMiss).append('\n');
        sb.append("Cache Hit: ").append(mCacheHit).append('\n');
        sb.append("Cache Miss: ").append(mCacheMiss).append('\n');
        sb.append("ReUsed: ").append(mReUsed).append('\n');
        sb.append("Returned: ").append(mReturned).append('\n');
        sb.append("Thrown: ").append(mThrown).append('\n');

        //        sb.append("Small: ")
        //                .append(mSmallCache.items()).append('/')
        //                .append(mSmallCache.getMaxItems()).append(", (")
        //                .append(NumberFormat.getInstance().format(mSmallCache.size() / 1024)).append("K/")
        //                .append(NumberFormat.getInstance().format(mSmallCache.maxSize() / 1024)).append("K)")
        //                .append('\n');

        //        sb.append("Bitmap Recycler: ").append('\n');
        //        sb.append("Added: ").append(mAdded).append('\n');
        //
        //        sb.append("Returned: ").append(mReturned).append('\n');
        //        sb.append("Thrown: ").append(mThrown).append('\n');
        //        for (Map.Entry<ImageLoadSpec, LinkedList<RecycleBitmap>> entry : mReusableBitmaps.entrySet()) {
        //            long size = 0;
        //            for (RecycleBitmap bitmap : entry.getValue()) {
        //                size += bitmap.getBitmap().getByteCount();
        //            }
        //            sb.append(entry.getKey()).append(": ")
        //                    .append(entry.getValue().size()).append(", ")
        //                    .append(NumberFormat.getInstance().format(size / 1024)).append("K\n");
        //        }

    }

    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                releaseUnUsedBitmaps(3);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                releaseUnUsedBitmaps(1);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                releaseUnUsedBitmaps(0);
                break;
        }
    }

    @Override
    public String toString() {
        return "ImageMemoryCache{" +
                "mCacheHit=" + mCacheHit +
                ", mCacheMiss=" + mCacheMiss +
                '}';
    }

    //region: Private methods

    /**
     * Get bitmap from cache that is of the given spec and has image loaded of the given URI.
     */
    private ReusableBitmap getUnusedBitmapBySpec(String uri, ImageLoadSpec spec) {
        LinkedList<ReusableBitmap> list = mBitmapsCachePool.get(spec);
        if (list != null) {
            Iterator<ReusableBitmap> iter = list.iterator();
            while (iter.hasNext()) {
                ReusableBitmap bitmap = iter.next();
                if (uri.equals(bitmap.getUri())) {
                    iter.remove();
                    list.addLast(bitmap);
                    return bitmap;
                }
            }
        }
        return null;
    }

    /**
     * Release unused bitmaps that are currently in the pool.
     *
     * @param graceLevel the number of unused bitmaps NOT to release
     */
    private void releaseUnUsedBitmaps(int graceLevel) {
        FILLogger.debug("trim image cache to size [{}]", graceLevel);
        for (LinkedList<ReusableBitmap> list : mBitmapsCachePool.values()) {
            int listGrace = graceLevel;
            Iterator<ReusableBitmap> iter = list.iterator();
            while (iter.hasNext()) {
                ReusableBitmap bitmap = iter.next();
                if (!bitmap.isInUse() && listGrace-- < 1) {
                    mThrown++;
                    iter.remove();
                    bitmap.close();
                }
            }
        }
    }
    //endregion
}

