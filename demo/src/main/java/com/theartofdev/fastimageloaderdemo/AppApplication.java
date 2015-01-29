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
import android.graphics.Bitmap;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloader.ImageLoadSpecBuilder;
import com.theartofdev.fastimageloader.UriEnhancerImgIX;

public class AppApplication extends Application {

    public static final int INSTAGRAM_IMAGE_SIZE = 640;

    @Override
    public void onCreate() {
        super.onCreate();

        FastImageLoader
                .init(this)
                .setUriEnhancer(new UriEnhancerImgIX())
                .setDebugIndicator(true);

        Specs.UNBOUNDED = new ImageLoadSpecBuilder()
                .setUnboundDimension()
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        Specs.IMAGE = new ImageLoadSpecBuilder()
                .setDimensionByDisplay()
                .setHeightByResource(R.dimen.image_height)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        Specs.INSTA_AVATAR = new ImageLoadSpecBuilder()
                .setDimensionByResource(R.dimen.avatar_size)
                .setMaxDensity(2)
                .build();

        Specs.INSTA_IMAGE = new ImageLoadSpecBuilder()
                .setDimension(INSTAGRAM_IMAGE_SIZE, INSTAGRAM_IMAGE_SIZE)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();
    }
}

