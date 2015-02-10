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
import android.graphics.Paint;

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
     * If to show indicator if the image was loaded from MEMORY/DISK/NETWORK.
     */
    public static boolean debugIndicator;

    private TargetHelper() {
    }

    /**
     * draw indicator on where the image was loaded from.<br/>
     * Green - memory, Yellow - disk, Red - network.
     */
    public static void drawDebugIndicator(Canvas canvas, LoadedFrom loadedFrom, int width, int height) {
        if (debugIndicator) {
            if (mDebugPaint == null) {
                mDebugPaint = new Paint();
                mDebugPaint.setAntiAlias(true);
            }

            mDebugPaint.setColor(WHITE);
            canvas.drawCircle(width / 2, height / 2, 12, mDebugPaint);

            mDebugPaint.setColor(loadedFrom == LoadedFrom.MEMORY ? GREEN : loadedFrom == LoadedFrom.DISK ? YELLOW : RED);
            canvas.drawCircle(width / 2, height / 2, 8, mDebugPaint);
        }
    }
}

