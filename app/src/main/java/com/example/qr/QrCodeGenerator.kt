package com.example.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.EnumMap
import kotlin.math.min

object QrCodeGenerator {

    /**
     * Generates a high-quality stylized QR Code Bitmap with custom branding and logo embedding.
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 1024,
        config: QrStyleConfig = QrStyleConfig(),
        context: Context? = null,
        logoBitmapOverride: Bitmap? = null
    ): Bitmap {
        val safeContent = content.ifBlank { "https://ai.studio" }
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) // 30% error correction allows logo
            put(EncodeHintType.MARGIN, 2)
        }

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(safeContent, BarcodeFormat.QR_CODE, size, size, hints)

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        // Calculate layout with optional bottom brand footer
        val hasBrandLabel = !config.brandLabel.isNullOrBlank()
        val brandBannerHeight = if (hasBrandLabel) (size * 0.18f).toInt() else 0
        val totalHeight = size + brandBannerHeight

        val bitmap = Bitmap.createBitmap(size, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = config.parseBgColor()
        val fgColor = config.parseFgColor()
        val eyeColor = config.parseEyeColor()

        // 1. Draw Canvas Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Setup Foreground Shader / Gradient
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val gradColor = config.parseGradientColor()
            if (gradColor != null && config.gradientType != GradientType.NONE) {
                shader = when (config.gradientType) {
                    GradientType.DIAGONAL -> LinearGradient(
                        0f, 0f, size.toFloat(), size.toFloat(),
                        fgColor, gradColor, Shader.TileMode.CLAMP
                    )
                    GradientType.HORIZONTAL -> LinearGradient(
                        0f, 0f, size.toFloat(), 0f,
                        fgColor, gradColor, Shader.TileMode.CLAMP
                    )
                    else -> LinearGradient(
                        0f, 0f, size.toFloat(), size.toFloat(),
                        fgColor, gradColor, Shader.TileMode.CLAMP
                    )
                }
            } else {
                color = fgColor
            }
        }

        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = eyeColor
            style = Paint.Style.FILL
        }

        val cellWidth = size.toFloat() / matrixWidth
        val cellHeight = size.toFloat() / matrixHeight

        // Identify Finder Pattern coordinates (top-left 7x7, top-right 7x7, bottom-left 7x7)
        // Find padding margin offset
        var minX = matrixWidth
        var minY = matrixHeight
        var maxX = 0
        var maxY = 0
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix.get(x, y)) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        val finderTL = Rect(minX, minY, minX + 7, minY + 7)
        val finderTR = Rect(maxX - 6, minY, maxX + 1, minY + 7)
        val finderBL = Rect(minX, maxY - 6, minX + 7, maxY + 1)

        fun isFinderPattern(x: Int, y: Int): Boolean {
            return finderTL.contains(x, y) || finderTR.contains(x, y) || finderBL.contains(x, y)
        }

        // Center exclusion zone for embedded logo
        val hasLogo = logoBitmapOverride != null || config.logoPreset != LogoPreset.NONE || config.customLogoUri != null
        val logoPercent = config.logoSizePercent.coerceIn(15, 28) / 100f
        val logoDim = size * logoPercent
        val logoLeft = (size - logoDim) / 2f
        val logoTop = (size - logoDim) / 2f
        val logoRect = RectF(logoLeft, logoTop, logoLeft + logoDim, logoTop + logoDim)
        val logoMargin = cellWidth * 0.75f
        val logoExclusionRect = RectF(
            logoRect.left - logoMargin,
            logoRect.top - logoMargin,
            logoRect.right + logoMargin,
            logoRect.bottom + logoMargin
        )

        // 3. Render Normal Modules
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix.get(x, y)) {
                    val left = x * cellWidth
                    val top = y * cellHeight
                    val right = left + cellWidth
                    val bottom = top + cellHeight
                    val moduleRect = RectF(left, top, right, bottom)

                    // Skip if inside Finder Pattern or Logo Zone
                    if (isFinderPattern(x, y)) continue
                    if (hasLogo && RectF.intersects(moduleRect, logoExclusionRect)) continue

                    when (config.dotStyle) {
                        QrDotStyle.SQUARE -> {
                            canvas.drawRect(moduleRect, fgPaint)
                        }
                        QrDotStyle.ROUNDED -> {
                            val radius = cellWidth * 0.35f
                            canvas.drawRoundRect(moduleRect, radius, radius, fgPaint)
                        }
                        QrDotStyle.DOTS -> {
                            val cx = left + cellWidth / 2f
                            val cy = top + cellHeight / 2f
                            val radius = cellWidth * 0.42f
                            canvas.drawCircle(cx, cy, radius, fgPaint)
                        }
                        QrDotStyle.SQUIRCLE -> {
                            val inset = cellWidth * 0.05f
                            val squircleRect = RectF(left + inset, top + inset, right - inset, bottom - inset)
                            canvas.drawRoundRect(squircleRect, cellWidth * 0.45f, cellWidth * 0.45f, fgPaint)
                        }
                    }
                }
            }
        }

        // 4. Render Styled Finder Eyes (Top-Left, Top-Right, Bottom-Left)
        drawFinderEye(canvas, finderTL, cellWidth, cellHeight, config.eyeStyle, eyePaint, bgPaint)
        drawFinderEye(canvas, finderTR, cellWidth, cellHeight, config.eyeStyle, eyePaint, bgPaint)
        drawFinderEye(canvas, finderBL, cellWidth, cellHeight, config.eyeStyle, eyePaint, bgPaint)

        // 5. Render Central Embedded Logo
        if (hasLogo) {
            val logo = logoBitmapOverride ?: loadLogoBitmap(context, config, (logoDim * 1.5f).toInt())
            if (logo != null) {
                // Background badge container for logo with subtle shadow/border
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = eyeColor
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.006f
                }

                val badgeRect = RectF(
                    logoRect.left - logoMargin * 0.6f,
                    logoRect.top - logoMargin * 0.6f,
                    logoRect.right + logoMargin * 0.6f,
                    logoRect.bottom + logoMargin * 0.6f
                )

                val cornerRad = badgeRect.width() * 0.35f
                canvas.drawRoundRect(badgeRect, cornerRad, cornerRad, badgePaint)
                canvas.drawRoundRect(badgeRect, cornerRad, cornerRad, borderPaint)

                // Scale and draw logo bitmap inside badge
                val targetLogoRect = RectF(
                    logoRect.left + logoDim * 0.1f,
                    logoRect.top + logoDim * 0.1f,
                    logoRect.right - logoDim * 0.1f,
                    logoRect.bottom - logoDim * 0.1f
                )
                canvas.drawBitmap(logo, null, targetLogoRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
            }
        }

        // 6. Render Brand Bottom Banner
        if (hasBrandLabel && !config.brandLabel.isNullOrBlank()) {
            val bannerTop = size.toFloat()
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = eyeColor
                textSize = size * 0.045f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isColorDark(bgColor)) AndroidColor.LTGRAY else AndroidColor.DKGRAY
                textSize = size * 0.028f
                textAlign = Paint.Align.CENTER
            }

            val textY = bannerTop + (brandBannerHeight * 0.45f)
            canvas.drawText(config.brandLabel, size / 2f, textY, textPaint)

            if (!config.brandSubtext.isNullOrBlank()) {
                val subtextY = bannerTop + (brandBannerHeight * 0.78f)
                canvas.drawText(config.brandSubtext, size / 2f, subtextY, subtextPaint)
            }
        }

        return bitmap
    }

    private fun drawFinderEye(
        canvas: Canvas,
        finderRect: Rect,
        cellW: Float,
        cellH: Float,
        eyeStyle: QrEyeStyle,
        eyePaint: Paint,
        bgPaint: Paint
    ) {
        val outerLeft = finderRect.left * cellW
        val outerTop = finderRect.top * cellH
        val outerRight = (finderRect.left + 7) * cellW
        val outerBottom = (finderRect.top + 7) * cellH
        val outerRect = RectF(outerLeft, outerTop, outerRight, outerBottom)

        val midLeft = (finderRect.left + 1) * cellW
        val midTop = (finderRect.top + 1) * cellH
        val midRight = (finderRect.left + 6) * cellW
        val midBottom = (finderRect.top + 6) * cellH
        val midRect = RectF(midLeft, midTop, midRight, midBottom)

        val innerLeft = (finderRect.left + 2) * cellW
        val innerTop = (finderRect.top + 2) * cellH
        val innerRight = (finderRect.left + 5) * cellW
        val innerBottom = (finderRect.top + 5) * cellH
        val innerRect = RectF(innerLeft, innerTop, innerRight, innerBottom)

        when (eyeStyle) {
            QrEyeStyle.SQUARE -> {
                canvas.drawRect(outerRect, eyePaint)
                canvas.drawRect(midRect, bgPaint)
                canvas.drawRect(innerRect, eyePaint)
            }
            QrEyeStyle.ROUNDED -> {
                val outerRad = cellW * 2f
                val midRad = cellW * 1.4f
                val innerRad = cellW * 1f
                canvas.drawRoundRect(outerRect, outerRad, outerRad, eyePaint)
                canvas.drawRoundRect(midRect, midRad, midRad, bgPaint)
                canvas.drawRoundRect(innerRect, innerRad, innerRad, eyePaint)
            }
            QrEyeStyle.CIRCLE -> {
                canvas.drawOval(outerRect, eyePaint)
                canvas.drawOval(midRect, bgPaint)
                canvas.drawOval(innerRect, eyePaint)
            }
        }
    }

    private fun loadLogoBitmap(context: Context?, config: QrStyleConfig, targetSize: Int): Bitmap? {
        if (context == null) return null

        // If custom logo URI is provided
        if (!config.customLogoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(config.customLogoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                    val original = BitmapFactory.decodeStream(stream, null, options)
                    if (original != null) {
                        return createCircularBitmap(original, targetSize)
                    }
                }
            } catch (e: Exception) {
                // Fallback to preset
            }
        }

        // Generate clean stylized vector bitmap for Preset logos
        if (config.logoPreset != LogoPreset.NONE) {
            return generatePresetLogoBitmap(config.logoPreset, config.parseEyeColor(), targetSize)
        }

        return null
    }

    /**
     * Creates a crisp geometric preset brand badge bitmap.
     */
    fun generatePresetLogoBitmap(preset: LogoPreset, tintColor: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.08f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val w = size.toFloat()
        val h = size.toFloat()

        when (preset) {
            LogoPreset.STAR -> {
                val path = android.graphics.Path()
                val cx = w / 2f
                val cy = h / 2f
                val outerR = w * 0.42f
                val innerR = w * 0.19f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = (i * 36 - 90) * Math.PI / 180.0
                    val x = (cx + r * Math.cos(angle)).toFloat()
                    val y = (cy + r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            LogoPreset.HEART -> {
                val path = android.graphics.Path()
                val cx = w / 2f
                val cy = h / 2f
                path.moveTo(cx, cy + h * 0.35f)
                path.cubicTo(cx - w * 0.45f, cy, cx - w * 0.45f, cy - h * 0.35f, cx, cy - h * 0.15f)
                path.cubicTo(cx + w * 0.45f, cy - h * 0.35f, cx + w * 0.45f, cy, cx, cy + h * 0.35f)
                path.close()
                canvas.drawPath(path, paint)
            }
            LogoPreset.WEB, LogoPreset.WIFI -> {
                // Draw Wifi / Signal arches
                val cx = w / 2f
                val cy = h * 0.72f
                canvas.drawCircle(cx, cy, w * 0.08f, paint)
                val oval1 = RectF(cx - w * 0.22f, cy - w * 0.22f, cx + w * 0.22f, cy + w * 0.22f)
                canvas.drawArc(oval1, 220f, 100f, false, strokePaint)
                val oval2 = RectF(cx - w * 0.38f, cy - w * 0.38f, cx + w * 0.38f, cy + w * 0.38f)
                canvas.drawArc(oval2, 220f, 100f, false, strokePaint)
            }
            LogoPreset.CODE -> {
                // Draw code brackets < / >
                val path = android.graphics.Path()
                // <
                path.moveTo(w * 0.35f, h * 0.3f)
                path.lineTo(w * 0.18f, h * 0.5f)
                path.lineTo(w * 0.35f, h * 0.7f)
                // >
                path.moveTo(w * 0.65f, h * 0.3f)
                path.lineTo(w * 0.82f, h * 0.5f)
                path.lineTo(w * 0.65f, h * 0.7f)
                // /
                path.moveTo(w * 0.55f, h * 0.25f)
                path.lineTo(w * 0.45f, h * 0.75f)
                canvas.drawPath(path, strokePaint)
            }
            LogoPreset.TECH, LogoPreset.SHIELD -> {
                // Shield badge
                val path = android.graphics.Path()
                path.moveTo(w * 0.5f, h * 0.15f)
                path.lineTo(w * 0.82f, h * 0.28f)
                path.lineTo(w * 0.82f, h * 0.55f)
                path.cubicTo(w * 0.82f, h * 0.75f, w * 0.5f, h * 0.88f, w * 0.5f, h * 0.88f)
                path.cubicTo(w * 0.5f, h * 0.88f, w * 0.18f, h * 0.75f, w * 0.18f, h * 0.55f)
                path.lineTo(w * 0.18f, h * 0.28f)
                path.close()
                canvas.drawPath(path, paint)
            }
            else -> {
                // Brand Diamond / Emblem
                val path = android.graphics.Path()
                path.moveTo(w * 0.5f, h * 0.18f)
                path.lineTo(w * 0.82f, h * 0.5f)
                path.lineTo(w * 0.5f, h * 0.82f)
                path.lineTo(w * 0.18f, h * 0.5f)
                path.close()
                canvas.drawPath(path, paint)
            }
        }

        return bitmap
    }

    private fun createCircularBitmap(src: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, size * 0.35f, size * 0.35f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val srcRect = Rect(0, 0, src.width, src.height)
        canvas.drawBitmap(src, srcRect, rect, paint)
        return output
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * AndroidColor.red(color) + 0.587 * AndroidColor.green(color) + 0.114 * AndroidColor.blue(color)) / 255
        return darkness >= 0.5
    }

    // ==========================================
    // Export & Sharing Utilities
    // ==========================================

    fun shareBitmap(context: Context, bitmap: Bitmap, title: String = "Branded QR Code") {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "qr_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title - Generated with QR Studio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing QR code: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String = "QR_Code"): Uri? {
        val name = "${fileName}_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "QRStudio")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "QRStudio"
                val dir = File(imagesDir)
                if (!dir.exists()) dir.mkdirs()
                val image = File(dir, name)
                fos = FileOutputStream(image)
                imageUri = Uri.fromFile(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.flush()
            }
            Toast.makeText(context, "Saved to Pictures/QRStudio", Toast.LENGTH_SHORT).show()
            return imageUri
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    fun copyTextToClipboard(context: Context, text: String, label: String = "QR Data") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
