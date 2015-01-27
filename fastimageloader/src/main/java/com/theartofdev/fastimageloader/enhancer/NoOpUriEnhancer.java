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
 * URL enhancer stub that doesn't change the URI.
 */
public class NoOpUriEnhancer implements ImageServiceUriEnhancer {

    @Override
    public String enhance(String url, ImageLoadSpec spec) {
        return url;
    }
}