package ru.sntnadezhda.dacha.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WebDavClient(
    private val email: String,
    private val password: String
) {
    companion object {
        private const val TAG = "WebDav"
        // Попробуйте оба варианта и посмотрите логи:
        private const val URL_PLAIN = "https://webdav.cloud.mail.ru/SNT/snt_data.json"
        // private const val URL_WITH_EMAIL = "https://webdav.cloud.mail.ru/sdn9999@mail.ru/SNT/snt_data.json"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun downloadJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val creds = Credentials.basic(email, password)
            Log.d(TAG, "URL: $URL_PLAIN")
            Log.d(TAG, "Email: '$email'")
            Log.d(TAG, "Pass length: ${password.length}")
            Log.d(TAG, "Auth header: $creds")

            val request = Request.Builder()
                .url(URL_PLAIN)
                .header("Authorization", creds)
                .header("User-Agent", "Python-urllib/3.11")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response message: ${response.message}")
                Log.d(TAG, "Response headers: ${response.headers}")

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val text = response.body?.string() ?: ""
                if (text.isBlank()) {
                    Result.failure(IllegalStateException("Файл пустой"))
                } else {
                    Result.success(text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            Result.failure(e)
        }
    }
}