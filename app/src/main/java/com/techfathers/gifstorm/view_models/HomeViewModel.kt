package com.techfathers.gifstorm.view_models

import androidx.lifecycle.ViewModel
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.data.respositories.CommonRepository
import com.techfathers.gifstorm.databinding.ItemGifBinding
import com.techfathers.gifstorm.ui.base.BaseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class HomeViewModel(
    private val mainRepository: CommonRepository
) : ViewModel() {

    internal var isLoading = true
    internal var isLastPage = false
    internal var nextPage = ""
    internal var mSharingGifUrl: String? = null
    internal var mLikedItems: MutableList<BaseItem<ResultModel, ItemGifBinding>> = mutableListOf()

    suspend fun trendingGifs(
        queryMap: HashMap<String, String>
    ) = withContext(Dispatchers.IO) {
        mainRepository.trendingGifs(queryMap)
    }

    suspend fun categories(
        key: String
    ) = withContext(Dispatchers.IO) {
        mainRepository.categories(key)
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

    suspend fun getAllLikedGifs() = withContext(Dispatchers.IO) {
        mainRepository.getAllLikedGifs()
    }
}