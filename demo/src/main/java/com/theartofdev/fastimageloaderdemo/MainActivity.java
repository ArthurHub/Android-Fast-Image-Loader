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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloaderdemo.instagram.InstagramFragment;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ViewPager and set an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.pager_tabs);
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setPrefetchMenuIcon(menu.findItem(R.id.toggle_prefetch));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_disk_cache) {
            FastImageLoader.clearDiskCache();
            return true;
        } else if (item.getItemId() == R.id.toggle_prefetch) {
            AppApplication.mPrefetchImages = !AppApplication.mPrefetchImages;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("prefetch", AppApplication.mPrefetchImages).apply();

            setPrefetchMenuIcon(item);
            Toast.makeText(this, AppApplication.mPrefetchImages ? R.string.toggle_use_prefetch_on : R.string.toggle_use_prefetch_off, Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPrefetchMenuIcon(MenuItem item) {
        if (AppApplication.mPrefetchImages) {
            item.setIcon(R.drawable.ic_arrow_down_bold_circle_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_arrow_down_bold_circle_outline_white_24dp);
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"Instagram", "img IX"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new InstagramFragment();
                case 1:
                    return new ImgIXFragment();
            }
            return null;
        }
    }
}
