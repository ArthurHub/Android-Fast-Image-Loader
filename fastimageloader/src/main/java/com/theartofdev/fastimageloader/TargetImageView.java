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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * TODO:a add doc
 */
public class TargetImageView extends ImageView implements Target {

    //region: Fields and Consts

    /**
     * The loaded image
     */
    private RecycleBitmap mRecycleBitmap;

    /**
     * Is the image should be rendered rounded
     */
    private boolean mRounded;

    /**
     * The URL source of the image
     */
    private String mUrl;

    /**
     * the spec to load the image by
     */
    private ImageLoadSpec mSpec;

    /**
     * Is the image loading has failed
     */
    protected boolean mFailed;

    /**
     * Is the recycle bitmap is currently set in use in this image view, so not to set twice
     */
    protected boolean mInUse;

    /**
     * don't clear the existing image on next image request
     */
    private boolean mNoClearOnNextLoad;

    /**
     * the time the image load request started
     */
    private long mStartImageLoadTime;
    //endregion

    public TargetImageView(Context context) {
        super(context);
    }

    public TargetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TargetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    /**
     * don't clear the existing image on next image request, existing image will be shown until it is replaced.
     */
    public void setNoClearOnNextLoad(boolean noClearOnNextLoad) {
        mNoClearOnNextLoad = noClearOnNextLoad;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            clearUsedBitmap(true);
        }
    }

    /**
     * Clear the image shown in the view, cancel loading if image not yet loaded.
     */
    public void clearImage() {
        setImageDrawable(null);
    }

    /**
     * See: {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)}.
     */
    public void loadImage(String url, ImageLoadSpec spec) {
        loadImage(url, spec, null, false);
    }

    /**
     * See: {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)}.
     */
    public void loadImage(String url, ImageLoadSpec spec, ImageLoadSpec altSpec) {
        loadImage(url, spec, altSpec, false);
    }

    /**
     * Load image from the given source.
     *
     * @param url the avatar source URL to load from
     * @param spec the spec to load the image by
     * @param altSpec optional: the spec to use for memory cached image in case the primary is not found.
     * @param force true - force image load even if it is the same source
     */
    protected void loadImage(String url, ImageLoadSpec spec, ImageLoadSpec altSpec, boolean force) {
        CommonUtils.notNull(spec, "spec");

        if (!TextUtils.equals(mUrl, url) || TextUtils.isEmpty(url) || force) {
            mStartImageLoadTime = System.currentTimeMillis();
            if (!mNoClearOnNextLoad)
                setImageDrawable(null);
            mNoClearOnNextLoad = false;

            mUrl = url;
            mSpec = spec;

            if (!TextUtils.isEmpty(url)) {
                mFailed = false;
                FastImageLoader.loadImage(this, altSpec);
            } else {
                clearImage();
                mFailed = true;
            }
            invalidate();
        }
    }

    @Override
    public void onBitmapLoaded(RecycleBitmap bitmap, LoadedFrom from) {

        clearUsedBitmap(false);

        mInUse = true;
        mRecycleBitmap = bitmap;
        mRecycleBitmap.setInUse(true);

        setImageDrawable(new ImageDrawable(bitmap.getBitmap(), from, mRounded, from == LoadedFrom.NETWORK && getDrawable() == null));

        if (from != LoadedFrom.MEMORY) {
            Logger.info("LoadImage successful [{}] [{}]", from, System.currentTimeMillis() - mStartImageLoadTime);
        }
    }

    @Override
    public void onBitmapFailed() {
        if (getDrawable() == null) {
            mFailed = true;
            setImageDrawable(null);
            invalidate();
        }
        Logger.warn("LoadImage failed [{}]", System.currentTimeMillis() - mStartImageLoadTime);
    }

    @Override
    public void draw(@SuppressWarnings("NullableProblems") Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Throwable e) {
            Logger.critical("image draw failed [{}]", e, mRecycleBitmap);
            setImageDrawable(null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mRecycleBitmap != null && !mInUse) {
            if (mRecycleBitmap.isMatchingUrl(mUrl)) {
                mInUse = true;
                mRecycleBitmap.setInUse(true);
            } else {
                Logger.info("ImageView attachToWindow uses recycled bitmap, reload... [{}]", mRecycleBitmap);
                loadImage(mUrl, mSpec, null, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRecycleBitmap != null && mInUse) {
            mInUse = false;
            mRecycleBitmap.setInUse(false);
        }
    }

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    private void clearUsedBitmap(boolean full) {
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