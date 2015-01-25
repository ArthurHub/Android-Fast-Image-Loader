package com.theartofdev.fastimageloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Create a placeholder image that build from 2 letters.<br/>
 * the author first name, first letter and the author last name, first letter.<br/>
 * EX : John Doe -> JD
 * <p/>
 * text color WHITE, and calculated background color (hash-code of author name % 30).
 */
public final class AvatarImageView extends TargetImageView {

    //region: Fields and Consts

    /**
     * Rect to draw circle
     */
    private final RectF mRect = new RectF();

    /**
     * Full AuthorName
     */
    private String mAuthorName;

    /**
     * Acronyms, 2 letters will be written as placeholder
     */
    private String mAcronyms;

    /**
     *
     */
    private Paint mPadPaint;

    /**
     * Calculate background color, by 'AuthorName' hasecode % 30
     */
    private Paint mBackgroundPaint;

    /**
     * WHITE color painter for text
     */
    private Paint mTextPaint;

    /**
     * color to be used in avatar padding border.
     */
    private int mPaddingBackgroundColor;
    //endregion

    @SuppressWarnings("UnusedDeclaration")
    public AvatarImageView(Context context) {
        super(context);
        setRounded(true);
    }

    @SuppressWarnings("UnusedDeclaration")
    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRounded(true);
    }

    /**
     * Set color to be used in avatar padding border.
     */
    public void setPaddingBackgroundColorRes(int colorRes) {
        mPaddingBackgroundColor = getResources().getColor(colorRes);
    }

    /**
     * See: {@link #loadAvatar(String, String, ImageLoadSpec, ImageLoadSpec)}
     */
    public void loadAvatar(String source, String name, ImageLoadSpec spec) {
        loadAvatar(source, name, spec, null);
    }

    /**
     * Load avatar image from the given source, use the given name for placeholder while loading
     * or avatar load failed.
     *
     * @param source the avatar source URL to load from
     * @param name the user name to use while avatar is loading or failed
     * @param spec the spec to load the image by
     * @param altSpec optional: the spec to use for memory cached image in case the primary is not found.
     */
    public void loadAvatar(String source, String name, ImageLoadSpec spec, ImageLoadSpec altSpec) {
        if (!TextUtils.equals(mAuthorName, name)) {
            mAuthorName = TextUtils.isEmpty(name) ? "UU" : name;
            mAcronyms = null;
        }
        loadImage(source, spec, altSpec, false);
    }

    //region: Private methods

    /**
     * Init the user acronym and the paint object required.
     */
    private void init() {

        mBackgroundPaint = new Paint();

        //calculate center x,y for the text
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getLayoutParams().width / (float) 2.5);

        mAcronyms = String.format("%c%c", mAuthorName.charAt(0), mAuthorName.charAt(mAuthorName.indexOf(' ') + 1));

        //change background color
        int colorNumber = Math.abs(mAuthorName.hashCode()) % 30;
        int resId = getResources().getIdentifier("color_" + colorNumber, "color", this.getContext().getPackageName());
        int colorCode = getResources().getColor(resId);
        mBackgroundPaint.setColor(colorCode);
        mBackgroundPaint.setAntiAlias(true);
    }

    @Override
    public void draw(@SuppressWarnings("NullableProblems") Canvas canvas) {
        if (getPaddingTop() > 0) {
            if (mPadPaint == null) {
                mPadPaint = new Paint();
                mPadPaint.setAntiAlias(true);
                mPadPaint.setColor(mPaddingBackgroundColor);
            }
            mRect.set(0, 0, getWidth(), getHeight());
            if (isRounded()) {
                canvas.drawRoundRect(mRect, getWidth(), getHeight(), mPadPaint);
            } else {
                canvas.drawRect(mRect, mPadPaint);
            }
        }
        super.draw(canvas);
    }

    @Override
    public void onDraw(@SuppressWarnings("NullableProblems") Canvas canvas) {
        if ((getDrawable() == null || (getDrawable() instanceof ImageDrawable && ((ImageDrawable) getDrawable()).isAnimating())) && !TextUtils.isEmpty(mAuthorName)) {
            if (mAcronyms == null) {
                init();
            }

            mRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            if (isRounded()) {
                canvas.drawRoundRect(mRect, getWidth(), getHeight(), mBackgroundPaint);
            } else {
                canvas.drawRect(mRect, mBackgroundPaint);
            }

            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(mAcronyms, xPos, yPos, mTextPaint);
        }
        super.onDraw(canvas);
    }
    //endregion
}