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
import android.view.View;
import android.widget.ImageView;

/**
 * Handler for loading image as {@link ReusableBitmap} and managing its lifecycle.<br/>
 * A single instance of the handler should be used for each ImageView.<br/>
 * <p/>
 * Use {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)} to load of image into the
 * handler, it will handle cancellation of unfinished requests if a new loading request is given.
 * <p/>
 * The handler attaches itself to ImageView StateChange to update the in-use of the loaded bitmap, allowing
 * to reuse bitmap that are currently not showed, on re-attach to window if the bitmap was reused the image
 * will be re-loaded.
 */
public class TargetImageViewHandler implements Target, View.OnAttachStateChangeListener {

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
    protected ReusableBitmap mReusableBitmap;

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
     * @param imageView The image view to handle.
     */
    public TargetImageViewHandler(ImageView imageView) {
        Utils.notNull(imageView, "imageView");
        mImageView = imageView;
        mImageView.addOnAttachStateChangeListener(this);
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
     * See {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)}.
     */
    public void loadImage(String url, ImageLoadSpec spec) {
        loadImage(url, spec, null, false);
    }

    /**
     * Load image from the given source.<br/>
     * If image of the same source is already requested/loaded the request is ignored unless force is true.
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
                clearUsedBitmap();
                mImageView.setImageDrawable(null);
            }
            mImageView.invalidate();
        }
    }

    @Override
    public void onBitmapLoaded(ReusableBitmap bitmap, LoadedFrom from) {

        clearUsedBitmap();

        mUrl = bitmap.getUrl();
        mSpec = bitmap.getSpec();

        mInUse = true;
        mReusableBitmap = bitmap;
        mReusableBitmap.incrementInUse();

        TargetDrawable drawable = new TargetDrawable(bitmap.getBitmap(), from, mRounded, from == LoadedFrom.NETWORK && mImageView.getDrawable() == null);
        mImageView.setImageDrawable(drawable);

        if (from != LoadedFrom.MEMORY) {
            Logger.info("LoadImage successful [{}] [{}]", from, System.currentTimeMillis() - mStartImageLoadTime);
        }
    }

    @Override
    public void onBitmapFailed() {

        if (mImageView.getDrawable() == null) {
            mImageView.setImageDrawable(null);
            mImageView.invalidate();
        }

        Logger.warn("LoadImage failed [{}]", System.currentTimeMillis() - mStartImageLoadTime);
    }

    /**
     * On attach of the ImageView to window verify that the set bitmap is still valid for the
     * image view (not reused).<br/>
     * If valid: set in-use on the bitmap.<br/>
     * If not valid: execute image load request to re-load the image needed for the image view.<br/>
     */
    @Override
    public void onViewAttachedToWindow(View v) {
        if (mReusableBitmap != null && !mInUse) {
            if (TextUtils.equals(mReusableBitmap.getUrl(), mUrl)) {
                mInUse = true;
                mReusableBitmap.incrementInUse();
            } else {
                Logger.info("ImageView attachToWindow uses recycled bitmap, reload... [{}]", mReusableBitmap);
                loadImage(mUrl, mSpec, null, true);
            }
        }
    }

    /**
     * On detach of the ImageView from window set the used bitmap to not-in-use so it can be reused.
     */
    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mReusableBitmap != null && mInUse) {
            mInUse = false;
            mReusableBitmap.decrementInUse();
        }
    }

    /**
     * Release the resource, unregister from state change.
     */
    public void close() {
        clearUsedBitmap();
        mImageView.removeOnAttachStateChangeListener(this);
    }

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    void clearUsedBitmap() {
        mUrl = null;
        mSpec = null;
        if (mReusableBitmap != null) {
            if (mInUse) {
                mInUse = false;
                mReusableBitmap.decrementInUse();
            }
            mReusableBitmap = null;
        }
    }
}