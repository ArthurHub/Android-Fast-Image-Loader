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

import com.theartofdev.fastimageloader.Decoder;
import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.MemoryPool;
import com.theartofdev.fastimageloader.ReusableBitmap;
import com.theartofdev.fastimageloader.impl.util.FILLogger;

import java.io.File;

/**
 * Handler for decoding image object from image File.<br>
 */
public class DecoderImpl implements Decoder {

    //region: Fields and Consts

    /**
     * Used to reuse bitmaps on image loading from disk
     */
    private final BitmapFactory.Options mOptions;
    //endregion

    public DecoderImpl() {
        mOptions = new BitmapFactory.Options();
        mOptions.inSampleSize = 1;
        mOptions.inMutable = true;
    }

    @Override
    public void decode(MemoryPool memoryPool, ImageRequest imageRequest, File file, ImageLoadSpec spec) {
        ReusableBitmap poolBitmap = memoryPool.getUnused(spec);

        FILLogger.debug("Decode image from disk... [{}] [{}]", imageRequest, poolBitmap);
        ReusableBitmap decodedBitmap = decode(file, spec, poolBitmap);

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
    protected ReusableBitmap decode(File file, ImageLoadSpec spec, ReusableBitmap poolBitmap) {
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

