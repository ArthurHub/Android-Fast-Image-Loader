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

/**
 * Appender to use to send logs to, allow client to log this library inner logs into custom framework.
 */
public interface LogAppender {

    /**
     * Write given log entry.
     *
     * @param level the log level ({@link android.util.Log#DEBUG}, {@link android.util.Log#INFO}, etc.)
     * @param tag the log tag string
     * @param message the log message
     * @param error optional: the error logged
     */
    void log(int level, String tag, String message, Throwable error);

    /**
     * Image load operation complete.
     *
     * @param url the url of the image
     * @param specKey the spec of the image load request
     * @param from from where the image was loaded (MEMORY/DISK/NETWORK)
     * @param successful was the image load successful
     * @param time the time in milliseconds it took from request to finish
     */
    void imageLoadOperation(String url, String specKey, LoadedFrom from, boolean successful, long time);

    /**
     * Image download operation complete.
     *
     * @param url the url of the image
     * @param specKey the spec of the image load request
     * @param responseCode the response code of the download web request
     * @param time the time in milliseconds it took to download the image
     * @param bytes the number of bytes received if download was successful
     * @param error optional: if download failed will contain the error
     */
    void imageDownloadOperation(String url, String specKey, int responseCode, long time, long bytes, Throwable error);
}
