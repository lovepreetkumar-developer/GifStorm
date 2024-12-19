package com.techfathers.gifstorm.data.respositories

import androidx.lifecycle.LiveData
import com.techfathers.gifstorm.data.db.AppDatabase
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.data.network.Apis
import com.techfathers.gifstorm.data.network.SafeApiRequest
import com.techfathers.gifstorm.models.CategoriesResponse
import com.techfathers.gifstorm.models.GifsResponse
import com.techfathers.gifstorm.models.models_to_send.StringListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class CommonRepository(
    private val api: Apis,
    private val db: AppDatabase
) : SafeApiRequest() {

    suspend fun trendingGifs(
        queryMap: HashMap<String, String>
    ): GifsResponse {
        return apiRequest { api.trendingGifs(queryMap) }
    }

    suspend fun autoCompleteSearch(
        queryMap: HashMap<String, String>
    ): StringListResponse {
        return apiRequest { api.autoCompleteSearch(queryMap) }
    }

    suspend fun search(
        queryMap: HashMap<String, String>
    ): GifsResponse {
        return apiRequest { api.search(queryMap) }
    }

    suspend fun trendingTerms(
        key:String
    ): StringListResponse {
        return apiRequest { api.trendingTerms(key) }
    }

    suspend fun categories(
        key:String
    ): CategoriesResponse {
        return apiRequest { api.categories(key) }
    }

    suspend fun insertLikedGif(resultModel: ResultModel) {
        return withContext(Dispatchers.IO) {
            db.getUserDao().insertLikedGif(
                resultModel
            )
        }
    }

    suspend fun deleteLikedGif(id: String) {
        return withContext(Dispatchers.IO) {
            db.getUserDao().deleteLikedGif(
                id
            )
        }
    }

    suspend fun getLikedGif(id: String): ResultModel? {
        return withContext(Dispatchers.IO) {
            db.getUserDao().getLikedGif(
                id
            )
        }
    }

    suspend fun getAllLikedGifs(): List<ResultModel>? {
        return withContext(Dispatchers.IO) {
            db.getUserDao().getAllLikedGifs()
        }
    }
}