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

import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bitmap that can reuse the allocated memory to load a new image into the bitmap.
 */
public class ReusableBitmap {

    //region: Fields and Consts

    /**
     * The actual bitmap
     */
    protected Bitmap mBitmap;

    /**
     * the spec to load the image by
     */
    protected final ImageLoadSpec mSpec;

    /**
     * The URL of the image loaded in the bitmap, used to know that loaded image changed
     */
    protected String mBitmapUrl;

    /**
     * Is the bitmap is currently in use
     */
    protected AtomicInteger mInUse = new AtomicInteger();

    /**
     * Is the bitmap is currently in use by loading, not real use
     */
    protected boolean mInLoadUse;

    /**
     * Is the bitmap is has been released.
     */
    protected boolean mClosed;

    /**
     * The number of times the bitmap has been recycled
     */
    protected int mRecycleCount;
    //endregion

    /**
     * @param bitmap The actual bitmap
     * @param spec the spec to load the image by
     */
    public ReusableBitmap(Bitmap bitmap, ImageLoadSpec spec) {
        FILUtils.notNull(bitmap, "bitmap");
        FILUtils.notNull(spec, "spec");
        mBitmap = bitmap;
        mSpec = spec;
    }

    /**
     * The actual bitmap instance.
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * the URI of the loaded image in the bitmap.<br>
     * Used to know if the target requested image has been changed.<br>
     */
    public String getUri() {
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
     * the spec the loaded image was loaded by.
     */
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    /**
     * Is the bitmap is currently in use and cannot be reused.
     */
    public boolean isInUse() {
        return mInUse.get() > 0 || mInLoadUse;
    }

    /**
     * Increment the bitmap in use count by 1.<br>
     * Affects the {@link #isInUse()} to know if the bitmap can be reused.<br>
     * Critical to call this method correctly.
     */
    public void incrementInUse() {
        mInUse.incrementAndGet();
        mInLoadUse = false;
    }

    /**
     * Decrement the bitmap in use count by 1.<br>
     * Affects the {@link #isInUse()} to know if the bitmap can be reused.<br>
     * Critical to call this method correctly.
     */
    public void decrementInUse() {
        mInUse.decrementAndGet();
    }

    /**
     * Is the bitmap is currently in use by loading, not real use.
     */
    public void setInLoadUse(boolean inLoadUse) {
        mInLoadUse = inLoadUse;
    }

    /**
     * Release the inner bitmap.
     */
    public void close() {
        FILLogger.debug("Close recycle bitmap [{}]", this);
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

