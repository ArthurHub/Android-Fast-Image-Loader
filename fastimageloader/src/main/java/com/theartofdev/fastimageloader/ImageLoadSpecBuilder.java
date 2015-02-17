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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * Builder for creating {@link com.theartofdev.fastimageloader.ImageLoadSpec} instances.
 * <br><br>
 * Defaults:<br>
 * Format - JPEG<br>
 * Max Density - 1.5<br>
 * Pixel Config - ARGB_8888<br>
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
     * the max pixel per inch deviceDensity to load the image in
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
    private ImageServiceAdapter mImageServiceAdapter;
    //endregion

    /**
     * @param key the unique key of the spec used for identification and debug
     * @param application The application object
     * @param imageServiceAdapter default URI enhancer to use for this spec image loading
     */
    ImageLoadSpecBuilder(String key, Application application, ImageServiceAdapter imageServiceAdapter) {
        FILUtils.notNullOrEmpty(key, "key");
        FILUtils.notNull(application, "application");
        FILUtils.notNull(imageServiceAdapter, "imageServiceAdapter");

        mKey = key;
        mApplication = application;
        mImageServiceAdapter = imageServiceAdapter;
    }

    /**
     * Get the display size of the device.
     */
    public Point getDisplaySize() {
        Point p = new Point();
        Display display = ((WindowManager) mApplication.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);
        return p;
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
        Point size = getDisplaySize();
        mWidth = size.x;
        mHeight = size.y;
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
     * the width and height of the image in pixels.<br>
     * to set one dimension and the second to scale set the second to 0.
     */
    public ImageLoadSpecBuilder setDimension(int width, int height) {
        mWidth = width;
        mHeight = height;
        return this;
    }

    /**
     * the width of the image in pixels.<br>
     * to set the height to scale set it to 0.
     */
    public ImageLoadSpecBuilder setWidth(int width) {
        mWidth = width;
        return this;
    }

    /**
     * the height of the image in pixels.<br>
     * to set the width to scale set it to 0.
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
     * the width and height of the image in pixels.<br>
     * to set one dimension and the second to scale set the second to 0.
     */
    public ImageLoadSpecBuilder setDimensionByResource(int widthResId, int heightResId) {
        mWidth = mApplication.getResources().getDimensionPixelSize(widthResId);
        mHeight = mApplication.getResources().getDimensionPixelSize(heightResId);
        return this;
    }

    /**
     * the width of the image by reading dimension resource by the given key.<br>
     * to set the height to scale set it to 0.
     */
    public ImageLoadSpecBuilder setWidthByResource(int resId) {
        mWidth = mApplication.getResources().getDimensionPixelSize(resId);
        return this;
    }

    /**
     * the height of the image by reading dimension resource by the given key.<br>
     * to set the width to scale set it to 0.
     */
    public ImageLoadSpecBuilder setHeightByResource(int resId) {
        mHeight = mApplication.getResources().getDimensionPixelSize(resId);
        return this;
    }

    /**
     * set the max pixel per inch deviceDensity to the device deviceDensity
     */
    public ImageLoadSpecBuilder setMaxDensity() {
        mMaxDensity = 9999;
        return this;
    }

    /**
     * the max pixel per inch deviceDensity to load the image in
     *
     * @throws IllegalArgumentException if value if &lt; 0.5
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
    public ImageLoadSpecBuilder setImageServiceAdapter(ImageServiceAdapter imageServiceAdapter) {
        mImageServiceAdapter = imageServiceAdapter;
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

        float deviceDensity = mApplication.getResources().getDisplayMetrics().density;
        float densityAdj = deviceDensity >= mMaxDensity ? mMaxDensity / deviceDensity : 1f;

        ImageLoadSpec spec = new ImageLoadSpec(mKey, (int) (mWidth * densityAdj), (int) (mHeight * densityAdj), mFormat, mPixelConfig, mImageServiceAdapter);

        FastImageLoader.addSpec(spec);

        return spec;
    }
}