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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * {@link ImageView} with embedded handling of loading image using {@link FastImageLoader} and managing its lifecycle.
 */
public class TargetImageView extends ImageView {

    //region: Fields and Consts

    /**
     * The target image handler to load the image and control its lifecycle.
     */
    protected TargetImageViewHandler mHandler;
    //endregion

    public TargetImageView(Context context) {
        super(context);
        mHandler = new TargetImageViewHandler(this);
    }

    public TargetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new TargetImageViewHandler(this);
    }

    public TargetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new TargetImageViewHandler(this);
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

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            mHandler.clearUsedBitmap();
        }
    }

    /**
     * See: {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)}.
     */
    public void loadImage(String url, ImageLoadSpec spec) {
        mHandler.loadImage(url, spec, null, false);
    }

    /**
     * See: {@link #loadImage(String, ImageLoadSpec, ImageLoadSpec, boolean)}.
     */
    public void loadImage(String url, ImageLoadSpec spec, ImageLoadSpec altSpec) {
        mHandler.loadImage(url, spec, altSpec, false);
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
        mHandler.loadImage(url, spec, altSpec, force);
    }

    /**
     * On image view visibility change set show/hide on the image handler to it will update its in-use status.
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            mHandler.onViewShown();
        } else {
            mHandler.onViewHidden();
        }
    }
}