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

package com.theartofdev.fastimageloader.target;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.theartofdev.fastimageloader.LoadState;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * TODO:a add doc
 */
public class TargetAvatarImageView extends TargetImageView {

    //region: Fields and Consts

    /**
     * Full AuthorName
     */
    protected String mName;

    /**
     * Acronyms, 2 letters will be written as placeholder
     */
    protected String mAcronyms;

    /**
     * Calculate background color, by 'AuthorName' hasecode % 30
     */
    protected static Paint mBackPaint;

    /**
     * WHITE color painter for text
     */
    protected static Paint mTextPaint;
    //endregion

    public TargetAvatarImageView(Context context) {
        super(context);
        setRounded(true);
    }

    public TargetAvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRounded(true);
    }

    public TargetAvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setRounded(true);
    }

    /**
     * See: {@link #loadAvatar(String, String, String, String)}
     */
    public void loadAvatar(String source, String name, String specKey) {
        loadAvatar(source, name, specKey, null);
    }

    /**
     * Load avatar image from the given source, use the given name for placeholder while loading
     * or avatar load failed.
     *
     * @param source the avatar source URL to load from
     * @param name the user name to use while avatar is loading or failed
     * @param specKey the spec to load the image by
     * @param altSpecKey optional: the spec to use for memory cached image in case the primary is not found.
     */
    public void loadAvatar(String source, String name, String specKey, String altSpecKey) {
        if (!TextUtils.equals(mName, name)) {
            mName = TextUtils.isEmpty(name) ? "UU" : name;
            mAcronyms = null;
        }
        loadImage(source, specKey, altSpecKey, false);
    }

    //region: Private methods

    @Override
    protected void drawPlaceholder(Canvas canvas, LoadState loadState) {
        if (!TextUtils.isEmpty(mName)) {
            if (mAcronyms == null) {
                init();
            }

            FILUtils.rectF.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            if (isRounded()) {
                canvas.drawRoundRect(FILUtils.rectF, getWidth(), getHeight(), mBackPaint);
            } else {
                canvas.drawRect(FILUtils.rectF, mBackPaint);
            }

            mTextPaint.setTextSize(getWidth() / 1.8f);
            int xPos = canvas.getWidth() / 2;
            int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(mAcronyms, xPos, yPos, mTextPaint);
        }
    }

    /**
     * Init the user acronym and the paint object required.
     */
    protected void init() {
        if (mBackPaint == null) {
            mBackPaint = new Paint();
            mBackPaint.setColor(Color.LTGRAY);
            mBackPaint.setAntiAlias(true);
        }

        if (mTextPaint == null) {
            mTextPaint = new Paint();
            mTextPaint.setColor(Color.DKGRAY);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        }

        int idx = mName.indexOf(' ');
        if (idx < 0) {
            idx = 0;
            while (idx + 1 < mName.length() && Character.isLowerCase(mName.charAt(idx + 1))) {
                idx++;
            }
        }
        mAcronyms = idx > -1 && idx + 1 < mName.length()
                ? String.format("%c%c", mName.charAt(0), mName.charAt(idx + 1))
                : mName.substring(0, 1);
    }
    //endregion
}