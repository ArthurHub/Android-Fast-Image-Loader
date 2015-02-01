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
     * @param spec the spec of the image load request
     * @param from from where the image was loaded (MEMORY/DISK/NETWORK)
     * @param successful was the image load successful
     * @param time the time in milliseconds it took from request to finish
     */
    void imageLoadOperation(String url, ImageLoadSpec spec, LoadedFrom from, boolean successful, long time);
}
