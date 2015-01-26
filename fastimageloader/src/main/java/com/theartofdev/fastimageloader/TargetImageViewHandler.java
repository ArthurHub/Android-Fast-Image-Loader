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

import android.text.TextUtils;
import android.widget.ImageView;

/**
 * TODO:a add doc
 */
public class TargetImageViewHandler implements Target {

    //region: Fields and Consts

    /**
     * the image view
     */
    protected ImageView mImageView;

    /**
     * The URL source of the image
     */
    protected String mUrl;

    /**
     * the spec to load the image by
     */
    protected ImageLoadSpec mSpec;

    /**
     * The loaded image
     */
    protected RecycleBitmap mRecycleBitmap;

    /**
     * Is the recycle bitmap is currently set in use in this image view, so not to set twice
     */
    protected boolean mInUse;

    /**
     * Is the image should be rendered rounded
     */
    protected boolean mRounded;

    /**
     * when the image load request started, measure image load request time
     */
    protected long mStartImageLoadTime;
    //endregion

    /**
     * The image view to handle.
     */
    public TargetImageViewHandler(ImageView imageView) {
        Utils.notNull(imageView, "imageView");
        mImageView = imageView;
    }

    /**
     * Is the image should be rendered rounded
     */
    public boolean isRounded() {
        return mRounded;
    }

    /**
     * Is the image should be rendered rounded
     */
    public void setRounded(boolean isRounded) {
        mRounded = isRounded;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    /**
     * Load image from the given source.
     *
     * @param url the avatar source URL to load from
     * @param spec the spec to load the image by
     * @param altSpec optional: the spec to use for memory cached image in case the primary is not found.
     * @param force true - force image load even if it is the same source
     */
    public void loadImage(String url, ImageLoadSpec spec, ImageLoadSpec altSpec, boolean force) {
        Utils.notNull(spec, "spec");

        if (!TextUtils.equals(mUrl, url) || TextUtils.isEmpty(url) || force) {
            mStartImageLoadTime = System.currentTimeMillis();
            mImageView.setImageDrawable(null);

            mUrl = url;
            mSpec = spec;

            if (!TextUtils.isEmpty(url)) {
                FastImageLoader.loadImage(this, altSpec);
            } else {
                clearUsedBitmap(true);
                mImageView.setImageDrawable(null);
            }
            mImageView.invalidate();
        }
    }

    @Override
    public void onBitmapLoaded(RecycleBitmap bitmap, LoadedFrom from) {

        clearUsedBitmap(false);

        mInUse = true;
        mRecycleBitmap = bitmap;
        mRecycleBitmap.setInUse(true);

        TargetDrawable drawable = new TargetDrawable(bitmap.getBitmap(), from, mRounded, from == LoadedFrom.NETWORK && mImageView.getDrawable() == null);
        mImageView.setImageDrawable(drawable);

        if (from != LoadedFrom.MEMORY) {
            ULogger.info("LoadImage successful [{}] [{}]", from, System.currentTimeMillis() - mStartImageLoadTime);
        }
    }

    @Override
    public void onBitmapFailed() {
        if (mImageView.getDrawable() == null) {
            mImageView.setImageDrawable(null);
            mImageView.invalidate();
        }
        ULogger.warn("LoadImage failed [{}]", System.currentTimeMillis() - mStartImageLoadTime);
    }

    /**
     *
     */
    public void attachedToWindow() {
        if (mRecycleBitmap != null && !mInUse) {
            if (mRecycleBitmap.isMatchingUrl(mUrl)) {
                mInUse = true;
                mRecycleBitmap.setInUse(true);
            } else {
                ULogger.info("ImageView attachToWindow uses recycled bitmap, reload... [{}]", mRecycleBitmap);
                loadImage(mUrl, mSpec, null, true);
            }
        }
    }

    /**
     *
     */
    public void detachedFromWindow() {
        if (mRecycleBitmap != null && mInUse) {
            mInUse = false;
            mRecycleBitmap.setInUse(false);
        }
    }

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    void clearUsedBitmap(boolean full) {
        if (full) {
            mUrl = null;
            mSpec = null;
        }
        if (mRecycleBitmap != null) {
            if (mInUse) {
                mInUse = false;
                mRecycleBitmap.setInUse(false);
            }
            mRecycleBitmap = null;
        }
    }
}