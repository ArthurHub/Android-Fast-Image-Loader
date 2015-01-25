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

package com.theartofdev.fastimageloader.impl;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.RecycleBitmap;

/**
 * Wrap Bitmap object to know if it is currently in use.
 */
final class RecycleBitmapImpl implements RecycleBitmap {

    //region: Fields and Consts

    /**
     * The actual bitmap
     */
    private Bitmap mBitmap;

    /**
     * the spec to load the image by
     */
    private final ImageLoadSpec mSpec;

    /**
     * The URL of the image loaded in the bitmap, used to know that loaded image changed
     */
    private String mBitmapUrl;

    /**
     * Is the bitmap is currently in use
     */
    private int mInUse;

    /**
     * Is the bitmap is currently in use by loading, not real use
     */
    private boolean mInLoadUse;

    /**
     * Is the bitmap is has been released.
     */
    private boolean mClosed;

    /**
     * The number of times the bitmap has been recycled
     */
    private int mRecycleCount;
    //endregion

    /**
     * @param bitmap The actual bitmap
     * @param spec the spec to load the image by
     */
    public RecycleBitmapImpl(Bitmap bitmap, ImageLoadSpec spec) {
        CommonUtils.notNull(bitmap, "bitmap");
        CommonUtils.notNull(spec, "spec");
        mBitmap = bitmap;
        mSpec = spec;
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * the spec to load the image by
     */
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    @Override
    public boolean isMatchingUrl(String url) {
        return TextUtils.equals(mBitmapUrl, url);
    }

    /**
     * the URL of the loaded image in the bitmap.
     */
    public String getUrl() {
        return mBitmapUrl;
    }

    /**
     * the URL of the loaded image in the bitmap.
     */
    public void setUrl(String url) {
        mRecycleCount++;
        mBitmapUrl = url;
    }

    /**
     * Is the bitmap is currently in use
     */
    public boolean isInUse() {
        return mInUse > 0 || mInLoadUse;
    }

    @Override
    public void setInUse(boolean inUse) {
        mInUse += inUse ? 1 : -1;
        if (inUse)
            mInLoadUse = false;
    }

    /**
     * Is the bitmap is currently in use by loading, not real use.
     */
    public boolean isInLoadUse() {
        return mInLoadUse;
    }

    /**
     * Is the bitmap is currently in use by loading, not real use.
     */
    public void setInLoadUse(boolean inLoadUse) {
        mInLoadUse = inLoadUse;
    }

    /**
     * Is the recycle bitmap can be recycled - not in use or in loading use.
     */
    public boolean canBeRecycled() {
        return !isInUse() && !isInLoadUse();
    }

    /**
     * Release the inner bitmap.
     */
    public void close() {
        Logger.debug("Close recycle bitmap [{}]", this);
        mClosed = true;
        mBitmapUrl = null;
        mBitmap.recycle();
        mBitmap = null;
    }

    @Override
    public String toString() {
        return "RecycleBitmap{" +
                "hash=" + hashCode() +
                ", mSpec='" + mSpec + '\'' +
                ", mInUse=" + mInUse +
                ", mInLoadUse=" + mInLoadUse +
                ", mRecycleCount=" + mRecycleCount +
                ", mClosed=" + mClosed +
                '}';
    }
}

