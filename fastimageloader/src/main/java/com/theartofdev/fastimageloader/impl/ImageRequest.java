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

import android.text.TextUtils;

import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.RecycleBitmap;
import com.theartofdev.fastimageloader.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Encapsulate
 */
class ImageRequest {

    //region: Fields and Consts

    /**
     * the URL of the requested image as given
     */
    private final String mUrl;

    /**
     * the URL of the requested image with thumbor parameters
     */
    private final String mThumborUrl;

    /**
     * the spec to load the image by
     */
    private final ImageLoadSpec mSpec;

    /**
     * the file of the image in the disk
     */
    private final File mFile;

    /**
     * the size of the file in the disk cache
     */
    private long mFileSize = -1;

    /**
     * the loaded image bitmap
     */
    private RecycleBitmapImpl mBitmap;

    /**
     * the target to load the image into
     */
    private List<Target> mTargets = new ArrayList<>(3);
    //endregion

    /**
     * @param target the target to load the image into
     * @param url the URL of the requested image as given
     * @param thumborUrl the URL of the requested image with thumbor parameters
     * @param spec the dimension key used to load the image in specific size
     * @param file the path of the image in the disk
     */
    ImageRequest(Target target, String url, String thumborUrl, ImageLoadSpec spec, File file) {
        mTargets.add(target);
        mUrl = url;
        mThumborUrl = thumborUrl;
        mSpec = spec;
        mFile = file;
    }

    /**
     * the URL of the requested image as given
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * the URL of the requested image with thumbor parameters
     */
    public String getThumborUrl() {
        return mThumborUrl;
    }

    /**
     * the spec to load the image by
     */
    public ImageLoadSpec getSpec() {
        return mSpec;
    }

    /**
     * the file of the image in the disk
     */
    public File getFile() {
        return mFile;
    }

    /**
     * the size of the file in the disk
     */
    public long getFileSize() {
        return mFileSize;
    }

    /**
     * the size of the file in the disk
     */
    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    /**
     * the loaded image bitmap
     */
    public RecycleBitmapImpl getBitmap() {
        return mBitmap;
    }

    /**
     * the loaded image bitmap
     */
    public void setBitmap(RecycleBitmapImpl bitmap) {
        if (mBitmap != null)
            mBitmap.setInLoadUse(false);
        mBitmap = bitmap;
        if (mBitmap != null)
            mBitmap.setInLoadUse(true);
    }

    /**
     * the target to load the image into
     */
    public Collection<Target> getValidTargets() {
        filterValidTargets();
        return mTargets;
    }

    /**
     * Is the loading of the requested image is still valid or was it canceled.<br/>
     */
    public boolean isValid() {
        filterValidTargets();
        return mTargets.size() > 0;
    }

    /**
     * Add another target to the request.
     */
    public void addTarget(Target target) {
        mTargets.add(target);
    }

    private void filterValidTargets() {
        for (int i = mTargets.size() - 1; i >= 0; i--) {
            boolean isValid = TextUtils.equals(mTargets.get(i).getUrl(), mUrl);
            if (!isValid) {
                mTargets.remove(i);
            }
        }
    }

    @Override
    public String toString() {
        return "ImageRequest{" +
                "mUrl='" + mUrl + '\'' +
                ", mThumborUrl='" + mThumborUrl + '\'' +
                ", mSpec='" + mSpec + '\'' +
                ", mFile='" + mFile + '\'' +
                ", mFileSize=" + mFileSize +
                ", mBitmap=" + mBitmap +
                ", mTargets=" + mTargets.size() +
                ", isValid=" + isValid() +
                '}';
    }
}

