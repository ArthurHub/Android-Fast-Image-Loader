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

package com.theartofdev.fastimageloader.target;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloader.LoadState;
import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.Target;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * Handler for loading image as {@link com.theartofdev.fastimageloader.ReusableBitmap} and managing its lifecycle.<br/>
 * A single instance of the handler should be used for each ImageView.<br/>
 * <p/>
 * Use {@link #loadImage(String, String, String, boolean)} to load of image into the
 * handler, it will handle cancellation of unfinished requests if a new loading request is given.
 * <p/>
 * The handler attaches itself to ImageView StateChange to update the in-use of the loaded
 * bitmap, allowing to reuse bitmaps that are detached from window. This ensures the bitmap is
 * reused when the activity/fragment is destroyed.<br/>
 * On reattach to window if the bitmap was reused the image will be reloaded.
 * <p/>
 * For improved reuse it is advisable to override {@link android.widget.ImageView#onWindowVisibilityChanged(int)}
 * method and call {@link #onViewShown()}/{@link #onViewHidden()} on the handler to update in-use state.
 * <pre>
 * {@code protected void onWindowVisibilityChanged(int visibility) {
 *   super.onWindowVisibilityChanged(visibility);
 *   if (visibility == VISIBLE) {
 *     mHandler.onViewShown();
 *   } else {
 *     mHandler.onViewHidden();
 *   }
 * }}
 * </pre>
 */
public abstract class TargetImageViewHandlerBase<T extends ImageView> implements Target, View.OnAttachStateChangeListener {

    //region: Fields and Consts

    /**
     * the image view
     */
    protected T mImageView;

    /**
     * The URL source of the image
     */
    protected String mUrl;

    /**
     * the spec to load the image by
     */
    protected String mSpecKey;

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
     * The current loading states of the image in the handler.
     */
    protected LoadState mLoadState = LoadState.UNSET;

    /**
     * when the image load request started, measure image load request time
     */
    protected long mStartImageLoadTime;
    //endregion

    /**
     * @param imageView The image view to handle.
     */
    public TargetImageViewHandlerBase(T imageView) {
        FILUtils.notNull(imageView, "imageView");
        mImageView = imageView;
        mImageView.addOnAttachStateChangeListener(this);
    }

    /**
     * The current loading states of the image in the handler.
     */
    public LoadState getLoadState() {
        return mLoadState;
    }

    /**
     * Is the drawable is currently animating fade-in of the image
     */
    public boolean isAnimating() {
        Drawable drawable = mImageView.getDrawable();
        return drawable != null && drawable instanceof TargetDrawable && ((TargetDrawable) drawable).isAnimating();
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
    public String getSpecKey() {
        return mSpecKey;
    }

    /**
     * See {@link #loadImage(String, String, String, boolean)}.
     */
    public void loadImage(String url, String specKey) {
        loadImage(url, specKey, null, false);
    }

    /**
     * Load image from the given source.<br/>
     * If image of the same source is already requested/loaded the request is ignored unless force is true.
     *
     * @param url the avatar source URL to load from
     * @param specKey the spec to load the image by
     * @param altSpecKey optional: the spec to use for memory cached image in case the primary is not found.
     * @param force true - force image load even if it is the same source
     */
    public void loadImage(String url, String specKey, String altSpecKey, boolean force) {
        FILUtils.notNull(specKey, "spec");

        if (!TextUtils.equals(mUrl, url) || TextUtils.isEmpty(url) || force) {
            mStartImageLoadTime = System.currentTimeMillis();
            clearImage();

            mUrl = url;
            mSpecKey = specKey;

            if (!TextUtils.isEmpty(url)) {
                mLoadState = LoadState.LOADING;
                FastImageLoader.loadImage(this, altSpecKey);
            } else {
                clearUsedBitmap();
                clearImage();
            }
            mImageView.invalidate();
        }
    }

    @Override
    public void onBitmapLoaded(ReusableBitmap bitmap, LoadedFrom from) {

        clearUsedBitmap(false);

        mLoadState = LoadState.LOADED;

        mInUse = true;
        mReusableBitmap = bitmap;
        mReusableBitmap.incrementInUse();

        setImage(bitmap, from);

        FILLogger.operation(mUrl, mSpecKey, from, true, System.currentTimeMillis() - mStartImageLoadTime);
    }

    @Override
    public void onBitmapFailed() {
        String url = mUrl;
        String specKey = mSpecKey;
        mLoadState = LoadState.FAILED;
        if (mImageView.getDrawable() == null) {
            clearImage();
            mImageView.invalidate();
        }
        FILLogger.operation(url, specKey, null, false, System.currentTimeMillis() - mStartImageLoadTime);
    }

    /**
     * On image view shown verify that the set bitmap is still valid for the image view (not reused).<br/>
     * If valid: set in-use on the bitmap.<br/>
     * If not valid: execute image load request to re-load the image needed for the image view.<br/>
     */
    public void onViewShown() {
        if (mReusableBitmap != null && !mInUse) {
            if (TextUtils.equals(mReusableBitmap.getUrl(), mUrl)) {
                mInUse = true;
                mReusableBitmap.incrementInUse();
            } else {
                FILLogger.info("ImageView attachToWindow uses recycled bitmap, reload... [{}]", mReusableBitmap);
                loadImage(mUrl, mSpecKey, null, true);
            }
        }
    }

    /**
     * On image view hidden set the used bitmap to not-in-use so it can be reused.
     */
    public void onViewHidden() {
        if (mReusableBitmap != null && mInUse) {
            mInUse = false;
            mReusableBitmap.decrementInUse();
        }
    }

    /**
     * On attach of the ImageView to window call {@link #onViewShown()}.
     */
    @Override
    public void onViewAttachedToWindow(View v) {
        onViewShown();
    }

    /**
     * On detach of the ImageView from window call {@link #onViewHidden()}.
     */
    @Override
    public void onViewDetachedFromWindow(View v) {
        onViewHidden();
    }

    /**
     * Release the resource, unregister from state change.
     */
    public void close() {
        clearUsedBitmap();
        mImageView.removeOnAttachStateChangeListener(this);
    }

    /**
     * Called to clear the existing image in the handled image view.<br/>
     * Called on loading or failure.
     */
    protected abstract void clearImage();

    /**
     * Called to set the loaded image bitmap in the handled image view.
     */
    protected abstract void setImage(ReusableBitmap bitmap, LoadedFrom from);

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    void clearUsedBitmap() {
        clearUsedBitmap(true);
    }

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    void clearUsedBitmap(boolean full) {
        if (full) {
            mUrl = null;
            mSpecKey = null;
        }
        mLoadState = LoadState.UNSET;
        if (mReusableBitmap != null) {
            if (mInUse) {
                mInUse = false;
                mReusableBitmap.decrementInUse();
            }
            mReusableBitmap = null;
        }
    }
}