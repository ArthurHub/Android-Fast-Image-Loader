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

import com.theartofdev.fastimageloader.impl.ImageRequest;

/**
 * TODO:a add doc
 */
public interface Downloader {

    /**
     * Download
     *
     * @param imageRequest the request to download the image for.
     * @param prefetch if the request is prefetch or actually required now.
     * @param callback The callback to execute on async requests to the downloader
     */
    void downloadAsync(ImageRequest imageRequest, boolean prefetch, Callback callback);

    //region: Inner class: Callback

    /**
     * Callback for getting cached image.
     */
    public static interface Callback {

        /**
         * Callback for getting cached image, if not cached will have null.
         */
        public void loadImageDownloaderCallback(ImageRequest imageRequest, boolean downloaded, boolean canceled);
    }
    //endregion
}
