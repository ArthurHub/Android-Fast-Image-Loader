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
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * Drawable used for loaded images with additional capabilities:<br>
 * 1. scale the image to the set rectangle.<br>
 * 2. render round image<br>
 * 3. fade effect for showing the image at start.<br>
 * 4. showing indicator if the image was loading from memory/disk/network.<br>
 */
public class TargetCircleDrawable extends Drawable implements AnimatingTargetDrawable {

    //region: Fields and Consts

    private static final float FADE_DURATION = 200f;

    protected final Paint mPaint;

    protected final Matrix mMatrix = new Matrix();

    protected final LoadedFrom mLoadedFrom;

    protected final float mBitmapWidth;

    protected final float mBitmapHeight;

    protected float mScale = -1;

    protected int mTranslateX = -1;

    protected int mTranslateY = -1;

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
    public TargetCircleDrawable(Bitmap bitmap, LoadedFrom loadedFrom, boolean showFade) {
        FILUtils.notNull(bitmap, "bitmap");

        mLoadedFrom = loadedFrom;

        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        mStartTimeMillis = showFade ? SystemClock.uptimeMillis() : 0;
    }

    @Override
    public boolean isAnimating() {
        return mStartTimeMillis > 0;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * On set of bounds update the transform matrix applied on the bitmap to fit into the bounds.<br>
     * - Scale to fit the dimensions of the image into the bounded rectangle.<br>
     * - Offset the rendered bitmap to center the dimension that is larger\smaller than the bounds.
     * </p>
     */
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        float scale = Math.max((right - left) / mBitmapWidth, (bottom - top) / mBitmapHeight);
        int translateX = (int) (((right - left) - mBitmapWidth * scale) / 2);
        int translateY = (int) (((bottom - top) - mBitmapHeight * scale) / 2);

        if (Math.abs(scale - mScale) > 0.01 || translateX != mTranslateX || translateY != mTranslateY) {
            mScale = scale;
            mTranslateX = translateX;
            mTranslateY = translateY;
            if (mScale != 0 || mTranslateX != 0 || mTranslateY != 0) {
                if (mScale != 0)
                    mMatrix.setScale(mScale, mScale);
                if (mTranslateX != 0 || mTranslateY != 0)
                    mMatrix.postTranslate(mTranslateX, mTranslateY);
                mPaint.getShader().setLocalMatrix(mMatrix);
            } else {
                mPaint.getShader().setLocalMatrix(null);
            }
        }
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
            drawBitmap(canvas);
            if (mStartTimeMillis > 0)
                invalidateSelf();
            mStartTimeMillis = 0;
        } else {
            mPaint.setAlpha((int) (255 * normalized));
            drawBitmap(canvas);
            mPaint.setAlpha(255);
            invalidateSelf();
        }

        if (TargetHelper.debugIndicator) {
            Rect bounds = getBounds();
            TargetHelper.drawDebugIndicator(canvas, mLoadedFrom, bounds.width(), bounds.height());
        }
    }

    /**
     * Draw the bitmap on the canvas either rounded or rectangular.
     */
    protected void drawBitmap(Canvas canvas) {
        Rect bounds = getBounds();
        FILUtils.rectF.set(0, 0, bounds.width(), bounds.height());
        canvas.drawRoundRect(FILUtils.rectF, bounds.width() / 2, bounds.height() / 2, mPaint);
    }
}