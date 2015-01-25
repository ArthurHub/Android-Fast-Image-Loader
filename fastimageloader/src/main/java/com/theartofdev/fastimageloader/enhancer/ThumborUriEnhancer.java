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
import com.theartofdev.fastimageloader.impl.CommonUtils;

/**
 * TODO:a add doc
 */
public class ThumborUriEnhancer implements ImageServiceUriEnhancer {

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        String split = "/images/";
        int idx = url.indexOf(split);
        if (idx > -1) {
            String thumborPart = url.substring(0, idx);
            String imagePart = url.substring(idx + split.length());
            return CommonUtils.format("{}/unsafe/{}x{}/filters:fill(fff,true):format(jpeg)/{}", thumborPart, spec.getWidth(), spec.getHeight(), imagePart);
        }
        return url;
    }
}