package com.techfathers.gifstorm.data.network

import com.techfathers.gifstorm.util.MyApiException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

abstract class SafeApiRequest {

    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody()?.string()
            val message = StringBuilder()
            error?.let {
                try {
                    message.append(JSONObject(it).getString("message"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            throw MyApiException(message.toString())
        }
    }
}