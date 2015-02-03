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

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theartofdev.fastimageloader.FastImageLoader;
import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.TargetImageView;
import com.theartofdev.fastimageloaderdemo.zoom.ZoomActivity;

public final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private String[] mItems = new String[]{
            "http://assets.imgix.net/examples/clownfish.jpg",
            "http://assets.imgix.net/examples/espresso.jpg",
            "http://assets.imgix.net/examples/kayaks.png",
            "http://assets.imgix.net/examples/leaves.jpg",
            "http://assets.imgix.net/examples/puffins.jpg",
            "http://assets.imgix.net/examples/redleaf.jpg",
            "http://assets.imgix.net/examples/butterfly.jpg",
            "http://assets.imgix.net/examples/blueberries.jpg",
            "http://assets.imgix.net/examples/octopus.jpg"
    };

    public Adapter() {
        for (String item : mItems) {
            FastImageLoader.prefetchImage(item, Specs.IMG_IX_IMAGE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageLoadSpec spec = FastImageLoader.getSpec(Specs.IMG_IX_IMAGE);
        holder.mUrlTextView.setText(mItems[position]);
        holder.mSpecTextView.setText(spec.getFormat() + ": (" + spec.getWidth() + "," + spec.getHeight() + ") Config: " + spec.getPixelConfig());
        holder.mTargetImageView.loadImage(mItems[position], spec.getKey());
    }

    //region: Inner class: ViewHolder

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mUrlTextView;

        public final TextView mSpecTextView;

        public final TargetImageView mTargetImageView;

        public ViewHolder(View v) {
            super(v);
            mUrlTextView = (TextView) v.findViewById(R.id.image_url);
            mSpecTextView = (TextView) v.findViewById(R.id.image_spec);
            mTargetImageView = (TargetImageView) v.findViewById(R.id.image_view);
            mTargetImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Activity activity = (Activity) mTargetImageView.getContext();
            if (activity != null) {
                ZoomActivity.startActivity(activity, mTargetImageView.getUrl(), Specs.UNBOUNDED_MAX, Specs.IMG_IX_IMAGE);
            }
        }

    }
    //endregion
}