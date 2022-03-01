package com.example.labc.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface JsonPlaceHolderApi {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("/v1/me/top/tracks")
    fun getUserTopTracks(
        @Header("Authorization") authorizationHeader: String
    ): Call<ItemJSONResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("/v1/me/top/artists")
    fun getUserTopArtists(
        @Header("Authorization") authorizationHeader: String,
    ): Call<ItemJSONResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("/v1/recommendations")
    fun getRecommendations(
        @Header("Authorization") authorizationHeader: String,
        @Query("limit", encoded = true) limit: Int,
        @Query("seed_artists", encoded = true) seed_artists: String,
        @Query("seed_tracks", encoded = true) seed_tracks: String,
    ): Call<RecommendationJSONResponse>

    //https://api.spotify.com/v1/recommendations?limit=60&seed_artists=4NHQUGzhtTLFvgF5SZesLK%2C2CIMQHirSU0MQqyYHq0eOx%2C57dN52uHvrHOxijzpIgu3E&seed_tracks=0c6xIDDpzE81m2q797ordA%2C1p7939nftudJfauI7fpa04
    //https://api.spotify.com/v1/recommendations?limit=60&seed_artists=4NHQUGzhtTLFvgF5SZesLK%2C2CIMQHirSU0MQqyYHq0eOx%2C57dN52uHvrHOxijzpIgu3E&seed_tracks=0c6xIDDpzE81m2q797ordA%2C1p7939nftudJfauI7fpa04
}