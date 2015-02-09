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

import android.widget.ImageView;

import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.ReusableBitmap;

/**
 * See {@link TargetImageViewHandlerBase}.
 */
public class TargetImageViewDrawableHandler extends TargetImageViewHandlerBase<ImageView> {

    /**
     * @param imageView The image view to handle.
     */
    public TargetImageViewDrawableHandler(ImageView imageView) {
        super(imageView);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Set image drawable to null.
     */
    @Override
    protected void clearImage() {
        mImageView.setImageDrawable(null);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Create {@link TargetDrawable} and set it.
     */
    @Override
    protected void setImage(ReusableBitmap bitmap, LoadedFrom from) {
        TargetDrawable drawable = new TargetDrawable(bitmap.getBitmap(), from, mRounded, from == LoadedFrom.NETWORK && mImageView.getDrawable() == null);
        mImageView.setImageDrawable(drawable);
    }
}