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

import com.theartofdev.fastimageloader.TargetAvatarImageView;
import com.theartofdev.fastimageloader.TargetImageView;
import com.theartofdev.fastimageloaderdemo.R;
import com.theartofdev.fastimageloaderdemo.Specs;
import com.theartofdev.fastimageloaderdemo.instagram.service.Item;
import com.theartofdev.fastimageloaderdemo.instagram.service.User;

public final class ItemView extends ViewGroup {

    private final TargetAvatarImageView mAvatar;

    private final TargetImageView mImage;

    private final TextView mAuthor;

    public ItemView(Context context) {
        super(context);

        mAvatar = new TargetAvatarImageView(context);
        int size = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        mAvatar.setLayoutParams(new LayoutParams(size, size));
        mAvatar.setRounded(true);
        addView(mAvatar);

        Point p = new Point();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);

        mImage = new TargetImageView(context);
        mImage.setLayoutParams(new LayoutParams(p.x, p.x));
        mImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImage.setPlaceholder(getResources().getDrawable(R.drawable.pattern_repeat));
        addView(mImage);

        mAuthor = new TextView(context);
        mAuthor.setSingleLine();
        addView(mAuthor);
    }

    public void setData(Item item) {
        User user = item.user;
        String userName = user.full_name != null ? user.full_name : user.username;
        mAvatar.loadAvatar(user.profile_picture, userName, Specs.INSTA_AVATAR);
        mImage.loadImage(item.images.standard_resolution.url, Specs.INSTA_IMAGE);
        mAuthor.setText(userName);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int m = getResources().getDimensionPixelSize(R.dimen.margin);

        measureChild(mAvatar, widthMeasureSpec, heightMeasureSpec);
        measureChild(mImage, widthMeasureSpec, heightMeasureSpec);
        measureChild(mAuthor, widthMeasureSpec, heightMeasureSpec);

        mAvatar.layout(2 * m, 2 * m, 2 * m + mAvatar.getMeasuredWidth(), 2 * m + mAvatar.getMeasuredHeight());
        mImage.layout(0, 2 * m + mAvatar.getBottom(), getMeasuredWidth(), 2 * m + mAvatar.getBottom() + mImage.getMeasuredHeight());

        int left = mAvatar.getRight() + 2 * m;
        int top = mAvatar.getTop() + (mAvatar.getHeight() - mAuthor.getMeasuredHeight()) / 2 - m;
        mAuthor.layout(left, top, left + mAuthor.getMeasuredWidth(), top + mAuthor.getMeasuredHeight());

        setMeasuredDimension(getMeasuredWidth(), mImage.getBottom() + 2 * m);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }
}