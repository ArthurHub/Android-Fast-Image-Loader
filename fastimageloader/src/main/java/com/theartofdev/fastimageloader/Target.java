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
 * Client will receive the raw instance of {@link com.theartofdev.fastimageloader.ReusableBitmap} and will
 * be responsible for calling {@link com.theartofdev.fastimageloader.ReusableBitmap#incrementInUse()} and
 * {@link com.theartofdev.fastimageloader.ReusableBitmap#decrementInUse()} correctly.
 * <p/>
 * Instances of this interface will used to determine the image to load by {@link #getUri()} and the
 * specification to load the image by {@link #getSpecKey()}.<br/>
 * Those methods will also be used to cancel image load request if the returned value of
 * {@link #getUri()} has been changed or nullified.
 * <p/>
 * <b>Note: </b> Prefer using {@link com.theartofdev.fastimageloader.target.TargetImageViewHandlerBase}, it
 * implements most of the required functionality.
 */
public interface Target {

    /**
     * The URI source of the image
     */
    String getUri();

    /**
     * the spec to load the image by
     */
    String getSpecKey();

    /**
     * Callback when an image has been successfully loaded.<br/>
     * <strong>Note:</strong> You must not recycle the bitmap.
     */
    void onBitmapLoaded(ReusableBitmap bitmap, LoadedFrom from);

    /**
     * Callback indicating the image could not be successfully loaded.<br/>
     */
    void onBitmapFailed();
}

