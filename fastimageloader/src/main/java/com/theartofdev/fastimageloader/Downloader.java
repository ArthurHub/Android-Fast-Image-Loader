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
     * Callback for downloading image.
     */
    public static interface Callback {

        /**
         * Callback for downloading image.<br>
         * If the image was downloaded the download flag will be true, it can be false is request
         * was canceled during execution or download has failed. download can be true even if
         * the request was canceled if more than 50% was download before cancellation.
         *
         * @param downloaded if the image was downloaded, maybe false if canceled or failed
         * @param canceled if the request was canceled during execution therefor not loading the image
         */
        public void loadImageDownloaderCallback(ImageRequest imageRequest, boolean downloaded, boolean canceled);
    }
    //endregion
}
