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
import android.util.Log;

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
                .setWriteLogsToLogcat(true)
                .setLogLevel(Log.DEBUG)
                .setDebugIndicator(true);

        FastImageLoader.buildSpec(Specs.IMG_IX_UNBOUNDED)
                .setUnboundDimension()
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        FastImageLoader.buildSpec(Specs.IMG_IX_IMAGE)
                .setDimensionByDisplay()
                .setHeightByResource(R.dimen.image_height)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .build();

        UriEnhancerIdentity uriEnhancerIdentity = new UriEnhancerIdentity();
        FastImageLoader.buildSpec(Specs.INSTA_AVATAR)
                .setDimension(INSTAGRAM_AVATAR_SIZE)
                .setUriEnhancer(uriEnhancerIdentity)
                .build();

        FastImageLoader.buildSpec(Specs.INSTA_IMAGE)
                .setDimension(INSTAGRAM_IMAGE_SIZE)
                .setPixelConfig(Bitmap.Config.RGB_565)
                .setUriEnhancer(uriEnhancerIdentity)
                .build();

        FastImageLoader.buildSpec(Specs.UNBOUNDED_MAX)
                .setUnboundDimension()
                .setMaxDensity()
                .build();
    }
}

