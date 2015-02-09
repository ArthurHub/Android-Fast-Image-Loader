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
import android.graphics.BitmapFactory;

import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.File;

/**
 * Handler for loading image bitmap object from file on disk.<br/>
 * Load the bitmap so it can be re-used.<br/>
 */
final class DiskHandler {

    //region: Fields and Consts

    /**
     * Handler for bitmap recycling, holds recycled bitmaps by dimension key to be used later.
     */
    private final MemoryCachePool mBitmapRecycler;

    /**
     * Used to reuse bitmaps on image loading from disk
     */
    private final BitmapFactory.Options mOptions;

    /**
     * the folder that the image cached on disk are located
     */
    private File mCacheFolder;
    //endregion

    DiskHandler(MemoryCachePool bitmapRecycler, File cacheFolder) {
        mBitmapRecycler = bitmapRecycler;
        mCacheFolder = cacheFolder;

        mOptions = new BitmapFactory.Options();
        mOptions.inSampleSize = 1;
        mOptions.inMutable = true;
    }

    /**
     * the folder that the image cached on disk are located
     */
    public File getCacheFolder() {
        return mCacheFolder;
    }

    /**
     * Gets the representation of the online uri on the local disk.
     *
     * @param uri The online image uri
     * @return The path of the file on the disk
     */
    public File getCacheFile(String uri, ImageLoadSpec spec) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }
        String name = FILUtils.format("{}_{}_{}_{}",
                Integer.toHexString(uri.substring(0, lastSlash).hashCode()),
                Integer.toHexString(uri.substring(lastSlash + 1).hashCode()),
                uri.substring(Math.max(lastSlash + 1, uri.length() - 10)),
                spec.getKey());
        return new File(FILUtils.pathCombine(mCacheFolder.getAbsolutePath(), name));
    }

    /**
     * Load image from disk file on the current thread and set it in the image request object.
     */
    public void decodeImageObject(ImageRequest imageRequest, File file, ImageLoadSpec spec) {
        try {
            //noinspection ResultOfMethodCallIgnored
            file.setLastModified(System.currentTimeMillis());

            ReusableBitmapImpl bitmap = mBitmapRecycler.getUnused(spec);
            mOptions.inBitmap = bitmap != null ? bitmap.getBitmap() : null;
            mOptions.inPreferredConfig = spec.getPixelConfig();

            FILLogger.debug("Decode image from disk... [{}] [{}]", imageRequest, bitmap);
            Bitmap rawBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), mOptions);

            // if cached bitmap was used the raw image will be null
            if (rawBitmap != null) {
                if (bitmap != null) {
                    if (rawBitmap != bitmap.getBitmap()) {
                        // failed to use recycled bitmap, return it
                        mBitmapRecycler.returnUnused(bitmap);
                        bitmap = null;
                    } else {
                        imageRequest.setBitmap(bitmap);
                    }
                }
                if (bitmap == null) {
                    // create cached bitmap wrapper with new raw bitmap
                    bitmap = new ReusableBitmapImpl(rawBitmap, spec);
                    imageRequest.setBitmap(bitmap);
                }
            } else {
                FILLogger.critical("Failed to load image from cache [{}] [{}]", imageRequest, bitmap);
                if (bitmap != null) {
                    mBitmapRecycler.returnUnused(bitmap);
                    bitmap = null;
                }
            }

            if (bitmap != null) {
                bitmap.setUrl(imageRequest.getUri());
            }
        } catch (Throwable e) {
            FILLogger.warn("Failed to load disk cached image [{}]", e, imageRequest);
        }
    }
}

