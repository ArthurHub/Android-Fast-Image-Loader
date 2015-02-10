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

import com.theartofdev.fastimageloader.impl.ImageRequest;

import java.io.File;

/**
 * TODO:a add doc
 */
public interface Decoder {

    /**
     * Load image from disk file on the current thread and set it in the image request object.
     */
    void decode(MemoryPool memoryPool, ImageRequest imageRequest, File file, ImageLoadSpec spec);
}
