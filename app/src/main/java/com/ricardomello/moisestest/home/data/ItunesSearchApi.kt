package com.ricardomello.moisestest.home.data

import com.ricardomello.moisestest.home.data.dto.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesSearchApi {

    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("country") country: String = "US",
        @Query("media") media: String = "music",
        @Query("entity") entity: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("lang") lang: String = "en_us",
        @Query("explicit") explicit: String = "Yes",
    ): ItunesSearchResponse

    @GET("lookup")
    suspend fun lookup(
        @Query("id") id: Long,
        @Query("entity") entity: String? = null,
        @Query("limit") limit: Int = 50,
    ): ItunesSearchResponse
}
