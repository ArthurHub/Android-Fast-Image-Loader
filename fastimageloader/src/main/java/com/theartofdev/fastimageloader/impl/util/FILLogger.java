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

package com.theartofdev.fastimageloader.impl.util;

import android.util.Log;

import com.theartofdev.fastimageloader.LoadedFrom;
import com.theartofdev.fastimageloader.LogAppender;

/**
 * Logger for Fast Image Loader internal use only.
 */
public final class FILLogger {

    /**
     * The tag to use for all logs
     */
    private static final String TAG = "FastImageLoader";

    /**
     * If to write logs to logcat.
     */
    public static boolean mLogcatEnabled = false;

    /**
     * Extensibility appender to write the logs to.
     */
    public static LogAppender mAppender;

    /**
     * The min log level to write logs at, logs below this level are ignored.
     */
    public static int mLogLevel = Log.INFO;

    /**
     * Image load operation complete.
     *
     * @param url the url of the image
     * @param specKey the spec of the image load request
     * @param from from where the image was loaded (MEMORY/DISK/NETWORK)
     * @param successful was the image load successful
     * @param time the time in milliseconds it took from request to finish
     */
    public static void operation(String url, String specKey, LoadedFrom from, boolean successful, long time) {
        if (mLogcatEnabled) {
            String msg = FILUtils.format("Operation: LoadImage [{}] [{}] [{}] [{}]", from, specKey, successful, time);
            Log.println(from == LoadedFrom.MEMORY ? Log.DEBUG : Log.INFO, TAG, msg);
        }
        if (mAppender != null)
            mAppender.imageLoadOperation(url, specKey, from, successful, time);
    }

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
    public static void operation(String url, String specKey, int responseCode, long time, long bytes, Throwable error) {
        if (mLogcatEnabled) {
            String msg = FILUtils.format("Operation: DownloadImage [{}] [{}] [{}] [{}] [{}]", url, specKey, responseCode, bytes, time);
            if (error == null) {
                Log.i(TAG, msg);
            } else {
                Log.e(TAG, msg, error);
            }
        }
        if (mAppender != null)
            mAppender.imageDownloadOperation(url, specKey, responseCode, time, bytes, error);
    }

    public static void debug(String msg) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, msg);
            if (mAppender != null)
                mAppender.log(Log.DEBUG, TAG, msg, null);
        }
    }

    public static void debug(String msg, Object arg1) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, FILUtils.format(msg, arg1));
            if (mAppender != null)
                mAppender.log(Log.DEBUG, TAG, FILUtils.format(msg, arg1), null);
        }
    }

    public static void debug(String msg, Object arg1, Object arg2) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, FILUtils.format(msg, arg1, arg2));
            if (mAppender != null)
                mAppender.log(Log.DEBUG, TAG, FILUtils.format(msg, arg1, arg2), null);
        }
    }

    public static void debug(String msg, Object arg1, Object arg2, Object arg3) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, FILUtils.format(msg, arg1, arg2, arg3));
            if (mAppender != null)
                mAppender.log(Log.DEBUG, TAG, FILUtils.format(msg, arg1, arg2, arg3), null);
        }
    }

    public static void debug(String msg, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, FILUtils.format(msg, arg1, arg2, arg3, arg4));
            if (mAppender != null)
                mAppender.log(Log.DEBUG, TAG, FILUtils.format(msg, arg1, arg2, arg3, arg4), null);
        }
    }

    public static void info(String msg) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, msg);
            if (mAppender != null)
                mAppender.log(Log.INFO, TAG, msg, null);
        }
    }

    public static void info(String msg, Object arg1) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, FILUtils.format(msg, arg1));
            if (mAppender != null)
                mAppender.log(Log.INFO, TAG, FILUtils.format(msg, arg1), null);
        }
    }

    public static void info(String msg, Object arg1, Object arg2) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, FILUtils.format(msg, arg1, arg2));
            if (mAppender != null)
                mAppender.log(Log.INFO, TAG, FILUtils.format(msg, arg1, arg2), null);
        }
    }

    public static void info(String msg, Object arg1, Object arg2, Object arg3) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, FILUtils.format(msg, arg1, arg2, arg3));
            if (mAppender != null)
                mAppender.log(Log.INFO, TAG, FILUtils.format(msg, arg1, arg2, arg3), null);
        }
    }

    public static void info(String msg, Object... args) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, FILUtils.format(msg, args));
            if (mAppender != null)
                mAppender.log(Log.INFO, TAG, FILUtils.format(msg, args), null);
        }
    }

    public static void warn(String msg, Object... args) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, FILUtils.format(msg, args));
            if (mAppender != null)
                mAppender.log(Log.WARN, TAG, FILUtils.format(msg, args), null);
        }
    }

    public static void warn(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, FILUtils.format(msg, args), e);
            if (mAppender != null)
                mAppender.log(Log.WARN, TAG, FILUtils.format(msg, args), e);
        }
    }

    public static void error(String msg) {
        if (mLogLevel <= Log.ERROR) {
            if (mLogcatEnabled)
                Log.e(TAG, msg);
            if (mAppender != null)
                mAppender.log(Log.ERROR, TAG, msg, null);
        }
    }

    public static void error(String msg, Object... args) {
        if (mLogLevel <= Log.ERROR) {
            msg = FILUtils.format(msg, args);
            if (mLogcatEnabled)
                Log.e(TAG, msg);
            if (mAppender != null)
                mAppender.log(Log.ERROR, TAG, msg, null);
        }
    }

    public static void error(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.ERROR) {
            msg = FILUtils.format(msg, args);
            if (mLogcatEnabled)
                Log.e(TAG, msg, e);
            if (mAppender != null)
                mAppender.log(Log.ERROR, TAG, msg, e);
        }
    }

    public static void critical(String msg, Object... args) {
        if (mLogLevel <= Log.ASSERT) {
            msg = FILUtils.format(msg, args);
            Exception e = new Exception(msg);
            if (mLogcatEnabled)
                Log.e(TAG, msg, e);
            if (mAppender != null)
                mAppender.log(Log.ASSERT, TAG, msg, e);
        }
    }

    public static void critical(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.ASSERT) {
            msg = FILUtils.format(msg, args);
            if (mLogcatEnabled)
                Log.e(TAG, msg, e);
            if (mAppender != null)
                mAppender.log(Log.ASSERT, TAG, msg, e);
        }
    }
}

