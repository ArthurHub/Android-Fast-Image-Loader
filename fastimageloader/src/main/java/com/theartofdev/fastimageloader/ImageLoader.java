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
import android.graphics.BitmapFactory;

/**
 * Handler for loading image bitmap object from file on disk.<br/>
 * Load the bitmap so it can be re-used.<br/>
 */
final class ImageLoader {

    //region: Fields and Consts

    /**
     * Handler for bitmap recycling, holds recycled bitmaps by dimension key to be used later.
     */
    private final ImageMemoryCache mBitmapRecycler;

    /**
     * Used to reuse bitmaps on image loading from disk
     */
    private final BitmapFactory.Options mOptions;
    //endregion

    ImageLoader(ImageMemoryCache bitmapRecycler) {
        mBitmapRecycler = bitmapRecycler;

        mOptions = new BitmapFactory.Options();
        mOptions.inSampleSize = 1;
        mOptions.inMutable = true;
    }

    /**
     * Load image from disk file on the current thread and set it in the image request object.
     */
    public void loadImageObject(final ImageRequest imageRequest) {
        try {
            //noinspection ResultOfMethodCallIgnored
            imageRequest.getFile().setLastModified(System.currentTimeMillis());

            RecycleBitmapImpl bitmap = mBitmapRecycler.getUnused(imageRequest.getSpec());
            mOptions.inBitmap = bitmap != null ? bitmap.getBitmap() : null;
            mOptions.inPreferredConfig = imageRequest.getSpec().getPixelConfig();

            // load image from disk
            Bitmap rawBitmap = BitmapFactory.decodeFile(imageRequest.getFile().getAbsolutePath(), mOptions);

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
                    bitmap = new RecycleBitmapImpl(rawBitmap, imageRequest.getSpec());
                    imageRequest.setBitmap(bitmap);
                }
            } else {
                Logger.critical("Failed to load image from cache [{}] [{}]", imageRequest, bitmap);
                if (bitmap != null) {
                    mBitmapRecycler.returnUnused(bitmap);
                    bitmap = null;
                }
            }

            if (bitmap != null) {
                bitmap.setUrl(imageRequest.getUrl());
            }
        } catch (Throwable e) {
            Logger.warn("Failed to load disk cached image [{}]", e, imageRequest);
        }
    }
}

