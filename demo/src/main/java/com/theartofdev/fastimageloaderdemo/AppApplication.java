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
import com.theartofdev.fastimageloader.UriEnhancerIdentity;
import com.theartofdev.fastimageloader.UriEnhancerImgIX;

public class AppApplication extends Application {

    public static final int INSTAGRAM_IMAGE_SIZE = 640;

    public static final int INSTAGRAM_AVATAR_SIZE = 150;

    @Override
    public void onCreate() {
        super.onCreate();

        FastImageLoader
                .init(this)
                .setDefaultUriEnhancer(new UriEnhancerImgIX())
                .setDebugIndicator(true);

        Specs.UNBOUNDED = FastImageLoader.createSpec()
                .setUnboundDimension()
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        Specs.IMAGE = FastImageLoader.createSpec()
                .setDimensionByDisplay()
                .setHeightByResource(R.dimen.image_height)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        UriEnhancerIdentity uriEnhancerIdentity = new UriEnhancerIdentity();
        Specs.INSTA_AVATAR = FastImageLoader.createSpec()
                .setDimension(INSTAGRAM_AVATAR_SIZE)
                .setUriEnhancer(uriEnhancerIdentity)
                .build();

        Specs.INSTA_IMAGE = FastImageLoader.createSpec()
                .setDimension(INSTAGRAM_IMAGE_SIZE)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .setUriEnhancer(uriEnhancerIdentity)
                .build();
    }
}

