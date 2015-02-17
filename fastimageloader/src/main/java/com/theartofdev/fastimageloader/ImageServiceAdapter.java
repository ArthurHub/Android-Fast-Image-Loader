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
 * Define adapter for specific image service (Thumbor/imgIX/Cloudinary/etc.) used in image loading.<br>
 * Used to add to the requested image URI the required image loading specification (format/size/etc).
 */
public interface ImageServiceAdapter {

    /**
     * Add to raw image loading URI the required specifications (format/size/etc.) parameters.
     *
     * @param uri the raw image URI to convert
     * @param spec the image loading specification to convert by
     * @return URI with loading specification
     */
    String convert(String uri, ImageLoadSpec spec);
}
