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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.theartofdev.fastimageloader.LoadState;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * {@link ImageView} with embedded handling of loading image using {@link com.theartofdev.fastimageloader.FastImageLoader}
 * and managing its lifecycle.
 */
public class TargetImageView extends ImageView {

    //region: Fields and Consts

    /**
     * The target image handler to load the image and control its lifecycle.
     */
    protected TargetImageViewHandler mHandler;

    /**
     * The placeholder drawable to draw while the image is not loaded
     */
    protected Drawable mPlaceholder;
    //endregion

    public TargetImageView(Context context) {
        super(context);
        mHandler = new TargetImageViewHandler(this);
        mHandler.setInvalidateOnDownloading(true);
    }

    public TargetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new TargetImageViewHandler(this);
        mHandler.setInvalidateOnDownloading(true);
    }

    public TargetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new TargetImageViewHandler(this);
        mHandler.setInvalidateOnDownloading(true);
    }

    /**
     * The URL source of the image
     */
    public String getUrl() {
        return mHandler.getUri();
    }

    /**
     * the spec to load the image by
     */
    public String getSpecKey() {
        return mHandler.getSpecKey();
    }

    /**
     * Is the image should be rendered rounded
     */
    public boolean isRounded() {
        return mHandler.isRounded();
    }

    /**
     * Is the image should be rendered rounded
     */
    public void setRounded(boolean isRounded) {
        mHandler.setRounded(isRounded);
    }

    /**
     * If to show download progress indicator when the requested image is downloading.
     */
    public boolean isShowDownloadProgressIndicator() {
        return mHandler.isInvalidateOnDownloading();
    }

    /**
     * If to show download progress indicator when the requested image is downloading.
     */
    public void setShowDownloadProgressIndicator(boolean show) {
        mHandler.setInvalidateOnDownloading(show);
    }

    /**
     * The placeholder drawable to draw while the image is not loaded
     */
    public Drawable getPlaceholder() {
        return mPlaceholder;
    }

    /**
     * The placeholder drawable to draw while the image is not loaded
     */
    public void setPlaceholder(Drawable placeholder) {
        mPlaceholder = placeholder;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            mHandler.clearUsedBitmap();
        }
    }

    /**
     * See: {@link #loadImage(String, String, String, boolean)}.
     */
    public void loadImage(String url, String specKey) {
        mHandler.loadImage(url, specKey, null, false);
    }

    /**
     * See: {@link #loadImage(String, String, String, boolean)}.
     */
    public void loadImage(String url, String specKey, String altSpecKey) {
        mHandler.loadImage(url, specKey, altSpecKey, false);
    }

    /**
     * Load image from the given source.
     *
     * @param url the avatar source URL to load from
     * @param specKey the spec to load the image by
     * @param altSpecKey optional: the spec to use for memory cached image in case the primary is not found.
     * @param force true - force image load even if it is the same source
     */
    public void loadImage(String url, String specKey, String altSpecKey, boolean force) {
        mHandler.loadImage(url, specKey, altSpecKey, force);
    }

    /**
     * On image view visibility change set show/hide on the image handler to it will update its in-use status.
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mHandler.onViewVisibilityChanged(visibility);
    }

    /**
     * Override draw to draw placeholder before the image if it is not loaded yet or animating fade-in.
     */
    @Override
    public void onDraw(@SuppressWarnings("NullableProblems") Canvas canvas) {
        if (getDrawable() == null || mHandler.isAnimating()) {
            drawPlaceholder(canvas, mHandler.getLoadState());
        }

        super.onDraw(canvas);

        if (isShowDownloadProgressIndicator() && mHandler.getContentLength() > 0 && mHandler.getDownloaded() < mHandler.getContentLength()) {
            drawProgressIndicator(canvas);
        }
    }

    /**
     * Draw placeholder if the image is loading/animating to show or failed to load.
     *
     * @param loadState the current load state of the image to draw specific placeholder
     */
    protected void drawPlaceholder(Canvas canvas, LoadState loadState) {
        if (mPlaceholder != null) {
            canvas.getClipBounds(FILUtils.rect);
            mPlaceholder.setBounds(FILUtils.rect);
            mPlaceholder.draw(canvas);
        }
    }

    /**
     * Draw indicator of download progress.
     */
    protected void drawProgressIndicator(Canvas canvas) {
        TargetHelper.drawProgressIndicator(canvas, mHandler.getDownloaded(), mHandler.getContentLength());
    }
}