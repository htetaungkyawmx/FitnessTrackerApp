package org.hak.fitnesstrackerapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityTestConnectionBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.network.UserRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TestConnectionActivity : BaseActivity() {
    private lateinit var binding: ActivityTestConnectionBinding
    private val apiService = RetrofitClient.instance

    override fun setupActivity() {
        binding = ActivityTestConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnTestGet.setOnClickListener {
            testGetRequest()
        }

        binding.btnTestPost.setOnClickListener {
            testPostRequest()
        }

        binding.btnTestRetrofit.setOnClickListener {
            testRetrofit()
        }

        binding.btnTestRawHttp.setOnClickListener {
            testRawHttp()
        }
    }

    private fun testGetRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the public BASE_URL
                val url = URL("${RetrofitClient.BASE_URL}test.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                withContext(Dispatchers.Main) {
                    binding.tvResult.text = """
                        GET Response:
                        Code: $responseCode
                        Response: $response
                    """.trimIndent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvResult.text = "GET Error: ${e.message}"
                }
            }
        }
    }

    private fun testPostRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the public BASE_URL
                val url = URL("${RetrofitClient.BASE_URL}test.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val output = connection.outputStream
                val json = """{"test": "data", "timestamp": "${System.currentTimeMillis()}"}"""
                output.write(json.toByteArray())
                output.flush()
                output.close()

                val responseCode = connection.responseCode
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                withContext(Dispatchers.Main) {
                    binding.tvResult.text = """
                        POST Response:
                        Code: $responseCode
                        Response: $response
                    """.trimIndent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvResult.text = "POST Error: ${e.message}"
                }
            }
        }
    }

    private fun testRetrofit() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use UserRequest data class instead of Map
                val userRequest = UserRequest(
                    name = "Test User",
                    email = "test@test.com",
                    password = "password123"
                )
                val response = apiService.register(userRequest)

                withContext(Dispatchers.Main) {
                    binding.tvResult.text = """
                        Retrofit Response:
                        Success: ${response.isSuccessful}
                        Code: ${response.code()}
                        Body: ${response.body()}
                    """.trimIndent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvResult.text = "Retrofit Error: ${e.message}"
                }
            }
        }
    }

    private fun testRawHttp() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Test with HttpURLConnection
                val url = URL("${RetrofitClient.BASE_URL}test.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000

                val responseCode = connection.responseCode
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                withContext(Dispatchers.Main) {
                    showToast("HTTP Response: $responseCode - $response")
                    binding.tvResult.text = """
                        Raw HTTP Test:
                        URL: ${url}
                        Response Code: $responseCode
                        Response: $response
                    """.trimIndent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvResult.text = "Raw HTTP Error: ${e.message}"
                }
            }
        }
    }
}