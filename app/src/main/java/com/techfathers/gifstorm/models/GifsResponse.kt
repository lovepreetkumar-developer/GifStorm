package com.techfathers.gifstorm.models

import android.os.Parcelable
import com.techfathers.gifstorm.data.db.entities.ResultModel
import kotlinx.parcelize.Parcelize

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

data class GifsResponse(
    val locale: String,
    val next: String,
    val results: List<ResultModel>
)

@Parcelize
data class MediaModel(
    val nanogif: NanoGifModel,
    val gif: GifModel,
    val mp4: Mp4Model,
    val tinygif: TinygifModel
) : Parcelable

@Parcelize
data class GifModel(
    val dims: List<Int>,
    val preview: String,
    val size: Int,
    val url: String
) : Parcelable

@Parcelize
data class Mp4Model(
    val dims: List<Int>,
    val duration: Double,
    val preview: String,
    val size: Int,
    val url: String
) : Parcelable

@Parcelize
data class TinygifModel(
    val dims: List<Int>,
    val preview: String,
    val size: Int,
    val url: String
) : Parcelable

@Parcelize
data class NanoGifModel(
    val dims: List<Int>,
    val preview: String,
    val size: Int,
    val url: String
) : Parcelable