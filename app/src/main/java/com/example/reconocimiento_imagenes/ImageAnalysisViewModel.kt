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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ImageAnalysisViewModel : ViewModel() {
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

    private val geminiApiKey = "token-xxxx"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image-preview:generateContent"

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
                val base64Image = withContext(Dispatchers.IO) {
                    uriToBase64(context, imageUri)
                }

                if (base64Image == null) {
                    throw Exception("No se pudo procesar la imagen seleccionada.")
                }

                val prompts = listOf(
                    "Reconstruye visualmente esta pieza arqueológica rota como si estuviera completa. Conserva su forma original probable, material cerámico, textura, estilo arqueológico y detalles visibles. Completa las partes faltantes de forma coherente. Fondo neutro.",
                    //"Reconstruye visualmente esta pieza arqueológica rota como si estuviera completa y muestra una posible versión con colores originales. Conserva el estilo arqueológico, textura cerámica, decoración visible y una coloración probable basada en piezas antiguas similares. Fondo neutro.",
                    //"Genera una segunda alternativa visual de esta pieza arqueológica reconstruida con posibles colores originales. Mantén coherencia histórica, textura cerámica, decoración visible y una apariencia arqueológica realista. Fondo neutro."
                )

                val results = mutableListOf<Bitmap>()

                for (prompt in prompts) {
                    val bitmap = callGeminiApi(prompt, base64Image)
                    if (bitmap != null) {
                        results.add(bitmap)
                    } else {
                        throw Exception("El API no devolvió una imagen para uno de los procesos.")
                    }
                }

                generatedImages = results
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido al llamar al API de Gemini."
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun callGeminiApi(prompt: String, base64Image: String): Bitmap? = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("x-goog-api-key", geminiApiKey)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                throw Exception("Error API (${response.code}): $responseBody")
            }

            if (responseBody == null) return@withContext null
            val responseJson = JSONObject(responseBody)

            val candidates = responseJson.optJSONArray("candidates")
            val content = candidates?.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var base64Result: String? = null
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inline_data")
                    val data = inlineData?.optString("data")
                    if (!data.isNullOrEmpty()) {
                        base64Result = data
                        break
                    }
                }
            }

            if (base64Result != null) {
                val imageBytes = Base64.decode(base64Result, Base64.DEFAULT)
                return@withContext BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
            null
        }
    }

    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            // Reducir tamaño para optimizar envío al API
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
