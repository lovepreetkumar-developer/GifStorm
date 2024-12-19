package com.techfathers.gifstorm.models

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

data class SocialMediaTypeModel(
    val name: String,
    val image: Int
) {
    constructor() : this(
        "",
        0
    )
}