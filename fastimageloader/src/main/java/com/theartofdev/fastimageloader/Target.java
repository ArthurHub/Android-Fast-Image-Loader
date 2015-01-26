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
 * Represents an arbitrary listener for image loading.<br/>
 * Objects implementing this class <strong>must</strong> have a working implementation of
 * {@link Object#equals(Object)} and {@link Object#hashCode()} for proper storage internally.
 * Instances of this interface will also be compared to determine if view recycling is occurring.
 * It is recommended that you add this interface directly on to a custom view type when using in an
 * adapter to ensure correct recycling behavior.
 */
public interface Target {

    /**
     * The URL source of the image
     */
    String getUrl();

    /**
     * the spec to load the image by
     */
    ImageLoadSpec getSpec();

    /**
     * Callback when an image has been successfully loaded.<br/>
     * <strong>Note:</strong> You must not recycle the bitmap.
     */
    public void onBitmapLoaded(RecycleBitmap bitmap, LoadedFrom from);

    /**
     * Callback indicating the image could not be successfully loaded.<br/>
     */
    public void onBitmapFailed();
}

