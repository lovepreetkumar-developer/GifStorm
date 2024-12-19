package com.techfathers.gifstorm.view_models

import androidx.lifecycle.ViewModel
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.data.respositories.CommonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class SearchGifViewModel(
    private val mainRepository: CommonRepository
) : ViewModel() {

    internal var isLoading = true
    internal var isLastPage = false
    internal var nextPage = ""
    internal var mSharingGifUrl:String? = null

    suspend fun autoCompleteSearch(
        queryMap: HashMap<String, String>
    ) = withContext(Dispatchers.IO) {
        mainRepository.autoCompleteSearch(queryMap)
    }

    suspend fun search(
        queryMap: HashMap<String, String>
    ) = withContext(Dispatchers.IO) {
        mainRepository.search(queryMap)
    }

    suspend fun trendingTerms(
        key: String
    ) = withContext(Dispatchers.IO) {
        mainRepository.trendingTerms(key)
    }

    suspend fun insertLikedGif(resultModel: ResultModel) = withContext(Dispatchers.IO) {
        mainRepository.insertLikedGif(resultModel)
    }

    suspend fun deleteLikedGif(id: String) = withContext(Dispatchers.IO) {
        mainRepository.deleteLikedGif(id)
    }

    suspend fun getLikedGif(id: String) = withContext(Dispatchers.IO) {
        mainRepository.getLikedGif(id)
    }
}