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
 * Client will receive the raw instance of {@link RecycleBitmap} and will
 * be responsible for setting its {@link RecycleBitmap#setInUse(boolean)} state.
 * <p/>
 * Instances of this interface will used to determine the image to load by {@link #getUrl()} and the
 * specification to load the image by {@link #getSpec()}.<br/>
 * Those methods will also be used to cancel image load request it the returned value have been changed or nullified.
 * <p/>
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

