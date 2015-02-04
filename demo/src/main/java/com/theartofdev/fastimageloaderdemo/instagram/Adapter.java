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

package com.theartofdev.fastimageloaderdemo.instagram;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloaderdemo.AppApplication;
import com.theartofdev.fastimageloaderdemo.Specs;
import com.theartofdev.fastimageloaderdemo.instagram.service.Feed;
import com.theartofdev.fastimageloaderdemo.instagram.service.InstagramService;
import com.theartofdev.fastimageloaderdemo.instagram.service.Item;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private InstagramService mService;

    private Item[] mItems = new Item[0];

    public Adapter() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.instagram.com/v1")
                .build();
        mService = restAdapter.create(InstagramService.class);
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public void loadData(final Callback<Feed> callback) {
        mService.getFeed(new Callback<Feed>() {
            @Override
            public void success(Feed feed, Response response) {
                mItems = feed.data;
                if (AppApplication.mPrefetchImages) {
                    for (Item item : mItems) {
                        FastImageLoader.prefetchImage(item.user.profile_picture, Specs.INSTA_IMAGE);
                        FastImageLoader.prefetchImage(item.images.standard_resolution.url, Specs.INSTA_IMAGE);
                    }
                }
                Adapter.this.notifyDataSetChanged();
                callback.success(feed, response);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new ItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((ItemView) holder.itemView).setData(mItems[position]);
    }

    //region: Inner class: ViewHolder

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static final class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(ItemView v) {
            super(v);
        }
    }
    //endregion
}

