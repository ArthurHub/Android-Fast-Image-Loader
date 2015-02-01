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
 * The possible states of loading image request.
 */
public enum LoadState {

    /**
     * No image is set to load or last image was cleared
     */
    UNSET,

    /**
     * Image requested to load
     */
    LOADING,

    /**
     * Image finished loading successfully
     */
    LOADED,

    /**
     * Image failed to load
     */
    FAILED
}

