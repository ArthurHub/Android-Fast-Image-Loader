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

/**
 * TODO:a add doc
 */
public interface MemoryPool {

    /**
     * Retrieve an image for the specified {@code url} and {@code spec}.<br>
     * If not found for primary spec, use the alternative.
     */
    ReusableBitmap get(String url, ImageLoadSpec spec, ImageLoadSpec altSpec);

    /**
     * Store an image in the cache for the specified {@code key}.
     */
    void set(ReusableBitmap bitmap);

    /**
     * TODO:a. doc
     */
    ReusableBitmap getUnused(ImageLoadSpec spec);

    /**
     * TODO:a. doc
     */
    void returnUnused(ReusableBitmap bitmap);

    /**
     * Clears the cache/pool.
     */
    void clear();

    /**
     * Handle trim memory event to release image caches on memory pressure.
     */
    void onTrimMemory(int level);
}
