package com.photofieldapp.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.util.TypedValue
import com.photofieldapp.data.local.entity.PdfTemplate
import com.photofieldapp.data.local.entity.Photo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGeneratorUtil {

    private const val TAG = "PdfGeneratorUtil"

    data class PdfResult(
        val file: File?,
        val success: Boolean,
        val errorMessage: String? = null,
        val pageCount: Int = 0
    )

    data class GenerationProgress(
        val currentPage: Int,
        val totalPages: Int,
        val currentPhotoIndex: Int,
        val totalPhotos: Int
    )

    typealias ProgressCallback = (GenerationProgress) -> Unit

    fun generatePdf(
        context: Context,
        projectName: String,
        activityName: String,
        photos: List<Photo>,
        template: PdfTemplate,
        progressCallback: ProgressCallback? = null
    ): PdfResult {
        val pdfFile = FileStorageUtil.createPdfFile(context, projectName)

        val pageOrientation = if (template.pageOrientation == 1) {
            PdfDocument.PageInfo.PAGE_ORIENTATION_LANDSCAPE
        } else {
            PdfDocument.PageInfo.PAGE_ORIENTATION_PORTRAIT
        }

        // Calculate page size in points (1/72 inch)
        val (pageWidth, pageHeight) = if (pageOrientation == PdfDocument.PageInfo.PAGE_ORIENTATION_LANDSCAPE) {
            842f to 595f // A4 landscape in points
        } else {
            595f to 842f // A4 portrait in points
        }

        val marginLeft = dpToPx(template.marginLeft)
        val marginRight = dpToPx(template.marginRight)
        val marginTop = dpToPx(template.marginTop)
        val marginBottom = dpToPx(template.marginBottom)

        val contentWidth = pageWidth - marginLeft - marginRight
        val contentHeight = pageHeight - marginTop - marginBottom

        val photosPerPage = template.photosPerPage.coerceAtLeast(1)
        val totalPages = (photos.size + photosPerPage - 1) / photosPerPage

        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        try {
            for (pageIndex in 0 until totalPages) {
                val startIdx = pageIndex * photosPerPage
                val endIdx = min(startIdx + photosPerPage, photos.size)
                val pagePhotos = photos.subList(startIdx, endIdx)

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1)
                    .setOrientation(pageOrientation)
                    .create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Draw watermark if present
                if (template.watermarkText.isNotBlank()) {
                    drawWatermark(canvas, pageWidth, pageHeight, template, paint)
                }

                // Draw header
                val headerBottom = drawHeader(
                    canvas, pageWidth, marginLeft, marginTop, contentWidth,
                    projectName, activityName, template, textPaint, pageIndex + 1, totalPages
                )

                // Draw photos
                val photoAreaTop = headerBottom + dpToPx(16)
                val availableHeight = pageHeight - marginBottom - photoAreaTop
                val photoAreaHeight = availableHeight / photosPerPage

                for ((i, photo) in pagePhotos.withIndex()) {
                    val photoTop = photoAreaTop + i * photoAreaHeight
                    val photoBottom = photoTop + photoAreaHeight - dpToPx(8)

                    drawPhotoWithInfo(
                        canvas, context, photo,
                        marginLeft, photoTop, contentWidth, photoAreaHeight - dpToPx(8),
                        template, textPaint, paint
                    )
                }

                // Draw footer
                drawFooter(
                    canvas, pageWidth, marginLeft, pageHeight - marginBottom, contentWidth,
                    template, textPaint, pageIndex + 1, totalPages
                )

                document.finishPage(page)

                progressCallback?.invoke(GenerationProgress(
                    currentPage = pageIndex + 1,
                    totalPages = totalPages,
                    currentPhotoIndex = endIdx,
                    totalPhotos = photos.size
                ))
            }

            FileOutputStream(pdfFile).use { output ->
                document.writeTo(output)
            }

            document.close()

            return PdfResult(
                file = pdfFile,
                success = true,
                pageCount = totalPages
            )

        } catch (e: Exception) {
            Log.e(TAG, "PDF generation failed", e)
            document.close()
            if (pdfFile.exists()) pdfFile.delete()
            return PdfResult(
                file = null,
                success = false,
                errorMessage = e.message
            )
        }
    }

    private fun drawWatermark(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        template: PdfTemplate,
        paint: Paint
    ) {
        paint.color = Color.parseColor("#CCCCCC")
        paint.alpha = (255 * template.watermarkOpacity).toInt()
        paint.textSize = max(pageWidth, pageHeight) / 10f
        paint.textAlign = Paint.Align.CENTER

        val watermarkText = template.watermarkText
        canvas.save()
        canvas.translate(pageWidth / 2, pageHeight / 2)
        canvas.rotate(template.watermarkAngle)
        canvas.drawText(watermarkText, 0f, 0f, paint)
        canvas.restore()
    }

    private fun drawHeader(
        canvas: Canvas,
        pageWidth: Float,
        marginLeft: Float,
        marginTop: Float,
        contentWidth: Float,
        projectName: String,
        activityName: String,
        template: PdfTemplate,
        textPaint: TextPaint,
        pageNum: Int,
        totalPages: Int
    ): Float {
        textPaint.color = Color.BLACK
        textPaint.textSize = dpToPx(template.headerFontSize)
        textPaint.textAlign = when (template.headerAlignment) {
            1 -> Paint.Align.CENTER
            2 -> Paint.Align.RIGHT
            else -> Paint.Align.LEFT
        }

        val headerText = template.headerText
            .replace("{project}", projectName)
            .replace("{activity}", activityName)
            .replace("{page}", pageNum.toString())
            .replace("{total}", totalPages.toString())
            .replace("{date}", SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()))

        val x = when (textPaint.textAlign) {
            Paint.Align.CENTER -> pageWidth / 2
            Paint.Align.RIGHT -> pageWidth - marginLeft
            else -> marginLeft
        }

        val layout = StaticLayout.Builder.obtain(headerText, 0, headerText.length, textPaint, contentWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .build()

        val y = marginTop
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()

        return y + layout.height + dpToPx(8)
    }

    private fun drawFooter(
        canvas: Canvas,
        pageWidth: Float,
        marginLeft: Float,
        footerBaseline: Float,
        contentWidth: Float,
        template: PdfTemplate,
        textPaint: TextPaint,
        pageNum: Int,
        totalPages: Int
    ) {
        textPaint.color = Color.GRAY
        textPaint.textSize = dpToPx(template.footerFontSize)
        textPaint.textAlign = when (template.footerAlignment) {
            1 -> Paint.Align.CENTER
            2 -> Paint.Align.RIGHT
            else -> Paint.Align.LEFT
        }

        val footerText = template.footerText
            .replace("{page}", pageNum.toString())
            .replace("{total}", totalPages.toString())
            .replace("{date}", SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()))

        val x = when (textPaint.textAlign) {
            Paint.Align.CENTER -> pageWidth / 2
            Paint.Align.RIGHT -> pageWidth - marginLeft
            else -> marginLeft
        }

        val layout = StaticLayout.Builder.obtain(footerText, 0, footerText.length, textPaint, contentWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .build()

        canvas.save()
        canvas.translate(x, footerBaseline - layout.height)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawPhotoWithInfo(
        canvas: Canvas,
        context: Context,
        photo: Photo,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        template: PdfTemplate,
        textPaint: TextPaint,
        paint: Paint
    ) {
        // Calculate image bounds (leave space for info below)
        val infoHeight = dpToPx(48)
        val imageHeight = height - infoHeight
        val imageWidth = width

        // Load and draw image
        val imageFile = FileStorageUtil.getAbsolutePath(context, photo.filePath)
        if (imageFile.exists()) {
            val bitmap = loadScaledBitmap(imageFile, imageWidth.toInt(), imageHeight.toInt())
            if (bitmap != null) {
                val scaledWidth = min(bitmap.width.toFloat(), imageWidth)
                val scaledHeight = min(bitmap.height.toFloat(), imageHeight)
                val left = x + (width - scaledWidth) / 2
                val top = y + (imageHeight - scaledHeight) / 2
                val dstRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                canvas.drawBitmap(bitmap, null, dstRect, paint)
                bitmap.recycle()
            }
        }

        // Draw photo border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(1)
        paint.color = Color.LTGRAY
        canvas.drawRect(x, y, x + width, y + imageHeight, paint)
        paint.style = Paint.Style.FILL

        // Draw info text
        val infoY = y + imageHeight + dpToPx(4)
        textPaint.color = Color.DKGRAY
        textPaint.textSize = dpToPx(10)
        textPaint.textAlign = Paint.Align.LEFT

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(photo.timestamp))
        var infoText = "Foto ${photo.sortOrder + 1}  |  $timeStr"

        if (template.includeGps && photo.hasLocation) {
            infoText += "\nLat: ${String.format("%.6f", photo.latitude)}, Lng: ${String.format("%.6f", photo.longitude)}"
            if (photo.accuracy > 0) infoText += "  (±${photo.accuracy.toInt()}m)"
        }

        if (template.includeTimestamp) {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(photo.timestamp))
            infoText += "  |  $dateStr"
        }

        val layout = StaticLayout.Builder.obtain(infoText, 0, infoText.length, textPaint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.3f)
            .build()

        canvas.save()
        canvas.translate(x, infoY)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun loadScaledBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, android.content.res.Resources.getSystem().displayMetrics)
    }
}