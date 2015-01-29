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

package com.theartofdev.fastimageloaderdemo.instagram.service;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * http://instagram.com/developer/endpoints/
 */
public interface InstagramService {

    /**
     * http://instagram.com/developer/endpoints/users/#get_users_feed
     */
    @GET("/users/self/feed?access_token=1670815861.1fb234f.b9690c21d125435a8f722856f8043ea2")
    public void getFeed(Callback<Feed> callback);

}

