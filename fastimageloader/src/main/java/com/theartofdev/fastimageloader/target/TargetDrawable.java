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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;

import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * Drawable used for loaded images with additional capabilities:<br>
 * 1. fade effect for showing the image at start.<br>
 * 2. showing indicator if the image was loading from memory/disk/network.<br>
 */
public class TargetDrawable extends BitmapDrawable implements AnimatingTargetDrawable {

    //region: Fields and Consts

    private static final float FADE_DURATION = 200f;

    protected final LoadedFrom mLoadedFrom;

    /**
     * used for fade animation progress
     */
    protected long mStartTimeMillis;
    //endregion

    /**
     * @param bitmap the bitmap to render in the drawable
     * @param loadedFrom where the bitmap was loaded from MEMORY/DISK/NETWORK for debug indicator
     * @param showFade if to show fade effect starting from now
     */
    public TargetDrawable(Bitmap bitmap, LoadedFrom loadedFrom, boolean showFade) {
        super(bitmap);
        FILUtils.notNull(bitmap, "bitmap");

        mLoadedFrom = loadedFrom;

        mStartTimeMillis = showFade ? SystemClock.uptimeMillis() : 0;
    }

    @Override
    public boolean isAnimating() {
        return mStartTimeMillis > 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additional functionality:<br>
     * Draw bitmap with opacity to show fade-in if animating.<br>
     * Draw loaded from debug indicator.<br>
     * </p>
     */
    @Override
    public void draw(Canvas canvas) {
        float normalized = (SystemClock.uptimeMillis() - mStartTimeMillis) / FADE_DURATION;
        if (normalized >= 1f) {
            super.draw(canvas);
            if (mStartTimeMillis > 0)
                invalidateSelf();
            mStartTimeMillis = 0;
        } else {
            setAlpha((int) (255 * normalized));
            super.draw(canvas);
            setAlpha(255);
            invalidateSelf();
        }

        if (TargetHelper.debugIndicator) {
            Rect bounds = getBounds();
            TargetHelper.drawDebugIndicator(canvas, mLoadedFrom, bounds.width(), bounds.height());
        }
    }

    //region: Inner class: Animated

    //endregion
}