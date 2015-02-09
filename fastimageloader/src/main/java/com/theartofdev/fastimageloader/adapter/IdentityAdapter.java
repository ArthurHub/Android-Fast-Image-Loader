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

/**
 * Doesn't change the URI.
 */
public class IdentityAdapter implements ImageServiceAdapter {

    @Override
    public String convert(String uri, ImageLoadSpec spec) {
        return uri;
    }
}