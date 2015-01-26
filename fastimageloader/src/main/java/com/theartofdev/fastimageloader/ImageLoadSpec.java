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

import com.theartofdev.fastimageloader.impl.CommonUtils;

/**
 * The image loading spec data.
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
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    private final Bitmap.Config mPixelConfig;
    //endregion

    /**
     * Init image loading spec with default max-density of 1.5 and pixel config of RGB_565 (2 bytes, no alpha channel).
     *
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     */
    public ImageLoadSpec(int width, int height) {
        this(width, height, 1.5f, Bitmap.Config.RGB_565);
    }

    /**
     * Init image loading spec with default pixel config of RGB_565 (2 bytes, no alpha channel).
     *
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param maxDensity the max pixel per inch density to load the image in
     */
    public ImageLoadSpec(int width, int height, float maxDensity) {
        this(width, height, maxDensity, Bitmap.Config.RGB_565);
    }

    /**
     * Init image loading spec.
     *
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param maxDensity the max pixel per inch density to load the image in
     * @param pixelConfig the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    public ImageLoadSpec(int width, int height, float maxDensity, Bitmap.Config pixelConfig) {
        float densityAdj = CommonUtils.density > maxDensity ? maxDensity / CommonUtils.density : 1f;
        mWidth = (int) (width * densityAdj);
        mHeight = (int) (height * densityAdj);
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
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    public Bitmap.Config getPixelConfig() {
        return mPixelConfig;
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
                ", mPixelConfig=" + mPixelConfig +
                '}';
    }
}