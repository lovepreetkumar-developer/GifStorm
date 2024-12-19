package com.techfathers.gifstorm.data.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.techfathers.gifstorm.models.MediaModel
import kotlinx.parcelize.Parcelize

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

@Entity
@Parcelize
data class ResultModel(
    val bg_color: String,
    val content_description: String,
    val created: Double,
    val h1_title: String,
    val hasaudio: Boolean,
    val hascaption: Boolean,
    @PrimaryKey
    val id: String,
    val itemurl: String,
    val media: List<MediaModel>,
    val shares: Int,
    val source_id: String,
    val title: String,
    val url: String,
    var liked: Boolean
) : Parcelable