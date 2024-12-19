package com.techfathers.gifstorm.view_models

import androidx.lifecycle.ViewModel
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.data.respositories.CommonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class GifDetailsViewModel(
    private val mainRepository: CommonRepository
) : ViewModel() {

    suspend fun insertLikedGif(resultModel: ResultModel) = withContext(Dispatchers.IO) {
        mainRepository.insertLikedGif(resultModel)
    }

    suspend fun deleteLikedGif(id: String) = withContext(Dispatchers.IO) {
        mainRepository.deleteLikedGif(id)
    }
}