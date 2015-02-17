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
 * imgIX image service (http://www.imgix.com) adapter.<br>
 */
public class ImgIXAdapter implements ImageServiceAdapter {

    @Override
    public String convert(String uri, ImageLoadSpec spec) {
        StringBuilder sb = new StringBuilder(uri);

        int qIdx = uri.indexOf('?');
        sb.append(qIdx > -1 ? '&' : '?');

        if (spec.getFormat() == ImageLoadSpec.Format.JPEG)
            sb.append("auto=jpeg&");
        else if (spec.getFormat() == ImageLoadSpec.Format.PNG)
            sb.append("auto=png&");
        else if (spec.getFormat() == ImageLoadSpec.Format.WEBP)
            sb.append("auto=webp&");

        sb.append("fit=crop&");

        if (spec.getWidth() > 0) {
            sb.append("w=").append(spec.getWidth()).append('&');
        }
        if (spec.getHeight() > 0) {
            sb.append("h=").append(spec.getHeight()).append('&');
        }

        return sb.toString().substring(0, sb.length() - 1);
    }
}