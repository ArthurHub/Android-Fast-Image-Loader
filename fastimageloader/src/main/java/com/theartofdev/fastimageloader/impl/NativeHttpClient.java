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

import com.theartofdev.fastimageloader.HttpClient;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link com.theartofdev.fastimageloader.HttpClient} using native Android HttpURLConnection.
 */
public class NativeHttpClient implements HttpClient {

    /**
     * the maximum time in milliseconds to wait while connecting
     */
    private final int mConnectTimeout;

    /**
     * the maximum time to wait for an input stream read to complete before giving up
     */
    private final int mReadTimeout;

    /**
     * Create new instance and set connect and read timeout to 10 and 15 seconds respectively.
     */
    public NativeHttpClient() {
        this(10000, 15000);
    }

    /**
     * @param connectTimeout the maximum time in milliseconds to wait while connecting
     * @param readTimeout the maximum time to wait for an input stream read to complete before giving up
     */
    public NativeHttpClient(int connectTimeout, int readTimeout) {
        mConnectTimeout = connectTimeout;
        mReadTimeout = readTimeout;
    }

    @Override
    public HttpClient.HttpResponse execute(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mReadTimeout);
            connection.connect();
            return new NativeHttpResponse(connection.getResponseCode(), connection);
        } catch (IOException e) {
            throw new RuntimeException("HTTP execute failed", e);
        }
    }

    //region: Inner class: OkHttpResponse

    private static final class NativeHttpResponse implements HttpResponse {

        private int mCode;

        private final HttpURLConnection mConnection;

        public NativeHttpResponse(int code, HttpURLConnection connection) {
            mCode = code;
            mConnection = connection;
        }

        @Override
        public int getCode() {
            return mCode;
        }

        @Override
        public String getErrorMessage() {
            try {
                return mConnection.getResponseMessage();
            } catch (IOException e) {
                throw new RuntimeException("HTTP execute failed", e);
            }
        }

        @Override
        public long getContentLength() {
            return FILUtils.parseLong(mConnection.getHeaderField("content-length"), -1);
        }

        @Override
        public InputStream getBodyStream() {
            try {
                return mConnection.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException("HTTP execute failed", e);
            }
        }
    }
    //endregion
}

