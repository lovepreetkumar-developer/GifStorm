package com.techfathers.gifstorm.vm_factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techfathers.gifstorm.data.respositories.CommonRepository
import com.techfathers.gifstorm.view_models.HomeViewModel

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: CommonRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}