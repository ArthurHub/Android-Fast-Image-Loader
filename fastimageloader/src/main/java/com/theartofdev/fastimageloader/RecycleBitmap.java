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
 * TODO:a add doc
 */
public interface RecycleBitmap {

    /**
     * The actual bitmap
     */
    Bitmap getBitmap();

    /**
     * Is the URL of image loaded in the bitmap matches the given URL.
     */
    boolean isMatchingUrl(String url);

    /**
     * Is the bitmap is currently in use
     */
    void setInUse(boolean inUse);
}
