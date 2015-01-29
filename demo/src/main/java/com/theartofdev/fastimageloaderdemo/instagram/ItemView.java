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

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.fastimageloader.TargetImageView;
import com.theartofdev.fastimageloaderdemo.R;
import com.theartofdev.fastimageloaderdemo.Specs;
import com.theartofdev.fastimageloaderdemo.instagram.service.Item;
import com.theartofdev.fastimageloaderdemo.instagram.service.User;

public final class ItemView extends ViewGroup {

    private final TargetImageView mAvatar;

    private final TargetImageView mImage;

    private final TextView mAuthor;

    public ItemView(Context context) {
        super(context);

        mAvatar = new TargetImageView(context);
        int size = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        mAvatar.setLayoutParams(new LayoutParams(size, size));
        addView(mAvatar);

        Point p = new Point();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);

        mImage = new TargetImageView(context);
        mImage.setLayoutParams(new LayoutParams(p.x, p.x));
        mImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(mImage);

        mAuthor = new TextView(context);
        mAuthor.setSingleLine();
        addView(mAuthor);
    }

    public void setData(Item item) {
        User user = item.user;
        mAvatar.loadImage(user.profile_picture, Specs.INSTA_AVATAR);
        mImage.loadImage(item.images.standard_resolution.url, Specs.INSTA_IMAGE);
        mAuthor.setText(user.full_name != null ? user.full_name : user.username);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int m = getResources().getDimensionPixelSize(R.dimen.margin);

        measureChild(mAvatar, widthMeasureSpec, heightMeasureSpec);
        measureChild(mImage, widthMeasureSpec, heightMeasureSpec);
        measureChild(mAuthor, widthMeasureSpec, heightMeasureSpec);

        mAvatar.layout(m, m, m + mAvatar.getMeasuredWidth(), m + mAvatar.getMeasuredHeight());
        mImage.layout(0, m + mAvatar.getBottom(), getMeasuredWidth(), m + mAvatar.getBottom() + mImage.getMeasuredHeight());

        mAuthor.layout(mAvatar.getRight() + m, mAvatar.getTop(), mAvatar.getRight() + m + mAuthor.getMeasuredWidth(), m + mAuthor.getMeasuredHeight());

        setMeasuredDimension(getMeasuredWidth(), mImage.getBottom() + m);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }
}