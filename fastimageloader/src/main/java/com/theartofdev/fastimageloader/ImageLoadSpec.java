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
 * The image loading spec data.<br>
 * <br><br>
 * equals and hashCode are used to match image request that can reuse bitmaps by spec, so it
 * contains only the config that define unique reusable bitmap.
 */
public final class ImageLoadSpec {

    //region: Fields and Consts

    /**
     * the unique key of the spec used for identification and debug
     */
    private final String mKey;

    /**
     * the width of the image in pixels
     */
    private final int mWidth;

    /**
     * the height of the image in pixels
     */
    private final int mHeight;

    /**
     * The format of the image.
     */
    private final Format mFormat;

    /**
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    private final Bitmap.Config mPixelConfig;

    /**
     * The URI enhancer to use for this spec image loading
     */
    private final ImageServiceAdapter mImageServiceAdapter;
    //endregion

    /**
     * Init image loading spec.
     *
     * @param key the unique key of the spec used for identification and debug
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param format The format of the image.
     * @param pixelConfig the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     * @param imageServiceAdapter The URI enhancer to use for this spec image loading
     */
    ImageLoadSpec(String key, int width, int height, Format format, Bitmap.Config pixelConfig, ImageServiceAdapter imageServiceAdapter) {
        mKey = key;
        mWidth = width;
        mHeight = height;
        mFormat = format;
        mPixelConfig = pixelConfig;
        mImageServiceAdapter = imageServiceAdapter;
    }

    /**
     * the unique key of the spec used for identification and debug
     */
    public String getKey() {
        return mKey;
    }

    /**
     * the width of the image in pixels
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * the height of the image in pixels
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * The format of the image.
     */
    public Format getFormat() {
        return mFormat;
    }

    /**
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    public Bitmap.Config getPixelConfig() {
        return mPixelConfig;
    }

    /**
     * The URI enhancer to use for this spec image loading
     */
    public ImageServiceAdapter getImageServiceAdapter() {
        return mImageServiceAdapter;
    }

    /**
     * Is the spec define specific width and height for the image.
     */
    public boolean isSizeBounded() {
        return mWidth > 0 && mHeight > 0;
    }

    @Override
    public String toString() {
        return "ImageLoadSpec{" +
                "mKey='" + mKey + '\'' +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mFormat=" + mFormat +
                ", mPixelConfig=" + mPixelConfig +
                ", mImageServiceAdapter=" + mImageServiceAdapter +
                '}';
    }

    //region: Inner class: Format

    /**
     * The format of the image.
     */
    public static enum Format {
        UNCHANGE,
        JPEG,
        PNG,
        WEBP,
    }
    //endregion
}