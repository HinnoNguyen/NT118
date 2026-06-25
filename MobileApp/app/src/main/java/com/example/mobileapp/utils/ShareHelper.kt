package com.example.mobileapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    private const val FACEBOOK_PACKAGE  = "com.facebook.katana"
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"

    /** Share to Facebook — image only (text sharing blocked by FB) */
    fun shareToFacebook(context: Context, title: String, content: String) {
        val bitmap = createShareCard(title, content)
        val uri = saveBitmapToCache(context, bitmap) ?: run {
            shareAsText(context, title, content)
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(FACEBOOK_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Facebook not installed — open system chooser with image
            val chooser = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(chooser, "Share via"))
        }
    }

    /** Share to Instagram Stories — image only */
    fun shareToInstagram(context: Context, title: String, content: String) {
        val bitmap = createShareCard(title, content)
        val uri = saveBitmapToCache(context, bitmap) ?: run {
            shareAsText(context, title, content)
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(INSTAGRAM_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val chooser = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(chooser, "Share via"))
        }
    }

    /** Generic text share (WhatsApp, Telegram, Email, SMS, etc.) */
    fun shareAsText(context: Context, title: String, content: String) {
        val text = buildString {
            if (title.isNotBlank()) { appendLine(title); appendLine() }
            appendLine(content)
            appendLine()
            append("— Shared from Narrativize")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    /** Shows a bottom-sheet style chooser: Facebook, Instagram, Other */
    fun showShareDialog(context: Context, title: String, content: String) {
        val options = arrayOf("Facebook", "Instagram", "Other apps (WhatsApp, Telegram...)")
        android.app.AlertDialog.Builder(context)
            .setTitle("Share via")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareToFacebook(context, title, content)
                    1 -> shareToInstagram(context, title, content)
                    2 -> shareAsText(context, title, content)
                }
            }
            .show()
    }

    // ─── internals ───────────────────────────────────────────────────────────

    private fun createShareCard(title: String, content: String): Bitmap {
        val width  = 1080
        val height = 1080
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Dark background
        canvas.drawColor(Color.parseColor("#0D0D0D"))

        // Accent border strip (top)
        val borderPaint = Paint().apply { color = Color.parseColor("#00FF88"); strokeWidth = 12f; isAntiAlias = true }
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, borderPaint)

        // Title paint
        val titlePaint = Paint().apply {
            color = Color.parseColor("#00FF88")
            textSize = 64f
            isFakeBoldText = true
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }

        // Body paint
        val bodyPaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }

        // Footer paint
        val footerPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }

        // Draw title (word-wrapped)
        var y = 140f
        if (title.isNotBlank()) {
            y = drawWrappedText(canvas, title, titlePaint, 80f, y, width - 160f)
            y += 40f
        }

        // Draw content (word-wrapped, max ~12 lines)
        val preview = if (content.length > 500) content.take(500) + "…" else content
        y = drawWrappedText(canvas, preview, bodyPaint, 80f, y, width - 160f, maxLines = 12)

        // Footer
        canvas.drawText("— Narrativize", 80f, height - 80f, footerPaint)

        return bmp
    }

    private fun drawWrappedText(
        canvas: Canvas, text: String, paint: Paint,
        x: Float, startY: Float, maxWidth: Float, maxLines: Int = Int.MAX_VALUE
    ): Float {
        val words = text.split(" ")
        val lineHeight = paint.textSize * 1.4f
        var line = ""
        var y = startY
        var lines = 0
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) > maxWidth) {
                if (lines >= maxLines) break
                canvas.drawText(line, x, y, paint)
                y += lineHeight; lines++; line = word
            } else { line = test }
        }
        if (line.isNotEmpty() && lines < maxLines) { canvas.drawText(line, x, y, paint); y += lineHeight }
        return y
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val dir = File(context.cacheDir, "images").also { it.mkdirs() }
            val file = File(dir, "share_card.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) { null }
    }
}
