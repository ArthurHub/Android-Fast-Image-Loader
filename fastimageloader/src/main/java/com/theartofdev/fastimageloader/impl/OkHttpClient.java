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

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.theartofdev.fastimageloader.HttpClient;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.theartofdev.fastimageloader.HttpClient} using OK HTTP network library.
 */
public class OkHttpClient implements HttpClient {

    //region: Fields and Consts

    /**
     * The actual OK HTTP client used
     */
    private com.squareup.okhttp.OkHttpClient mClient;
    //endregion

    /**
     * Create new OkHttpClient instance and set connect and read timeout to 10 and 15 seconds respectively.
     */
    public OkHttpClient() {
        this(new com.squareup.okhttp.OkHttpClient());
        mClient.setConnectTimeout(10, TimeUnit.SECONDS);
        mClient.setReadTimeout(15, TimeUnit.SECONDS);
    }

    /**
     * Use the given OK HTTP client for all requests.
     *
     * @param client The actual OK HTTP client used
     */
    public OkHttpClient(com.squareup.okhttp.OkHttpClient client) {
        FILUtils.notNull(client, "client");
        mClient = client;
    }

    @Override
    public HttpResponse execute(String uri) {
        try {
            Request httpRequest = new Request.Builder().url(uri).build();
            Response httpResponse = mClient.newCall(httpRequest).execute();
            return new OkHttpResponse(httpResponse);
        } catch (IOException e) {
            throw new RuntimeException("HTTP execute failed", e);
        }
    }

    //region: Inner class: OkHttpResponse

    private static final class OkHttpResponse implements HttpResponse {

        private final Response mResponse;

        public OkHttpResponse(Response response) {
            mResponse = response;
        }

        @Override
        public int getCode() {
            return mResponse.code();
        }

        @Override
        public String getErrorMessage() {
            return mResponse.message();
        }

        @Override
        public long getContentLength() {
            return FILUtils.parseLong(mResponse.header("content-length"), -1);
        }

        @Override
        public InputStream getBodyStream() {
            return mResponse.body().byteStream();
        }
    }
    //endregion
}

