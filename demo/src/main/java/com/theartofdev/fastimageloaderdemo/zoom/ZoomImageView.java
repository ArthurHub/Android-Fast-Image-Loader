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

package com.theartofdev.fastimageloaderdemo.zoom;

import android.content.Context;
import android.util.AttributeSet;

import com.theartofdev.fastimageloader.TargetImageViewBitmapHandler;
import com.theartofdev.fastimageloaderdemo.Specs;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class ZoomImageView extends ImageViewTouch {

    /**
     * The target image handler to load the image and control its lifecycle.
     */
    protected TargetImageViewBitmapHandler mHandler;

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new TargetImageViewBitmapHandler(this);
        setDisplayType(DisplayType.FIT_TO_SCREEN);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new TargetImageViewBitmapHandler(this);
        setDisplayType(DisplayType.FIT_TO_SCREEN);
    }

    /**
     * Load the given image into the zoom image view.
     */
    public void loadImage(String url) {
        mHandler.loadImage(url, Specs.ZOOM_IMAGE, Specs.IMAGE, false);
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

