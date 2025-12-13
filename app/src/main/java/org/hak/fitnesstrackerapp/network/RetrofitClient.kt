package org.hak.fitnesstrackerapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2/fitness_api/api/" // For emulator
    // For real device: "http://192.168.x.x/fitness_api/api/"

    private lateinit var retrofit: Retrofit
    private var authToken: String? = null

    fun init() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Add auth token if available
            authToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            // Add common headers
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("Accept", "application/json")

            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): ApiService {
        if (!::retrofit.isInitialized) {
            init()
        }
        return retrofit.create(ApiService::class.java)
    }

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    fun hasAuthToken(): Boolean {
        return authToken != null
    }

    fun getAuthToken(): String? {
        return authToken
    }
}