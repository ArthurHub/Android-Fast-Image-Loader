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

import android.widget.ImageView;

/**
 * See {@link TargetImageViewHandlerBase}.
 */
public class TargetImageViewBitmapHandler extends TargetImageViewHandlerBase<ImageView> {

    /**
     * @param imageView The image view to handle.
     */
    public TargetImageViewBitmapHandler(ImageView imageView) {
        super(imageView);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Set image view bitmap to null.
     */
    @Override
    protected void clearImage() {
        mImageView.setImageBitmap(null);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Set image view bitmap to the loaded image.
     */
    @Override
    protected void setImage(ReusableBitmap bitmap, LoadedFrom from) {
        mImageView.setImageBitmap(bitmap.getBitmap());
    }
}