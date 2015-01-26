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

package com.theartofdev.fastimageloader.enhancer;

import com.theartofdev.fastimageloader.ImageLoadSpec;

import java.text.MessageFormat;

/**
 * URL enhancer for thumbor (http://thumbor.org/) service.<br/>
 * Add image load specification into the path of the image URL.<br/>
 * Using Thumbor service URI to build new URI with the image URI as suffix.
 */
public class ThumborUriEnhancer implements ImageServiceUriEnhancer {

    //region: Fields and Consts

    /**
     * the thumbor base URI
     */
    private final String mBaseUri;
    //endregion

    /**
     * @param baseUri the thumbor base URI
     */
    public ThumborUriEnhancer(String baseUri) {
        if (baseUri == null || baseUri.length() < 1)
            throw new IllegalArgumentException("argument is null: " + baseUri);
        mBaseUri = baseUri;
    }

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        return MessageFormat.format("{0}/unsafe/{1}x{2}/filters:fill(fff,true):format(jpeg)/{3}",
                mBaseUri,
                spec.getWidth(),
                spec.getHeight(),
                url);
    }
}