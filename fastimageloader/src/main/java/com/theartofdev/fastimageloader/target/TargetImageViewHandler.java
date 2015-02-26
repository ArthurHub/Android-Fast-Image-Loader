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
 * Handler for loading image as {@link com.theartofdev.fastimageloader.ReusableBitmap} and managing its lifecycle.<br>
 * A single instance of the handler should be used for each ImageView.<br>
 * <br><br>
 * Use {@link #loadImage(String, String, String, boolean)} to load of image into the
 * handler, it will handle cancellation of unfinished requests if a new loading request is given.
 * <br><br>
 * The handler attaches itself to ImageView StateChange to update the in-use of the loaded
 * bitmap, allowing to reuse bitmaps that are detached from window. This ensures the bitmap is
 * reused when the activity/fragment is destroyed.<br>
 * On reattach to window if the bitmap was reused the image will be reloaded.
 * <br><br>
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
public class TargetImageViewHandler implements Target, View.OnAttachStateChangeListener {

    //region: Fields and Consts

    /**
     * the image view
     */
    protected final ImageView mImageView;

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
     * Is the handled image view should be invalidated on bitmap downloading progress event
     */
    protected boolean mInvalidateOnDownloading;

    /**
     * The current loading states of the image in the handler.
     */
    protected LoadState mLoadState = LoadState.UNSET;

    /**
     * when the image load request started, measure image load request time
     */
    protected long mStartImageLoadTime;

    /**
     * the number of bytes already downloaded, if requested image is downloading
     */
    protected long mDownloaded;

    /**
     * the total number of bytes to download, if requested image is downloading
     */
    protected long mContentLength;
    //endregion

    /**
     * @param imageView The image view to handle.
     */
    public TargetImageViewHandler(ImageView imageView) {
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
        return drawable != null &&
                drawable instanceof AnimatingTargetDrawable &&
                ((AnimatingTargetDrawable) drawable).isAnimating();
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
     * Is the handled image view should be invalidated on bitmap downloading progress event
     */
    public boolean isInvalidateOnDownloading() {
        return mInvalidateOnDownloading;
    }

    /**
     * Is the handled image view should be invalidated on bitmap downloading progress event
     */
    public void setInvalidateOnDownloading(boolean invalidateOnDownloading) {
        mInvalidateOnDownloading = invalidateOnDownloading;
    }

    /**
     * the number of bytes already downloaded, if requested image is downloading
     */
    public long getDownloaded() {
        return mDownloaded;
    }

    /**
     * the total number of bytes to download, if requested image is downloading
     */
    public long getContentLength() {
        return mContentLength;
    }

    @Override
    public String getUri() {
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
     * See {@link #loadImage(String, String, String, boolean)}.
     */
    public void loadImage(String url, String specKey, String altSpecKey) {
        loadImage(url, specKey, altSpecKey, false);
    }

    /**
     * Load image from the given source.<br>
     * If image of the same source is already requested/loaded the request is ignored unless force is true.
     *
     * @param url the avatar source URL to load from
     * @param specKey the spec to load the image by
     * @param altSpecKey optional: the spec to use for memory cached image in case the primary is not found.
     * @param force true - force image load even if it is the same source
     */
    public void loadImage(String url, String specKey, String altSpecKey, boolean force) {
        FILUtils.notNull(specKey, "spec");

        mDownloaded = 0;
        mContentLength = 0;
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

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    public void clearUsedBitmap() {
        clearUsedBitmap(true);
    }

    @Override
    public void onBitmapDownloading(long downloaded, long contentLength) {
        mDownloaded = downloaded;
        mContentLength = contentLength;
        if (mInvalidateOnDownloading) {
            mImageView.postInvalidate();
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
            mUrl = url;
            mSpecKey = specKey;
            mImageView.invalidate();
        }
        FILLogger.operation(url, specKey, null, false, System.currentTimeMillis() - mStartImageLoadTime);
    }

    /**
     * Handle Image View visibility change by updating the used bitmap.<br>
     * If the Image View is hidden then we decrement the in-use.<br>
     * If the Image View is shown we use the existing bitmap or reload the image.
     */
    public void onViewVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            onViewShown();
        } else {
            onViewHidden();
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
     * On image view shown verify that the set bitmap is still valid for the image view (not reused).<br>
     * If valid: set in-use on the bitmap.<br>
     * If not valid: execute image load request to re-load the image needed for the image view.<br>
     */
    protected void onViewShown() {
        if (mReusableBitmap != null && !mInUse) {
            if (TextUtils.equals(mReusableBitmap.getUri(), mUrl)) {
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
    protected void onViewHidden() {
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
     * Called to clear the existing image in the handled image view.<br>
     * Called on loading or failure.
     */
    protected void clearImage() {
        mImageView.setImageDrawable(null);
    }

    /**
     * Called to set the loaded image bitmap in the handled image view.
     */
    protected void setImage(ReusableBitmap bitmap, LoadedFrom from) {
        boolean showFade = from == LoadedFrom.NETWORK && mImageView.getDrawable() == null;
        Drawable drawable = mRounded
                ? new TargetCircleDrawable(bitmap.getBitmap(), from, showFade)
                : new TargetDrawable(bitmap.getBitmap(), from, showFade);
        mImageView.setImageDrawable(drawable);
    }

    /**
     * Clear the currently used bitmap and mark it as not in use.
     */
    protected void clearUsedBitmap(boolean full) {
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