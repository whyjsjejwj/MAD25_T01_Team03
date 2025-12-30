package np.ict.mad.studybuddy.core.storage

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    /**
     * Generates a simple PDF from note title/content and returns the created File.
     * Saved into cacheDir so you don't need storage permission.
     */
    fun noteToPdfFile(
        context: Context,
        title: String,
        content: String,
        fileName: String
    ): File {
        val safeTitle = title.ifBlank { "Untitled" }
        val safeContent = content.ifBlank { "" }

        val pdf = PdfDocument()

        val pageWidth = 595   // ~A4
        val pageHeight = 842  // ~A4
        val margin = 40

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
        }

        var pageNumber = 1
        var y = margin

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            y = margin
            return pdf.startPage(pageInfo)
        }

        var page = newPage()
        var canvas = page.canvas

        // Title
        canvas.drawText(safeTitle, margin.toFloat(), y.toFloat(), titlePaint)
        y += 30

        // Very simple wrapping (good enough for project)
        val maxCharsPerLine = 80
        val lines = safeContent
            .replace("\r\n", "\n")
            .split("\n")
            .flatMap { paragraph ->
                if (paragraph.isBlank()) listOf("")
                else paragraph.chunked(maxCharsPerLine)
            }

        for (line in lines) {
            if (y > pageHeight - margin) {
                pdf.finishPage(page)
                page = newPage()
                canvas = page.canvas
            }

            // blank line spacing
            if (line.isBlank()) {
                y += 18
                continue
            }

            canvas.drawText(line, margin.toFloat(), y.toFloat(), bodyPaint)
            y += 18
        }

        pdf.finishPage(page)

        val outFile = File(context.cacheDir, fileName)
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()

        return outFile
    }

    fun safePdfName(rawTitle: String): String {
        val t = rawTitle.ifBlank { "Untitled" }
            .replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
            .trim()
            .replace(" ", "_")
        return if (t.isBlank()) "Untitled.pdf" else "$t.pdf"
    }
}
