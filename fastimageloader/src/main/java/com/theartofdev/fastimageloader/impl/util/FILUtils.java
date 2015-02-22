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

import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;

/**
 * General utility methods for Fast Image Loader internal use only.
 */
public final class FILUtils {

    /**
     * Reuse Rect object
     */
    public static final Rect rect = new Rect();

    /**
     * Reuse RectF object
     */
    public static final RectF rectF = new RectF();

    /**
     * The ID of the main thread of the application, used to know if currently execution on main thread.
     */
    public static long MainThreadId;

    /**
     * Validate that the current executing thread is the main Android thread for the app.
     *
     * @throws RuntimeException current thread is not main thread.
     */
    public static void verifyOnMainThread() {
        if (!isOnMainThread())
            throw new RuntimeException("Access to this method must be on main thread only");
    }

    /**
     * Returns true if the current executing thread is the main Android app thread, false otherwise.
     */
    public static boolean isOnMainThread() {
        return Thread.currentThread().getId() == MainThreadId;
    }

    /**
     * Validate given argument isn't null.
     *
     * @param arg argument to validate
     * @param argName name of the argument to show in error message
     * @throws IllegalArgumentException
     */
    public static void notNull(Object arg, String argName) {
        if (arg == null) {
            throw new IllegalArgumentException("argument is null: " + argName);
        }
    }

    /**
     * Validate given string argument isn't null or empty string.
     *
     * @param arg argument to validate
     * @param argName name of the argument to show in error message
     * @throws IllegalArgumentException
     */
    public static void notNullOrEmpty(String arg, String argName) {
        if (arg == null || arg.length() < 1) {
            throw new IllegalArgumentException("argument is null: " + argName);
        }
    }

    /**
     * Validate given array argument isn't null or empty string.
     *
     * @param arg argument to validate
     * @param argName name of the argument to show in error message
     * @throws IllegalArgumentException
     */
    public static <T> void notNullOrEmpty(T[] arg, String argName) {
        if (arg == null || arg.length < 1) {
            throw new IllegalArgumentException("argument is null: " + argName);
        }
    }

    /**
     * Validate given collection argument isn't null or empty string.
     *
     * @param arg argument to validate
     * @param argName name of the argument to show in error message
     * @throws IllegalArgumentException
     */
    public static <T> void notNullOrEmpty(Collection<T> arg, String argName) {
        if (arg == null || arg.size() < 1) {
            throw new IllegalArgumentException("argument is null: " + argName);
        }
    }

    /**
     * Safe parse the given string to long value.
     */
    public static long parseLong(String value, long defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    /**
     * combine two path into a single path with File.separator.
     * Handle all cases where the separator already exists.
     */
    public static String pathCombine(String path1, String path2) {
        if (path2 == null)
            return path1;
        else if (path1 == null)
            return path2;

        path1 = path1.trim();
        path2 = path2.trim();
        if (path1.endsWith(File.separator)) {
            if (path2.startsWith(File.separator))
                return path1 + path2.substring(1);
            else
                return path1 + path2;
        } else {
            if (path2.startsWith(File.separator))
                return path1 + path2;
            else
                return path1 + File.separator + path2;
        }
    }

    /**
     * Close the given closeable object (Stream) in a safe way: check if it is null and catch-log
     * exception thrown.
     *
     * @param closeable the closable object to close
     */
    public static void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                FILLogger.warn("Failed to close closable object [{}]", e, closeable);
            }
        }
    }

    /**
     * Delete the given file in a safe way: check if it is null and catch-log exception thrown.
     *
     * @param file the file to delete
     */
    public static void deleteSafe(File file) {
        if (file != null) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            } catch (Throwable e) {
                FILLogger.warn("Failed to delete file [{}]", e, file);
            }
        }
    }

    /**
     * Format the given <i>format</i> string by replacing {} placeholders with given arguments.
     */
    public static String format(String format, Object arg1) {
        return replacePlaceHolder(format, arg1);
    }

    /**
     * Format the given <i>format</i> string by replacing {} placeholders with given arguments.
     */
    public static String format(String format, Object arg1, Object arg2) {
        format = replacePlaceHolder(format, arg1);
        return replacePlaceHolder(format, arg2);
    }

    /**
     * Format the given <i>format</i> string by replacing {} placeholders with given arguments.
     */
    public static String format(String format, Object arg1, Object arg2, Object arg3) {
        format = replacePlaceHolder(format, arg1);
        format = replacePlaceHolder(format, arg2);
        return replacePlaceHolder(format, arg3);
    }

    /**
     * Format the given <i>format</i> string by replacing {} placeholders with given arguments.
     */
    public static String format(String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        format = replacePlaceHolder(format, arg1);
        format = replacePlaceHolder(format, arg2);
        format = replacePlaceHolder(format, arg3);
        return replacePlaceHolder(format, arg4);
    }

    /**
     * Format the given <i>format</i> string by replacing {} placeholders with given arguments.
     */
    public static String format(String format, Object... args) {
        for (Object arg : args) {
            format = replacePlaceHolder(format, arg);
        }
        return format;
    }

    /**
     * Replace the first occurrence of {} placeholder in <i>format</i> string by <i>arg1</i> toString() value.
     */
    private static String replacePlaceHolder(String format, Object arg1) {
        int idx = format.indexOf("{}");
        if (idx > -1) {
            format = format.substring(0, idx) + arg1 + format.substring(idx + 2);
        }
        return format;
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

}