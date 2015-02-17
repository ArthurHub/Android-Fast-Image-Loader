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
import com.theartofdev.fastimageloader.impl.util.FILUtils;

/**
 * thumbor image service (http://thumbor.org/) adapter.<br>
 * Add image load specification into the path of the image URL.<br>
 * The image URI is already Thumbor URI, add Thumbor parameters in the middle of the URI.
 */
public class ThumborInlineAdapter extends ThumborAdapter {

    /**
     * the path part that split the thumbor URI part from image part.
     */
    private final String mPathPartSplit;

    /**
     * @param pathPartSplit the path part that split the thumbor URI part from image part.
     */
    public ThumborInlineAdapter(String pathPartSplit) {
        FILUtils.notNullOrEmpty(pathPartSplit, "pathPartSplit");
        mPathPartSplit = pathPartSplit;
    }

    @Override
    public String convert(String uri, ImageLoadSpec spec) {
        int idx = uri.indexOf(mPathPartSplit);
        if (idx > -1) {
            String thumborPart = uri.substring(0, idx);
            String imagePart = uri.substring(idx + mPathPartSplit.length());
            return createUri(thumborPart, imagePart, spec);
        }
        return uri;
    }
}