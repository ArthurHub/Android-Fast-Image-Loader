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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Memory cache for image handler.<br/>
 * Holds the images loaded two caches: large for images larger than 300px (width+height)
 * and small for smaller.<br/>
 * Caches may be evicted when memory pressure is detected.
 */
final class ImageMemoryCache {

    //region: Fields and Consts

    /**
     *
     */
    private final Map<ImageLoadSpec, LinkedList<RecycleBitmapImpl>> mBitmapsCachePool = new LinkedHashMap<>();

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

    /**
     * Retrieve an image for the specified {@code url} and {@code spec}.
     */
    public RecycleBitmapImpl get(String url, ImageLoadSpec spec) {
        synchronized (mBitmapsCachePool) {
            LinkedList<RecycleBitmapImpl> list = mBitmapsCachePool.get(spec);
            if (list != null) {
                Iterator<RecycleBitmapImpl> iter = list.iterator();
                while (iter.hasNext()) {
                    RecycleBitmapImpl bitmap = iter.next();
                    if (url.equals(bitmap.getUrl())) {
                        iter.remove();
                        list.addLast(bitmap);
                        mCacheHit++;
                        return bitmap;
                    }
                }
            }
            mCacheMiss++;
            return null;
        }
    }

    /**
     * Store an image in the cache for the specified {@code key}.
     */
    public void set(RecycleBitmapImpl bitmap) {
        synchronized (mBitmapsCachePool) {
            if (bitmap != null) {
                LinkedList<RecycleBitmapImpl> list = mBitmapsCachePool.get(bitmap.getSpec());
                if (list == null) {
                    list = new LinkedList<>();
                    mBitmapsCachePool.put(bitmap.getSpec(), list);
                }
                list.addFirst(bitmap);
            }
        }
    }

    /**
     * TODO:a. doc
     */
    public RecycleBitmapImpl getUnused(ImageLoadSpec spec) {
        synchronized (mBitmapsCachePool) {
            LinkedList<RecycleBitmapImpl> list = mBitmapsCachePool.get(spec);
            if (list != null) {
                Iterator<RecycleBitmapImpl> iter = list.iterator();
                while (iter.hasNext()) {
                    RecycleBitmapImpl bitmap = iter.next();
                    if (!bitmap.isInUse()) {
                        iter.remove();
                        mReUsed++;
                        bitmap.setInLoadUse(true);
                        return bitmap;
                    }
                }
            }
            return null;
        }
    }

    /**
     * TODO:a. doc
     */
    public void returnUnused(RecycleBitmapImpl bitmap) {
        synchronized (mBitmapsCachePool) {
            mReUsed--;
            mReturned++;
            bitmap.setInLoadUse(false);
            LinkedList<RecycleBitmapImpl> list = mBitmapsCachePool.get(bitmap.getSpec());
            if (list != null) {
                list.addFirst(bitmap);
            } else {
                mThrown++;
                bitmap.close();
            }
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        // mLargeCache.evictAll();
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
    public String toString() {
        return "ImageMemoryCache{" +
                "mCacheHit=" + mCacheHit +
                ", mCacheMiss=" + mCacheMiss +
                '}';
    }

    /**
     * Handle trim memory event to release image caches on memory pressure.
     */
    @SuppressWarnings("UnusedDeclaration")
    //    public void onEvent(TrimMemoryEvent event) {
    //        switch (event.getLevel()) {
    //            case UI_HIDDEN:
    //                //mLargeCache.trimToSize(.7f, false);
    //                break;
    //            case BACKGROUND:
    //                //mLargeCache.trimToSize(.5f, false);
    //                break;
    //            case MODERATE:
    //            case RUNNING_MODERATE:
    //                //mLargeCache.trimToSize(.4f, false);
    //                break;
    //            case RUNNING_LOW:
    //                //mLargeCache.trimToSize(.2f, false);
    //                break;
    //            case RUNNING_CRITICAL:
    //            case COMPLETE:
    //                //mLargeCache.trimToSize(0, false);
    //                break;
    //        }
    //    }

    //region: Inner class: RecycleCache

    /**
     * A memory cache which uses a least-recently used eviction policy.
     */
    private final class LruCache {

        //region: Fields and Consts

        private final String mName;

        private final LinkedList<String> mList;

        private final Map<String, RecycleBitmapImpl> mMap;

        private final int mMaxItems;

        private final int mMaxSize;

        private int mSize;
        //endregion

        /**
         * Create a cache with a given maximum items.
         */
        public LruCache(String name, int maxItems, int maxSize) {

            mName = name;
            mMaxItems = maxItems;
            mMaxSize = maxSize;
            mList = new LinkedList<>();
            mMap = new LinkedHashMap<>(0, 0.75f);
        }

        /**
         * Get cached item by key if exists.
         */
        public RecycleBitmapImpl get(String key) {
            RecycleBitmapImpl value = mMap.get(key);
            if (value != null) {
                mList.remove(key);
                mList.addFirst(key);
            }
            return value;
        }

        /**
         * Add given item to cache, evict items if cache size is reached.
         */
        public void put(String key, RecycleBitmapImpl bitmap) {
            if (!mMap.containsKey(key)) {
                mList.addFirst(key);
                mMap.put(key, bitmap);
                mSize += bitmap.getBitmap().getByteCount();
                if (mList.size() > mMaxItems || mSize > mMaxSize)
                    trimToSize(.9f, true);
            }
        }

        /**
         * Trim the items kept in the cache to the given ration from max count/size.
         */
        private void trimToSize(float ratio, boolean recycle) {
            int maxItems = (int) (mMaxItems * ratio);
            int maxSize = (int) (mMaxSize * ratio);
            if (mList.size() > maxItems || mSize > maxSize) {
                trimToSize(maxItems, maxSize, recycle);
            }
        }

        /**
         * Trim the items kept in the cache to the given count and size.
         */
        private void trimToSize(int maxItems, int maxSize, boolean recycle) {
            Logger.debug("trim image cache to size [{}] [{}] [{}] [{}]", mName, maxItems, maxSize, recycle);
            for (Iterator<String> iterator = mList.descendingIterator(); iterator.hasNext() && (mList.size() > maxItems || mSize > maxSize); ) {
                String toEvict = iterator.next();
                RecycleBitmapImpl toEvictBitmap = mMap.get(toEvict);
                if (toEvictBitmap != null) {
                    if (toEvictBitmap.canBeRecycled()) {
                        iterator.remove();
                        mSize -= toEvictBitmap.getBitmap().getByteCount();
                        mMap.remove(toEvict);

                        //                        if (recycle)
                        //                            mBitmapRecycler.add(toEvictBitmap);
                        //                        else
                        //                            toEvictBitmap.close();
                    }
                } else {
                    iterator.remove();
                }
            }
        }

        /**
         * Clear the cache.
         */
        public void evictAll() {
            trimToSize(-1, true);
        }

        /**
         * Returns the number of cached items in this cache.
         */
        public int items() {
            return mList.size();
        }

        /**
         * Returns the sum of the sizes of the entries in this cache.
         */
        public int size() {
            return mSize;
        }

        /**
         * Returns the maximum number of items in this cache.
         */
        public int getMaxItems() {
            return mMaxItems;
        }

        /**
         * Returns the maximum sum of the sizes of the items in this cache.
         */
        public int maxSize() {
            return mMaxSize;
        }
    }
    //endregion
}

