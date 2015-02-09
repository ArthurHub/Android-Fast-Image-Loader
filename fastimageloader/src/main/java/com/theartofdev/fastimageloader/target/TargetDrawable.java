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
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.impl.Utils;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.YELLOW;

/**
 * Drawable used for loaded images with additional capabilities:<br/>
 * 1. scale the image to the set rectangle.<br/>
 * 2. render round image<br/>
 * 3. fade effect for showing the image at start.<br/>
 * 4. showing indicator if the image was loading from memory/disk/network.<br/>
 */
public class TargetDrawable extends Drawable {

    //region: Fields and Consts

    private static final float FADE_DURATION = 200f;

    private static Paint mDebugPaint;

    private final Paint mPaint;

    private final Matrix mMatrix = new Matrix();

    private final LoadedFrom mLoadedFrom;

    private final float mBitmapWidth;

    private final float mBitmapHeight;

    protected float mScale = -1;

    protected int mTranslateX = -1;

    protected int mTranslateY = -1;

    /**
     * If to draw the bitmap as a circle
     */
    protected boolean mRounded;

    /**
     * used for fade animation progress
     */
    protected long mStartTimeMillis;
    //endregion

    /**
     * @param bitmap the bitmap to render in the drawable
     * @param loadedFrom where the bitmap was loaded from MEMORY/DISK/NETWORK for debug indicator
     * @param rounded is to render the bitmap rounded or rectangle
     * @param showFade if to show fade effect starting from now
     */
    public TargetDrawable(Bitmap bitmap, LoadedFrom loadedFrom, boolean rounded, boolean showFade) {
        Utils.notNull(bitmap, "bitmap");

        mLoadedFrom = loadedFrom;
        mRounded = rounded;

        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        mStartTimeMillis = showFade ? SystemClock.uptimeMillis() : 0;
    }

    /**
     * Is the drawable is currently animating fade-in of the image
     */
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
     * On set of bounds update the transform matrix applied on the bitmap to fit into the bounds.<br/>
     * - Scale to fit the dimensions of the image into the bounded rectangle.<br/>
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
     * Additional functionality:<br/>
     * Draw bitmap with opacity to show fade-in if animating.<br/>
     * Draw loaded from debug indicator.<br/>
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

        if (Utils.debugIndicator) {
            drawDebugIndicator(canvas);
        }
    }

    /**
     * Draw the bitmap on the canvas either rounded or rectangular.
     */
    protected void drawBitmap(Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();
        Utils.mRectF.set(0, 0, width, height);
        if (mRounded) {
            canvas.drawRoundRect(Utils.mRectF, width / 2, height / 2, mPaint);
        } else {
            canvas.drawRect(Utils.mRectF, mPaint);
        }
    }

    /**
     * draw indicator on where the image was loaded from.<br/>
     * Green - memory, Yellow - disk, Red - network.
     */
    private void drawDebugIndicator(Canvas canvas) {
        if (mDebugPaint == null) {
            mDebugPaint = new Paint();
            mDebugPaint.setAntiAlias(true);
        }

        int width = getBounds().width();
        int height = getBounds().height();

        mDebugPaint.setColor(WHITE);
        canvas.drawCircle(width / 2, height / 2, (int) (5 * Utils.density), mDebugPaint);

        mDebugPaint.setColor(mLoadedFrom == LoadedFrom.MEMORY ? GREEN : mLoadedFrom == LoadedFrom.DISK ? YELLOW : RED);
        canvas.drawCircle(width / 2, height / 2, (int) (3 * Utils.density), mDebugPaint);
    }
}