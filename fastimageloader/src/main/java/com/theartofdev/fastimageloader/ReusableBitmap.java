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

import android.graphics.Bitmap;

/**
 * Define Bitmap that can reuse the allocated memory to load a new image into the bitmap.
 */
public interface ReusableBitmap {

    /**
     * The actual bitmap
     */
    Bitmap getBitmap();

    /**
     * the URL of the loaded image in the bitmap.
     */
    String getUrl();

    /**
     * the spec the loaded image was loaded by
     */
    ImageLoadSpec getSpec();

    /**
     * Is the bitmap is currently in use
     */
    boolean isInUse();

    /**
     * Increment the bitmap in use count by 1.<br/>
     * Critical to call this method correctly.
     */
    void incrementInUse();

    /**
     * Decrement the bitmap in use count by 1.<br/>
     * Critical to call this method correctly.
     */
    void decrementInUse();
}