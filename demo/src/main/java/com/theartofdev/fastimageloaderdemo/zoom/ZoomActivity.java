package com.theartofdev.fastimageloaderdemo.zoom;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;

import com.theartofdev.fastimageloaderdemo.R;

public class ZoomActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);

        String uri = getIntent().getStringExtra("uri");
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ZoomImageView zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image);
        zoomImageView.loadImage(uri, progressBar);
    }
}
