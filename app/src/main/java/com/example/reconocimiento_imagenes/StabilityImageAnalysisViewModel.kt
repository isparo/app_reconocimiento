package com.example.reconocimiento_imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class StabilityImageAnalysisViewModel : ViewModel() {
    var generatedImages by mutableStateOf<List<Bitmap>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private val stabilityApiKey = "token-value"
    private val apiUrl = "https://api.stability.ai/v2beta/stable-image/control/structure"

    fun clearError() {
        errorMessage = null
    }

    fun analyzeImage(context: Context, imageUri: Uri?, onSuccess: () -> Unit) {
        if (imageUri == null) {
            errorMessage = "Por favor, selecciona primero una imagen."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            generatedImages = emptyList()

            try {
                val imageBytes = withContext(Dispatchers.IO) {
                    uriToBytes(context, imageUri)
                }

                if (imageBytes == null) {
                    throw Exception("No se pudo procesar la imagen seleccionada.")
                }

                // Prompts translated to English for Stability AI
                val prompts = listOf(
                    "Reconstruct the broken archaeological object in the input image as a fully restored vessel, showing how it most likely looked originally when it was complete and in everyday use. Infer and rebuild the missing parts from the visible fragments, restoring the full original shape, proportions, rim, body, base, and any missing structural sections in a coherent, symmetrical, and realistic way. Preserve the authentic ancient ceramic character, material, and surface texture, and add historically plausible original color, decoration, and finish consistent with the surviving evidence. Remove all cracks, fractures, holes, missing sections, and detached shards. The final result must show one single complete, intact, museum-quality reconstruction of the object, fully restored and clearly visible."
                   // "Visually reconstruct this broken archaeological piece as if it were complete and show a possible version with original colors. Preserve the archaeological style, ceramic texture, visible decoration, and a probable coloration based on similar ancient pieces. Neutral background.",
                    //"Generate a second visual alternative of this reconstructed archaeological piece with possible original colors. Maintain historical coherence, texture ceramic, visible decoration, and a realistic archaeological appearance. Neutral background."
                )

                val results = mutableListOf<Bitmap>()

                for (prompt in prompts) {
                    val bitmap = callStabilityApi(prompt, imageBytes)
                    if (bitmap != null) {
                        results.add(bitmap)
                    } else {
                        throw Exception("El API no devolvió una imagen para uno de los procesos.")
                    }
                }

                generatedImages = results
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido al llamar al API de Stability AI."
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun callStabilityApi(prompt: String, imageBytes: ByteArray): Bitmap? = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", imageBytes.toRequestBody("image/jpeg".toMediaType()))
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("output_format", "png")
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $stabilityApiKey")
            .addHeader("Accept", "image/*")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body?.string()
                throw Exception("Error API (${response.code}): $responseBody")
            }

            val bodyBytes = response.body?.bytes()
            if (bodyBytes != null) {
                return@withContext BitmapFactory.decodeByteArray(bodyBytes, 0, bodyBytes.size)
            }
            null
        }
    }

    private fun uriToBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            // Reducir tamaño para optimizar envío al API (ej. max 1024px)
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
