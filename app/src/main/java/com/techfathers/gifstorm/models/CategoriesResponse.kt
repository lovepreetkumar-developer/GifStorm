package com.techfathers.gifstorm.models

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

data class CategoriesResponse(
    val locale: String,
    val tags: List<CategoryModel>
)

data class CategoryModel(
    val image: String,
    val name: String,
    val path: String,
    val searchterm: String,
    var background: Int?
)