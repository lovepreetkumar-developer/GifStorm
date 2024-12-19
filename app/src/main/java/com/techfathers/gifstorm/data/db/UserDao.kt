package com.techfathers.gifstorm.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techfathers.gifstorm.data.db.entities.ResultModel

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedGif(resultModel: ResultModel): Long

    @Query("DELETE FROM ResultModel WHERE id=:gifId")
    fun deleteLikedGif(gifId: String)

    @Query("SELECT * FROM ResultModel WHERE id=:gifId")
    fun getLikedGif(gifId: String): ResultModel?

    @Query("SELECT * FROM ResultModel")
    fun getAllLikedGifs(): List<ResultModel>?

}