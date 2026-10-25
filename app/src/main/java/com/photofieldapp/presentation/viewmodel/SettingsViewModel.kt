package com.photofieldapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rx2.preferencesDataStore
import com.photofieldapp.util.FileStorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    private val dataStore = context.preferencesDataStore("settings")

    // Keys
    private val KEY_CAMERA_AUTOFOCUS = booleanPreferencesKey("camera_autofocus")
    private val KEY_CAMERA_FLASH_MODE = intPreferencesKey("camera_flash_mode")
    private val KEY_PDF_ORIENTATION = intPreferencesKey("pdf_orientation")
    private val KEY_PDF_PHOTOS_PER_PAGE = intPreferencesKey("pdf_photos_per_page")
    private val KEY_PDF_INCLUDE_GPS = booleanPreferencesKey("pdf_include_gps")
    private val KEY_PDF_INCLUDE_TIMESTAMP = booleanPreferencesKey("pdf_include_timestamp")
    private val KEY_STORAGE_LOCATION = stringPreferencesKey("storage_location")
    private val KEY_AUTO_BACKUP = booleanPreferencesKey("auto_backup")

    // Defaults
    private const val DEFAULT_AUTOFOCUS = true
    private const val DEFAULT_FLASH_MODE = 0 // Auto
    private const val DEFAULT_ORIENTATION = 0 // Portrait
    private const val DEFAULT_PHOTOS_PER_PAGE = 2
    private const val DEFAULT_INCLUDE_GPS = true
    private const val DEFAULT_INCLUDE_TIMESTAMP = true

    // Observables
    val cameraAutofocus: Flowable<Boolean> = dataStore.data
        .map { it[KEY_CAMERA_AUTOFOCUS] ?: DEFAULT_AUTOFOCUS }
        .distinctUntilChanged()

    val cameraFlashMode: Flowable<Int> = dataStore.data
        .map { it[KEY_CAMERA_FLASH_MODE] ?: DEFAULT_FLASH_MODE }
        .distinctUntilChanged()

    val pdfOrientation: Flowable<Int> = dataStore.data
        .map { it[KEY_PDF_ORIENTATION] ?: DEFAULT_ORIENTATION }
        .distinctUntilChanged()

    val pdfPhotosPerPage: Flowable<Int> = dataStore.data
        .map { it[KEY_PDF_PHOTOS_PER_PAGE] ?: DEFAULT_PHOTOS_PER_PAGE }
        .distinctUntilChanged()

    val pdfIncludeGps: Flowable<Boolean> = dataStore.data
        .map { it[KEY_PDF_INCLUDE_GPS] ?: DEFAULT_INCLUDE_GPS }
        .distinctUntilChanged()

    val pdfIncludeTimestamp: Flowable<Boolean> = dataStore.data
        .map { it[KEY_PDF_INCLUDE_TIMESTAMP] ?: DEFAULT_INCLUDE_TIMESTAMP }
        .distinctUntilChanged()

    val autoBackup: Flowable<Boolean> = dataStore.data
        .map { it[KEY_AUTO_BACKUP] ?: false }
        .distinctUntilChanged()

    // Setters
    fun setCameraAutofocus(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_CAMERA_AUTOFOCUS] = enabled }
        }
    }

    fun setCameraFlashMode(mode: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_CAMERA_FLASH_MODE] = mode }
        }
    }

    fun setPdfOrientation(orientation: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_PDF_ORIENTATION] = orientation }
        }
    }

    fun setPdfPhotosPerPage(count: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_PDF_PHOTOS_PER_PAGE] = count.coerceIn(1, 6) }
        }
    }

    fun setPdfIncludeGps(include: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_PDF_INCLUDE_GPS] = include }
        }
    }

    fun setPdfIncludeTimestamp(include: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_PDF_INCLUDE_TIMESTAMP] = include }
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_AUTO_BACKUP] = enabled }
        }
    }

    fun getStorageInfo(): String {
        val photosDir = FileStorageUtil.getPhotosDirectory(context)
        val pdfsDir = FileStorageUtil.getPdfsDirectory(context)
        val photosSize = getDirSize(photosDir)
        val pdfsSize = getDirSize(pdfsDir)
        return "Foto: ${FileStorageUtil.formatFileSize(photosSize)}\nPDF: ${FileStorageUtil.formatFileSize(pdfsSize)}"
    }

    private fun getDirSize(dir: java.io.File): Long {
        if (!dir.exists()) return 0
        return dir.walk().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearAllData() {
        viewModelScope.launch {
            FileStorageUtil.getPhotosDirectory(context).walk().filter { it.isFile }.forEach { it.delete() }
            FileStorageUtil.getPdfsDirectory(context).walk().filter { it.isFile }.forEach { it.delete() }
        }
    }
}