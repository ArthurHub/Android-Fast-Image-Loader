package com.theartofdev.fastimageloaderdemo.zoom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;

import com.theartofdev.fastimageloaderdemo.R;

public class ZoomActivity extends ActionBarActivity {

    public static void startActivity(Activity activity, String uri, String specKey, String altSpecKey) {
        Intent intent = new Intent(activity, ZoomActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("specKey", specKey);
        intent.putExtra("altSpecKey", altSpecKey);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);

        String uri = getIntent().getStringExtra("uri");
        String specKey = getIntent().getStringExtra("specKey");
        String altSpecKey = getIntent().getStringExtra("altSpecKey");

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ZoomImageView zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image);
        zoomImageView.loadImage(uri, specKey, altSpecKey, progressBar);
    }
}
