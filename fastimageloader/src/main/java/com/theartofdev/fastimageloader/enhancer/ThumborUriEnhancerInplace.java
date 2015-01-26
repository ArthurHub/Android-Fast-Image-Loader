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
 * The image URI is already Thumbor URI, add Thumbor parameters in the middle of the URI.
 */
public class ThumborUriEnhancerInPlace implements ImageServiceUriEnhancer {

    /**
     * the path part that split the thumbor URI part from image part.
     */
    private final String mPathPartSplit;

    /**
     * @param pathPartSplit the path part that split the thumbor URI part from image part.
     */
    public ThumborUriEnhancerInPlace(String pathPartSplit) {
        if (pathPartSplit == null || pathPartSplit.length() < 1)
            throw new IllegalArgumentException("argument is null: " + pathPartSplit);
        mPathPartSplit = pathPartSplit;
    }

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        int idx = url.indexOf(mPathPartSplit);
        if (idx > -1) {
            String thumborPart = url.substring(0, idx);
            String imagePart = url.substring(idx + mPathPartSplit.length());
            return MessageFormat.format("{0}/unsafe/{1}x{2}/filters:fill(fff,true):format(jpeg)/{3}",
                    thumborPart,
                    spec.getWidth(),
                    spec.getHeight(),
                    imagePart);
        }
        return url;
    }
}