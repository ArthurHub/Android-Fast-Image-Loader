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
 * The image loading spec data.<br/>
 * <p/>
 * equals and hashCode are used to match image request that can reuse bitmaps by spec, so it
 * contains only the config that define unique reusable bitmap.
 */
public final class ImageLoadSpec {

    //region: Fields and Consts

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
    //endregion

    /**
     * Init image loading spec.
     *
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param format The format of the image.
     * @param pixelConfig the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    ImageLoadSpec(int width, int height, Format format, Bitmap.Config pixelConfig) {
        mWidth = width;
        mHeight = height;
        mFormat = format;
        mPixelConfig = pixelConfig;
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
     * Is the spec define specific width and height for the image.
     */
    public boolean isSizeBounded() {
        return mWidth > 0 && mHeight > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageLoadSpec that = (ImageLoadSpec) o;

        if (mHeight != that.mHeight) {
            return false;
        }
        if (mWidth != that.mWidth) {
            return false;
        }
        return mPixelConfig == that.mPixelConfig;
    }

    @Override
    public int hashCode() {
        int result = mWidth;
        result = 31 * result + mHeight;
        result = 31 * result + mPixelConfig.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ImageLoadSpec{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mFormat=" + mFormat +
                ", mPixelConfig=" + mPixelConfig +
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