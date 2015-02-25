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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.theartofdev.fastimageloader.LoadedFrom;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.YELLOW;

/**
 * Helper methods for Target Drawable or Image View code.
 */
public final class TargetHelper {

    /**
     * Used to paint debug indicator
     */
    private static Paint mDebugPaint;

    /**
     * Paint used to draw download progress
     */
    private static Paint mProgressPaint;

    /**
     * If to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    public static boolean debugIndicator;

    /**
     * The density of the current
     */
    public static float mDensity;

    private TargetHelper() {
    }

    /**
     * draw indicator on where the image was loaded from.<br>
     * Green - memory, Yellow - disk, Red - network.
     */
    public static void drawDebugIndicator(Canvas canvas, LoadedFrom loadedFrom, int width, int height) {
        if (debugIndicator) {
            if (mDebugPaint == null) {
                mDebugPaint = new Paint();
                mDebugPaint.setAntiAlias(true);
            }

            mDebugPaint.setColor(WHITE);
            canvas.drawCircle(width / 2, height / 2, 6 * mDensity, mDebugPaint);

            mDebugPaint.setColor(loadedFrom == LoadedFrom.MEMORY ? GREEN : loadedFrom == LoadedFrom.DISK ? YELLOW : RED);
            canvas.drawCircle(width / 2, height / 2, 4 * mDensity, mDebugPaint);
        }
    }

    /**
     * Draw indicator of download progress.
     *
     * @param downloaded downloaded bytes
     * @param contentLength total bytes
     */
    public static void drawProgressIndicator(Canvas canvas, long downloaded, long contentLength) {
        if (contentLength > 0 && downloaded < contentLength) {
            if (mProgressPaint == null) {
                mProgressPaint = new Paint();
                mProgressPaint.setAntiAlias(true);
                mProgressPaint.setColor(Color.argb(160, 0, 160, 0));
            }

            float s = (float) Math.min(36 * mDensity, Math.min(canvas.getWidth() * .2, canvas.getHeight() * .2));
            int l = canvas.getWidth() / 2;
            int t = canvas.getHeight() / 2;
            RectF rect = new RectF(l - s / 2, t - s / 2, l + s / 2, t + s / 2);
            canvas.drawArc(rect, -90, 360f * downloaded / contentLength, true, mProgressPaint);
        }
    }
}

