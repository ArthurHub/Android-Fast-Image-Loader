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

import java.io.InputStream;

/**
 * Define a client to be used to download requested images.<br>
 * The client must be thread-safe as a single instance will be used for multiple requests
 * on multiple threads.
 */
public interface HttpClient {

    /**
     * Execute image download for the given URI.<br>
     * Invokes the request immediately, and blocks until the response can be processed or is in error.
     *
     * @param uri the URI of the image to download.
     * @return The response of the execution with the result data
     */
    HttpResponse execute(String uri);

    /**
     * The response returned from client execution.
     */
    public interface HttpResponse {

        /**
         * The HTTP status code.
         */
        int getCode();

        /**
         * The HTTP status message or null if it is unknown.
         */
        String getErrorMessage();

        /**
         * The content-length of the response body.
         */
        long getContentLength();

        /**
         * Stream of the HTTP response body.
         */
        InputStream getBodyStream();
    }
}
