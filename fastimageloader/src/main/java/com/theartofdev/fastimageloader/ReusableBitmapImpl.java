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

package com.theartofdev.fastimageloader;

import android.graphics.Bitmap;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrap Bitmap object to know if it is currently in use.
 */
final class ReusableBitmapImpl implements ReusableBitmap {

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
    private AtomicInteger mInUse = new AtomicInteger();

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
    public ReusableBitmapImpl(Bitmap bitmap, ImageLoadSpec spec) {
        Utils.notNull(bitmap, "bitmap");
        Utils.notNull(spec, "spec");
        mBitmap = bitmap;
        mSpec = spec;
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    @Override
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

    @Override
    public boolean isInUse() {
        return mInUse.get() > 0 || mInLoadUse;
    }

    @Override
    public void incrementInUse() {
        mInUse.incrementAndGet();
        mInLoadUse = false;
    }

    @Override
    public void decrementInUse() {
        mInUse.decrementAndGet();
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
        ULogger.debug("Close recycle bitmap [{}]", this);
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
                ", mInUse=" + mInUse.get() +
                ", mInLoadUse=" + mInLoadUse +
                ", mRecycleCount=" + mRecycleCount +
                ", mClosed=" + mClosed +
                '}';
    }
}

