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
 * The image URI is already Thumbor URI, add Thumbor parameters in the middle of the URI.
 */
public class UriEnhancerThumborInPlace implements UriEnhancer {

    /**
     * the path part that split the thumbor URI part from image part.
     */
    private final String mPathPartSplit;

    /**
     * @param pathPartSplit the path part that split the thumbor URI part from image part.
     */
    public UriEnhancerThumborInPlace(String pathPartSplit) {
        Utils.notNullOrEmpty(pathPartSplit, "pathPartSplit");
        mPathPartSplit = pathPartSplit;
    }

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        int idx = url.indexOf(mPathPartSplit);
        if (idx > -1) {
            String thumborPart = url.substring(0, idx);
            String imagePart = url.substring(idx + mPathPartSplit.length());

            StringBuilder sb = new StringBuilder();
            sb.append(thumborPart);
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
            sb.append(imagePart);
            return sb.toString();
        }
        return url;
    }
}