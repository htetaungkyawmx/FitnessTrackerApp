package org.hak.fitnesstrackerapp.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class CustomConverterFactory private constructor(private val json: Json) : Converter.Factory() {

    companion object {
        fun create(json: Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }): CustomConverterFactory {
            return CustomConverterFactory(json)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return if (type == Map::class.java || type == MutableMap::class.java) {
            Converter<Map<String, Any>, RequestBody> { value ->
                json.encodeToString(value).toRequestBody("application/json".toMediaType())
            }
        } else {
            null
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return Converter<ResponseBody, Any> { responseBody ->
            val jsonString = responseBody.string()
            when (type) {
                ApiResponse::class.java -> json.decodeFromString<ApiResponse>(jsonString)
                ActivitiesResponse::class.java -> json.decodeFromString<ActivitiesResponse>(jsonString)
                StatsResponse::class.java -> json.decodeFromString<StatsResponse>(jsonString)
                else -> jsonString
            }
        }
    }
}