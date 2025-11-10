package com.shipthis.go.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(
    private val gson: Gson
) {

    fun getErrorMessage(error: Throwable): String {
        return when {
            isNetworkError(error) -> {
                "Please check your internet connection."
            }
            error is HttpException -> {
                extractHttpErrorMessage(error)
            }
            else -> {
                error.message ?: "An unexpected error occurred"
            }
        }
    }

    private fun isNetworkError(error: Throwable): Boolean {
        return error is IOException ||
               error is UnknownHostException ||
               (error.cause is IOException) ||
               (error.cause is UnknownHostException)
    }

    private fun extractHttpErrorMessage(error: HttpException): String {
        return try {
            val errorBody = error.response()?.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                return getDefaultErrorMessage(error.code())
            }

            // Try to parse error message from response
            val errorMessage = parseErrorBody(errorBody)
            if (errorMessage.isNotBlank()) {
                return errorMessage
            }

            getDefaultErrorMessage(error.code())
        } catch (e: Exception) {
            getDefaultErrorMessage(error.code())
        }
    }

    private fun parseErrorBody(errorBody: String): String {
        // Try to parse as JSON array (Zod validation errors)
        try {
            val jsonArray = gson.fromJson(errorBody, JsonArray::class.java)
            if (jsonArray != null) {
                val messages = jsonArray.mapNotNull { element ->
                    when {
                        element.isJsonObject -> {
                            element.asJsonObject.get("message")?.asString
                        }
                        else -> element.asString
                    }
                }
                if (messages.isNotEmpty()) {
                    return messages.joinToString(" ") { "Error - $it" }
                }
            }
        } catch (e: Exception) {
            // Not an array, try object
        }

        // Try to parse as JSON object with error field
        try {
            val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
            jsonObject?.get("error")?.asString?.let { return it }
        } catch (e: Exception) {
            // Not valid JSON
        }

        return ""
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request. Please check your input."
            401 -> "Authentication failed. Please try again."
            404 -> "Resource not found."
            500 -> "Server error. Please try again later."
            else -> "Request failed with status $code"
        }
    }
}

