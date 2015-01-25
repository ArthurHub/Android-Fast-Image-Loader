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

package com.theartofdev.fastimageloaderdemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloader.ImageLoadSpec;

/**
 * TODO:a add doc
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FastImageLoader.init(this);

        Point p = new Point();
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);

        Specs.IMAGE = new ImageLoadSpec(p.x, getResources().getDimensionPixelSize(R.dimen.image_height));
    }
}

