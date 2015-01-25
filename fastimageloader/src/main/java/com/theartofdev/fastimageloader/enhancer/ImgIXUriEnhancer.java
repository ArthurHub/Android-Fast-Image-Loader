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
 * URL enhancer for imgIX (http://www.imgix.com) service.<br/>
 * Add image load specification as query params to the image URL.
 */
public class ImgIXUriEnhancer implements ImageServiceUriEnhancer {

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        int qIdx = url.indexOf('?');
        url += qIdx > -1 ? '&' : '?';
        return url + "auto=jpeg&fit=crop&w=" + spec.getWidth() + "&h=" + spec.getHeight();
    }
}