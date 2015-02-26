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

package com.theartofdev.fastimageloader.target;

/**
 * Used to declare target drawable to support animation.
 */
public interface AnimatingTargetDrawable {

    /**
     * Is the drawable is currently animating fade-in of the image
     */
    boolean isAnimating();
}