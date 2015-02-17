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

package com.theartofdev.fastimageloader.adapter;

import com.theartofdev.fastimageloader.ImageLoadSpec;
import com.theartofdev.fastimageloader.ImageServiceAdapter;
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * thumbor image service (http://thumbor.org/) adapter.<br>
 * Add image load specification into the path of the image URL.<br>
 * Using Thumbor service URI to build new URI with the image URI as suffix.
 */
public class ThumborAdapter implements ImageServiceAdapter {

    //region: Fields and Consts

    /**
     * the thumbor base URI
     */
    private final String mBaseUri;
    //endregion

    protected ThumborAdapter() {
        mBaseUri = null;
    }

    /**
     * @param baseUri the thumbor base URI
     */
    public ThumborAdapter(String baseUri) {
        FILUtils.notNullOrEmpty(baseUri, "baseUri");

        baseUri = baseUri.trim();
        if (baseUri.endsWith("/"))
            baseUri = baseUri.substring(0, baseUri.length() - 2);
        mBaseUri = baseUri;
    }

    @Override
    public String convert(String uri, ImageLoadSpec spec) {
        return createUri(mBaseUri, uri, spec);
    }

    /**
     * Create thumbor URI from thumbor and image parts for the given spec.
     */
    protected String createUri(String thumborPart, String imagePort, ImageLoadSpec spec) {
        StringBuilder sb = new StringBuilder();

        sb.append(thumborPart);
        sb.append("/unsafe");

        if (spec.getWidth() > 0 || spec.getHeight() > 0) {
            sb.append("/").append(spec.getWidth()).append("x").append(spec.getHeight());
        }

        sb.append("/filters:fill(fff,true)");

        if (spec.getFormat() == ImageLoadSpec.Format.JPEG)
            sb.append(":format(jpeg)");
        else if (spec.getFormat() == ImageLoadSpec.Format.PNG)
            sb.append(":format(png)");
        else if (spec.getFormat() == ImageLoadSpec.Format.WEBP)
            sb.append(":format(webp)");

        sb.append("/").append(imagePort);

        return sb.toString();
    }
}