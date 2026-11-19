package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    private const val MAX_SHEET_BASE64_LENGTH = 42_000

    /**
     * Copies a photo selected via Android Photo Picker into app-private storage so it
     * persists across app restarts without requiring broad storage permissions.
     */
    suspend fun persistPickedImage(context: Context, uri: Uri, prefix: String = "bag"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "mk_bags_images").apply { mkdirs() }
                val destFile = File(imagesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext null
                "file:${destFile.absolutePath}"
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Downscales and JPEG-compresses a local screenshot so it fits safely within
     * a single Google Sheets cell (50,000 char limit).
     */
    suspend fun encodeImageForSync(imageKey: String): String {
        if (!imageKey.startsWith("file:")) return ""
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imageKey.removePrefix("file:"))
                if (!file.exists()) return@withContext ""

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext ""

                var sampleSize = 1
                while ((bounds.outWidth / sampleSize) > 720 || (bounds.outHeight / sampleSize) > 720) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
                    ?: return@withContext ""

                var working = scaleDown(decoded, 640)
                if (working !== decoded) decoded.recycle()

                var encodedResult = ""
                for (quality in listOf(65, 48, 34, 22)) {
                    val out = ByteArrayOutputStream()
                    working.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    val candidate = "data:image/jpeg;base64,$b64"
                    if (candidate.length <= MAX_SHEET_BASE64_LENGTH) {
                        encodedResult = candidate
                        break
                    }
                }
                if (encodedResult.isEmpty()) {
                    val tiny = scaleDown(working, 360)
                    val out = ByteArrayOutputStream()
                    tiny.compress(Bitmap.CompressFormat.JPEG, 25, out)
                    if (tiny !== working) tiny.recycle()
                    val candidate = "data:image/jpeg;base64,${Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)}"
                    if (candidate.length <= MAX_SHEET_BASE64_LENGTH) {
                        encodedResult = candidate
                    }
                }
                working.recycle()
                encodedResult
            } catch (_: Exception) {
                ""
            }
        }
    }

    suspend fun saveBase64Image(context: Context, orderNumber: String, base64DataUrl: String): String? {
        if (base64DataUrl.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val rawBase64 = base64DataUrl.substringAfter("base64,", base64DataUrl).trim()
                if (rawBase64.isBlank()) return@withContext null
                val bytes = Base64.decode(rawBase64, Base64.DEFAULT)
                if (bytes.isEmpty()) return@withContext null

                val imagesDir = File(context.filesDir, "mk_bags_images").apply { mkdirs() }
                val safeName = orderNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val destFile = File(imagesDir, "sync_${safeName}_${bytes.contentHashCode().toUInt()}.jpg")
                if (!destFile.exists()) {
                    FileOutputStream(destFile).use { it.write(bytes) }
                }
                "file:${destFile.absolutePath}"
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        val targetW = (w * ratio).toInt().coerceAtLeast(1)
        val targetH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
