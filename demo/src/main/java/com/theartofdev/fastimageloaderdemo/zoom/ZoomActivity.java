package com.theartofdev.fastimageloaderdemo.zoom;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.Target;
import com.theartofdev.fastimageloaderdemo.R;
import com.theartofdev.fastimageloaderdemo.Specs;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class ZoomActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);

        ZoomImageView zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image);
        zoomImageView.loadImage(getIntent().getStringExtra("uri"));
        //
        //        new setImage((ImageViewTouch) findViewById(R.id.zoom_image)).loadImage(getIntent().getStringExtra("uri"));
    }

    private class bla implements Target {

        private ImageViewTouch mImageViewTouch;

        private String mUrl;

        private bla(ImageViewTouch imageViewTouch) {
            mImageViewTouch = imageViewTouch;
        }

        /**
         *
         */
        public void loadImage(String url) {
            mUrl = url;
            FastImageLoader.loadImage(this, null);
        }

        @Override
        public String getUrl() {
            return mUrl;
        }

        @Override
        public String getSpecKey() {
            return Specs.ZOOM_IMAGE;
        }

        @Override
        public void onBitmapLoaded(ReusableBitmap bitmap, LoadedFrom from) {
            mImageViewTouch.setImageBitmap(bitmap.getBitmap(), null, -1, 8f);
        }

        @Override
        public void onBitmapFailed() {

        }

    }
}
