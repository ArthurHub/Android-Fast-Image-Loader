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
        baseUri = baseUri.trim();
        if (baseUri.endsWith("/"))
            baseUri = baseUri.substring(0, baseUri.length() - 2);
        mBaseUri = baseUri;
    }

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        StringBuilder sb = new StringBuilder();
        sb.append(mBaseUri);
        sb.append("/unsafe/");
        sb.append(spec.getWidth());
        sb.append("x");
        sb.append(spec.getHeight());
        sb.append("/filters:fill(fff,true)");
        if (spec.getFormat() == ImageLoadSpec.Format.JPEG)
            sb.append(":format(jpeg)");
        else if (spec.getFormat() == ImageLoadSpec.Format.PNG)
            sb.append(":format(png)");
        else if (spec.getFormat() == ImageLoadSpec.Format.WEBP)
            sb.append(":format(webp)");
        sb.append("/");
        sb.append(url);
        return sb.toString();
    }
}