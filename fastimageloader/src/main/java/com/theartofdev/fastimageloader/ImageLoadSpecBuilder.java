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
public final class ImageLoadSpecBuilder {

    //region: Fields and Consts

    /**
     * the width of the image in pixels
     */
    private int mWidth;

    /**
     * the height of the image in pixels
     */
    private int mHeight;

    /**
     * the max pixel per inch density to load the image in
     */
    private float mMaxDensity = 1.5f;

    /**
     * The format of the image.
     */
    private ImageLoadSpec.Format mFormat = ImageLoadSpec.Format.JPEG;

    /**
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    private Bitmap.Config mPixelConfig = Bitmap.Config.ARGB_8888;
    //endregion

    /**
     * The format of the image to download.
     */
    public void setFormat(ImageLoadSpec.Format format) {
        mFormat = format;
    }

    /**
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    public ImageLoadSpecBuilder setPixelConfig(Bitmap.Config pixelConfig) {
        mPixelConfig = pixelConfig;
        return this;
    }

    /**
     * the width and height of the image in pixels to the same value (square).
     */
    public ImageLoadSpecBuilder setDimension(int size) {
        mWidth = size;
        mHeight = size;
        return this;
    }

    /**
     * the width and height of the image in pixels.
     */
    public ImageLoadSpecBuilder setDimension(int width, int height) {
        mWidth = width;
        mHeight = height;
        return this;
    }

    /**
     * the max pixel per inch density to load the image in
     */
    public ImageLoadSpecBuilder setMaxDensity(float maxDensity) {
        if (maxDensity <= 0.5)
            throw new IllegalArgumentException("max density must be > .5");
        mMaxDensity = maxDensity;
        return this;
    }

    /**
     * Create spec by set parameters.
     */
    public ImageLoadSpec build() {
        if (mWidth < 1)
            throw new IllegalArgumentException("width must be set");
        if (mHeight < 1)
            throw new IllegalArgumentException("height must be set");
        float densityAdj = Utils.density > mMaxDensity ? mMaxDensity / Utils.density : 1f;
        return new ImageLoadSpec((int) (mWidth * densityAdj), (int) (mHeight * densityAdj), mFormat, mPixelConfig);
    }
}