package com.techfathers.gifstorm.data.network

import com.google.gson.GsonBuilder
import com.techfathers.gifstorm.models.CategoriesResponse
import com.techfathers.gifstorm.models.GifsResponse
import com.techfathers.gifstorm.models.models_to_send.StringListResponse
import com.techfathers.gifstorm.util.ApiConstants.AUTOCOMPLETE_SEARCH_ENDPOINT
import com.techfathers.gifstorm.util.ApiConstants.BASE_URL
import com.techfathers.gifstorm.util.ApiConstants.CATEGORIES_ENDPOINT
import com.techfathers.gifstorm.util.ApiConstants.GIF_SEARCH_ENDPOINT
import com.techfathers.gifstorm.util.ApiConstants.TRENDING_GIFS_ENDPOINT
import com.techfathers.gifstorm.util.ApiConstants.TRENDING_TERMS_ENDPOINT
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

interface Apis {

    @GET(TRENDING_GIFS_ENDPOINT)
    suspend fun trendingGifs(
        @QueryMap queryMap: HashMap<String, String>
    ): Response<GifsResponse>

    @GET(AUTOCOMPLETE_SEARCH_ENDPOINT)
    suspend fun autoCompleteSearch(
        @QueryMap queryMap: HashMap<String, String>
    ): Response<StringListResponse>

    @GET(GIF_SEARCH_ENDPOINT)
    suspend fun search(
        @QueryMap queryMap: HashMap<String, String>
    ): Response<GifsResponse>

    @GET(TRENDING_TERMS_ENDPOINT)
    suspend fun trendingTerms(
        @Query("key") key: String
    ): Response<StringListResponse>

    @GET(CATEGORIES_ENDPOINT)
    suspend fun categories(
        @Query("key") key: String
    ): Response<CategoriesResponse>

    companion object {
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): Apis {

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(Apis::class.java)
        }
    }
}