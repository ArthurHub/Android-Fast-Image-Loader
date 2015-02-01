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
 * URL enhancer for thumbor (http://thumbor.org/) service.<br/>
 * Add image load specification into the path of the image URL.<br/>
 * Using Thumbor service URI to build new URI with the image URI as suffix.
 */
public class UriEnhancerThumbor implements UriEnhancer {

    //region: Fields and Consts

    /**
     * the thumbor base URI
     */
    private final String mBaseUri;
    //endregion

    protected UriEnhancerThumbor() {
        mBaseUri = null;
    }

    /**
     * @param baseUri the thumbor base URI
     */
    public UriEnhancerThumbor(String baseUri) {
        Utils.notNullOrEmpty(baseUri, "baseUri");

        baseUri = baseUri.trim();
        if (baseUri.endsWith("/"))
            baseUri = baseUri.substring(0, baseUri.length() - 2);
        mBaseUri = baseUri;
    }

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        return createUri(mBaseUri, url, spec);
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