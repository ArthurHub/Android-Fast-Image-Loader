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

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Builder for creating {@link com.theartofdev.fastimageloader.ImageLoadSpec} instances.
 * <p/>
 * Defaults:<br/>
 * Format - JPEG<br/>
 * Max Density - 1.5<br/>
 * Pixel Config - ARGB_8888<br/>
 */
public final class ImageLoadSpecBuilder {

    //region: Fields and Consts

    /**
     * the unique key of the spec used for identification and debug
     */
    private String mKey;

    /**
     * The application object
     */
    private Application mApplication;

    /**
     * the width of the image in pixels
     */
    private int mWidth = -1;

    /**
     * the height of the image in pixels
     */
    private int mHeight = -1;

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

    /**
     * The URI enhancer to use for this spec image loading
     */
    private UriEnhancer mUriEnhancer;
    //endregion

    /**
     * @param key the unique key of the spec used for identification and debug
     * @param application The application object
     * @param uriEnhancer default URI enhancer to use for this spec image loading
     */
    ImageLoadSpecBuilder(String key, Application application, UriEnhancer uriEnhancer) {
        Utils.notNullOrEmpty(key, "key");
        Utils.notNull(application, "application");
        Utils.notNull(uriEnhancer, "uriEnhancer");

        mKey = key;
        mApplication = application;
        mUriEnhancer = uriEnhancer;
    }

    /**
     * Get the display size of the device.
     */
    public Point getDisplaySize() {
        return Utils.displaySize;
    }

    /**
     * The format of the image to download.
     */
    public ImageLoadSpecBuilder setFormat(ImageLoadSpec.Format format) {
        mFormat = format;
        return this;
    }

    /**
     * the pixel configuration to load the image in (4 bytes per image pixel, 2 bytes, etc.)
     */
    public ImageLoadSpecBuilder setPixelConfig(Bitmap.Config pixelConfig) {
        mPixelConfig = pixelConfig;
        return this;
    }

    /**
     * the width and height of the image in pixels to the size of the screen.
     */
    public ImageLoadSpecBuilder setDimensionByDisplay() {
        mWidth = Utils.displaySize.x;
        mHeight = Utils.displaySize.y;
        return this;
    }

    /**
     * the width and height of the image to unbound, will be the size of the downloaded image.
     */
    public ImageLoadSpecBuilder setUnboundDimension() {
        mWidth = 0;
        mHeight = 0;
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
     * the width of the image in pixels.
     */
    public ImageLoadSpecBuilder setWidth(int width) {
        mWidth = width;
        return this;
    }

    /**
     * the height of the image in pixels.
     */
    public ImageLoadSpecBuilder setHeight(int height) {
        mHeight = height;
        return this;
    }

    /**
     * the width and height of the image in pixels to the same value (square).
     */
    public ImageLoadSpecBuilder setDimensionByResource(int resId) {
        mWidth = mHeight = mApplication.getResources().getDimensionPixelSize(resId);
        return this;
    }

    /**
     * the width and height of the image in pixels.
     */
    public ImageLoadSpecBuilder setDimensionByResource(int widthResId, int heightResId) {
        mWidth = mApplication.getResources().getDimensionPixelSize(widthResId);
        mHeight = mApplication.getResources().getDimensionPixelSize(heightResId);
        return this;
    }

    /**
     * the width of the image by reading dimension resource by the given key.
     */
    public ImageLoadSpecBuilder setWidthByResource(int resId) {
        mWidth = mApplication.getResources().getDimensionPixelSize(resId);
        return this;
    }

    /**
     * the height of the image by reading dimension resource by the given key.
     */
    public ImageLoadSpecBuilder setHeightByResource(int resId) {
        mHeight = mApplication.getResources().getDimensionPixelSize(resId);
        return this;
    }

    /**
     * the max pixel per inch density to load the image in
     *
     * @throws IllegalArgumentException if value if < 0.5
     */
    public ImageLoadSpecBuilder setMaxDensity(float maxDensity) {
        if (maxDensity <= 0.5)
            throw new IllegalArgumentException("max density must be > .5");
        mMaxDensity = maxDensity;
        return this;
    }

    /**
     * The URI enhancer to use for this spec image loading
     */
    public ImageLoadSpecBuilder setUriEnhancer(UriEnhancer uriEnhancer) {
        mUriEnhancer = uriEnhancer;
        return this;
    }

    /**
     * Create spec by set parameters.
     *
     * @throws IllegalArgumentException width or height not set correctly.
     */
    public ImageLoadSpec build() {
        if (mWidth < 0 || mHeight < 0)
            throw new IllegalArgumentException("width and height must be set");
        if ((mWidth == 0 && mHeight > 0) || (mHeight == 0 && mWidth > 0))
            throw new IllegalArgumentException("width and height must be either unbound or both positive");

        float densityAdj = Utils.density > mMaxDensity ? mMaxDensity / Utils.density : 1f;

        ImageLoadSpec spec = new ImageLoadSpec(mKey, (int) (mWidth * densityAdj), (int) (mHeight * densityAdj), mFormat, mPixelConfig, mUriEnhancer);

        FastImageLoader.addSpec(spec);

        return spec;
    }
}