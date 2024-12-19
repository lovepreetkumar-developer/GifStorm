package com.techfathers.gifstorm.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techfathers.gifstorm.models.MediaModel

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class AllTypeConverters {

    companion object {
        private val gson = Gson()

        /*** MediaModel TypeConverts */
        @TypeConverter
        @JvmStatic
        fun stringToMediaModel(data: String?): List<MediaModel?>? {
            if (data == null) {
                return emptyList<MediaModel>()
            }
            val listType = object : TypeToken<List<MediaModel?>?>() {}.type
            return gson.fromJson<List<MediaModel?>>(data, listType)
        }

        @TypeConverter
        @JvmStatic
        fun mediaModelToString(someObjects: List<MediaModel?>?): String? {
            return gson.toJson(someObjects)
        }
    }
}