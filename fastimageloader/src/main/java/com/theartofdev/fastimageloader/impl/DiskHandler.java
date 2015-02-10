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
import com.theartofdev.fastimageloader.MemoryPool;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.impl.util.FILLogger;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.File;

/**
 * Handler for loading image bitmap object from file on disk.<br/>
 * Load the bitmap so it can be re-used.<br/>
 */
public class DiskHandler {

    //region: Fields and Consts

    /**
     * Used to reuse bitmaps on image loading from disk
     */
    private final BitmapFactory.Options mOptions;

    /**
     * the folder that the image cached on disk are located
     */
    private File mCacheFolder;
    //endregion

    public DiskHandler(File cacheFolder) {
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
    public void decodeImageObject(MemoryPool memoryPool, ImageRequest imageRequest, File file, ImageLoadSpec spec) {
        ReusableBitmap poolBitmap = memoryPool.getUnused(spec);

        FILLogger.debug("Decode image from disk... [{}] [{}]", imageRequest, poolBitmap);
        ReusableBitmap decodedBitmap = decodeImageObject(file, spec, poolBitmap);

        if (decodedBitmap != null) {
            imageRequest.setBitmap(decodedBitmap);
        }

        if (poolBitmap != null && poolBitmap != decodedBitmap) {
            memoryPool.returnUnused(poolBitmap);
        }
    }

    /**
     * Load image from disk file on the current thread and set it in the image request object.
     */
    protected ReusableBitmap decodeImageObject(File file, ImageLoadSpec spec, ReusableBitmap poolBitmap) {
        try {
            mOptions.inBitmap = poolBitmap != null ? poolBitmap.getBitmap() : null;
            mOptions.inPreferredConfig = spec.getPixelConfig();

            Bitmap rawBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), mOptions);
            if (rawBitmap != null) {
                if (poolBitmap != null && poolBitmap.getBitmap() == rawBitmap) {
                    // successful load of image into reusable bitmap
                    return poolBitmap;
                }

                FILLogger.debug("Create new reusable bitmap... [{}]", spec);
                return new ReusableBitmap(rawBitmap, spec);
            } else {
                FILLogger.critical("Failed to load image from cache [{}] [{}] [{}]", file, spec, poolBitmap);
                return null;
            }
        } catch (Throwable e) {
            FILLogger.warn("Failed to load disk cached image [{}] [{}] [{}]", e, file, spec, poolBitmap);
            return null;
        }
    }
}

