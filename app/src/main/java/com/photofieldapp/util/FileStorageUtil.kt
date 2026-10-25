package com.photofieldapp.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileStorageUtil {

    private const val TAG = "FileStorageUtil"
    private const val APP_FOLDER_NAME = "PhotoField"
    private const val PHOTOS_FOLDER = "Pictures"
    private const val PDF_FOLDER = "Documents"

    fun getPhotosDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(PHOTOS_FOLDER), APP_FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getPdfsDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(PDF_FOLDER), APP_FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createPhotoFile(context: Context, activityId: Long): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timestamp}_${activityId}.jpg"
        val dir = getPhotosDirectory(context)
        return File(dir, fileName)
    }

    fun createPdfFile(context: Context, projectName: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeProjectName = projectName.replace(" ", "_").replace("/", "-")
        val fileName = "${safeProjectName}_$timestamp.pdf"
        val dir = getPdfsDirectory(context)
        return File(dir, fileName)
    }

    fun getRelativePath(context: Context, file: File): String {
        val baseDir = context.getExternalFilesDir(null)?.absolutePath ?: ""
        val absolutePath = file.absolutePath
        return if (absolutePath.startsWith(baseDir)) {
            absolutePath.substring(baseDir.length + 1)
        } else {
            file.name
        }
    }

    fun getAbsolutePath(context: Context, relativePath: String): File {
        val baseDir = context.getExternalFilesDir(null)?.absolutePath ?: ""
        return File(baseDir, relativePath)
    }

    fun copyUriToFile(context: Context, uri: Uri, destination: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy URI to file", e)
            false
        }
    }

    fun deleteFile(context: Context, relativePath: String): Boolean {
        return try {
            val file = getAbsolutePath(context, relativePath)
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file", e)
            false
        }
    }

    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}