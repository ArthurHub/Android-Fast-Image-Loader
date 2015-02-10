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

import com.theartofdev.fastimageloader.impl.DiskCacheImpl;
import com.theartofdev.fastimageloader.impl.ImageRequest;

/**
 * TODO:a add doc
 */
public interface DiskCache {

    /**
     * Get disk cached image for the given request.<br/>
     * If the image is NOT in the cache the callback will be executed immediately.<br/>
     * If the image is in cache an async operation will load the image from disk and then execute the callback.
     *
     * @param callback The callback to execute on async requests to the cache
     */
    void getAsync(ImageRequest imageRequest, ImageLoadSpec altSpec, DiskCacheImpl.Callback callback);

    void imageAdded(long size);

    void clear();
}
