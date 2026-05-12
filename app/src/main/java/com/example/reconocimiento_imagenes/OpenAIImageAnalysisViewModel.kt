package com.example.reconocimiento_imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class OpenAIImageAnalysisViewModel : ViewModel() {
    var generatedImages by mutableStateOf<List<Bitmap>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    private val openAIToken = "<token>"
    private val apiUrl = "https://api.openai.com/v1/images/edits"

    fun clearError() {
        errorMessage = null
    }

    fun analyzeImage(context: Context, imageUri: Uri?, onSuccess: () -> Unit) {
        if (imageUri == null) {
            errorMessage = "Please select an image first."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            generatedImages = emptyList()

            try {
                val imageBytes = withContext(Dispatchers.IO) {
                    uriToPngBytes(context, imageUri)
                }

                if (imageBytes == null) {
                    throw Exception("Could not process the selected image.")
                }

                val prompts = listOf(
                    "Reconstruct this damaged archaeological artifact to show how it would have originally looked, restoring missing parts and preserving the ceramic style, material, and texture. Display the result on a neutral background.",
                    //"Reconstruct this damaged archaeological artifact and provide a historical coloration proposal based on similar ancient pieces. Restore missing parts and preserve texture and decorative details. Display the result on a neutral background.",
                    //"Generate a second alternative historical coloration for this reconstructed archaeological artifact, maintaining historical consistency, ceramic texture, and visible decoration. Display the result on a neutral background."
                )

                val results = mutableListOf<Bitmap>()

                for (prompt in prompts) {
                    val bitmap = callOpenAIApi(prompt, imageBytes)
                    if (bitmap != null) {
                        results.add(bitmap)
                    } else {
                        throw Exception("The API did not return an image for one of the processes.")
                    }
                }

                generatedImages = results
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error calling OpenAI API."
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun callOpenAIApi(prompt: String, imageBytes: ByteArray): Bitmap? = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "dall-e-2")
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("n", "1")
            .addFormDataPart("size", "1024x1024")
            .addFormDataPart("response_format", "b64_json")
            .addFormDataPart(
                "image",
                "image.png",
                imageBytes.toRequestBody("image/png".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $openAIToken")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                throw Exception("API Error (${response.code}): $responseBody")
            }

            if (responseBody == null) return@withContext null
            val responseJson = JSONObject(responseBody)
            val dataArray = responseJson.optJSONArray("data")
            val b64Json = dataArray?.optJSONObject(0)?.optString("b64_json")

            if (!b64Json.isNullOrEmpty()) {
                val decodedBytes = Base64.decode(b64Json, Base64.DEFAULT)
                return@withContext BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            }
            null
        }
    }

    private fun uriToPngBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // OpenAI Edits requires a square PNG image.
            val size = Math.min(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            val squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
            
            // Resize to 1024x1024 as recommended
            val scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, 1024, 1024, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
