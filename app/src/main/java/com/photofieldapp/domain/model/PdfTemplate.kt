package com.photofieldapp.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PdfTemplate(
    val id: Long = 0,
    val name: String,
    val headerText: String = "",
    val headerAlignment: Int = 0,
    val headerFontSize: Float = 18f,
    val footerText: String = "Halaman {page}/{total}",
    val footerAlignment: Int = 1,
    val footerFontSize: Float = 10f,
    val watermarkText: String = "",
    val watermarkOpacity: Float = 0.15f,
    val watermarkAngle: Float = -45f,
    val includeGps: Boolean = true,
    val includeTimestamp: Boolean = true,
    val photosPerPage: Int = 2,
    val pageOrientation: Int = 0,
    val marginTop: Float = 24f,
    val marginBottom: Float = 24f,
    val marginLeft: Float = 24f,
    val marginRight: Float = 24f,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun createDefault(): PdfTemplate {
            val now = Instant.now()
            return PdfTemplate(
                name = "Default",
                headerText = "PhotoField - Laporan Foto",
                headerAlignment = 1,
                headerFontSize = 20f,
                footerText = "Halaman {page}/{total} | {date}",
                footerAlignment = 1,
                footerFontSize = 10f,
                includeGps = true,
                includeTimestamp = true,
                photosPerPage = 2,
                pageOrientation = 0,
                isDefault = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun copyWithUpdated(
        name: String? = null,
        headerText: String? = null,
        headerAlignment: Int? = null,
        headerFontSize: Float? = null,
        footerText: String? = null,
        footerAlignment: Int? = null,
        footerFontSize: Float? = null,
        watermarkText: String? = null,
        watermarkOpacity: Float? = null,
        watermarkAngle: Float? = null,
        includeGps: Boolean? = null,
        includeTimestamp: Boolean? = null,
        photosPerPage: Int? = null,
        pageOrientation: Int? = null,
        marginTop: Float? = null,
        marginBottom: Float? = null,
        marginLeft: Float? = null,
        marginRight: Float? = null,
        isDefault: Boolean? = null
    ): PdfTemplate {
        return copy(
            name = name ?: this.name,
            headerText = headerText ?: this.headerText,
            headerAlignment = headerAlignment ?: this.headerAlignment,
            headerFontSize = headerFontSize ?: this.headerFontSize,
            footerText = footerText ?: this.footerText,
            footerAlignment = footerAlignment ?: this.footerAlignment,
            footerFontSize = footerFontSize ?: this.footerFontSize,
            watermarkText = watermarkText ?: this.watermarkText,
            watermarkOpacity = watermarkOpacity ?: this.watermarkOpacity,
            watermarkAngle = watermarkAngle ?: this.watermarkAngle,
            includeGps = includeGps ?: this.includeGps,
            includeTimestamp = includeTimestamp ?: this.includeTimestamp,
            photosPerPage = photosPerPage ?: this.photosPerPage,
            pageOrientation = pageOrientation ?: this.pageOrientation,
            marginTop = marginTop ?: this.marginTop,
            marginBottom = marginBottom ?: this.marginBottom,
            marginLeft = marginLeft ?: this.marginLeft,
            marginRight = marginRight ?: this.marginRight,
            isDefault = isDefault ?: this.isDefault,
            updatedAt = Instant.now()
        )
    }
}