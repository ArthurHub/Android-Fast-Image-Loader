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

import android.util.Log;

/**
 * Logger.
 */
final class Logger {

    /**
     * The tag to use for all logs
     */
    private static final String TAG = "FastImageLoader";

    /**
     * If to write logs to logcat.
     */
    static boolean mLogcatEnabled = false;

    /**
     * The min log level to write logs at, logs below this level are ignored.
     */
    static int mLogLevel = Log.INFO;

    public static void debug(String msg) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, msg);
        }
    }

    public static void debug(String msg, Object arg1) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, Utils.format(msg, arg1));
        }
    }

    public static void debug(String msg, Object arg1, Object arg2) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, Utils.format(msg, arg1, arg2));
        }
    }

    public static void debug(String msg, Object arg1, Object arg2, Object arg3) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, Utils.format(msg, arg1, arg2, arg3));
        }
    }

    public static void debug(String msg, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (mLogLevel <= Log.DEBUG) {
            if (mLogcatEnabled)
                Log.d(TAG, Utils.format(msg, arg1, arg2, arg3, arg4));
        }
    }

    public static void info(String msg) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, msg);
        }
    }

    public static void info(String msg, Object arg1) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, Utils.format(msg, arg1));
        }
    }

    public static void info(String msg, Object arg1, Object arg2) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, Utils.format(msg, arg1, arg2));
        }
    }

    public static void info(String msg, Object arg1, Object arg2, Object arg3) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, Utils.format(msg, arg1, arg2, arg3));
        }
    }

    public static void info(String msg, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, Utils.format(msg, arg1, arg2, arg3, arg4));
        }
    }

    public static void info(String msg, Object... args) {
        if (mLogLevel <= Log.INFO) {
            if (mLogcatEnabled)
                Log.i(TAG, Utils.format(msg, args));
        }
    }

    public static void warn(String msg) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, msg);
        }
    }

    public static void warn(String msg, Object arg1) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, Utils.format(msg, arg1));
        }
    }

    public static void warn(String msg, Object arg1, Object arg2) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, Utils.format(msg, arg1, arg2));
        }
    }

    public static void warn(String msg, Object... args) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, Utils.format(msg, args));
        }
    }

    public static void warn(String msg, Throwable e, Object arg1) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, Utils.format(msg, arg1), e);
        }
    }

    public static void warn(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.WARN) {
            if (mLogcatEnabled)
                Log.w(TAG, Utils.format(msg, args), e);
        }
    }

    public static void error(String msg) {
        if (mLogLevel <= Log.ERROR) {
            if (mLogcatEnabled)
                Log.e(TAG, msg);
        }
    }

    public static void error(String msg, Object... args) {
        if (mLogLevel <= Log.ERROR) {
            if (mLogcatEnabled)
                Log.e(TAG, Utils.format(msg, args));
        }
    }

    public static void error(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.ERROR) {
            if (mLogcatEnabled)
                Log.e(TAG, Utils.format(msg, args), e);
        }
    }

    public static void critical(String msg, Object... args) {
        if (mLogLevel <= Log.ASSERT) {
            String format = Utils.format(msg, args);
            Exception e = new Exception(format);
            if (mLogcatEnabled)
                Log.e(TAG, Utils.format(format, args), e);
        }
    }

    public static void critical(String msg, Throwable e, Object... args) {
        if (mLogLevel <= Log.ASSERT) {
            String format = Utils.format(msg, args);
            if (mLogcatEnabled)
                Log.e(TAG, Utils.format(format, args), e);
        }
    }
}

