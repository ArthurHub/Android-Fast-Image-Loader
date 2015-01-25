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
 * Enhance image loading URL with format/size/etc. parameters by image loading specification.
 */
public interface ImageServiceUriEnhancer {

    /**
     * Enhance image loading URL with format/size/etc. parameters by image loading specification.
     *
     * @param url the raw image URL to enhance
     * @param spec the image loading specification to enhance by
     * @return enhanced URL
     */
    String enhance(String url, ImageLoadSpec spec);
}
