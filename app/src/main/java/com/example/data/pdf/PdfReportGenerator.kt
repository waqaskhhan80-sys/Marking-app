package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.StudentExamResultSummary
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateStudentReportPdf(context: Context, summary: StudentExamResultSummary): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val primaryPaint = Paint().apply {
            color = Color.rgb(13, 71, 161) // Deep Blue
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(240, 244, 250)
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(200, 215, 235)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Draw Header Banner
        canvas.drawRect(0f, 0f, 595f, 90f, primaryPaint)
        canvas.drawText("AI ANSWER SHEET CHECKER", 40f, 40f, headerTitlePaint)
        val subheaderPaint = Paint().apply {
            color = Color.rgb(200, 225, 255)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("OFFICIAL STUDENT EXAMINATION RESULT REPORT", 40f, 60f, subheaderPaint)
        canvas.drawText("Verified Academic Evaluation", 40f, 75f, subheaderPaint)

        // Student & Exam Details Box
        var y = 120f
        val boxPaint = Paint().apply {
            color = Color.rgb(248, 249, 250)
        }
        canvas.drawRect(35f, 100f, 560f, 195f, boxPaint)
        canvas.drawRect(35f, 100f, 560f, 195f, borderPaint)

        canvas.drawText("Student Name: ${summary.student.name}", 50f, y, boldPaint)
        canvas.drawText("Roll Number: ${summary.student.rollNumber}", 350f, y, boldPaint)
        y += 22f

        canvas.drawText("Class & Section: ${summary.student.className} - ${summary.student.section}", 50f, y, textPaint)
        canvas.drawText("Examination: ${summary.exam.examName}", 350f, y, textPaint)
        y += 22f

        canvas.drawText("Subject: ${summary.exam.subject}", 50f, y, textPaint)
        canvas.drawText("Date: ${summary.exam.date}", 350f, y, textPaint)
        y += 22f

        canvas.drawText("Session: ${summary.exam.session}", 50f, y, textPaint)
        val statusText = if (summary.passed) "PASSED" else "NEEDS IMPROVEMENT"
        canvas.drawText("Status: $statusText", 350f, y, boldPaint)

        // Score Summary Banner
        y = 215f
        val scoreBoxPaint = Paint().apply {
            color = Color.rgb(232, 240, 254)
        }
        canvas.drawRect(35f, y, 560f, y + 45f, scoreBoxPaint)
        canvas.drawRect(35f, y, 560f, y + 45f, borderPaint)

        boldPaint.textSize = 14f
        canvas.drawText("Marks: ${summary.obtainedMarks} / ${summary.totalMarks}", 50f, y + 28f, boldPaint)
        canvas.drawText("Percentage: ${String.format("%.1f", summary.percentage)}%", 230f, y + 28f, boldPaint)
        canvas.drawText("Grade: ${summary.grade}", 420f, y + 28f, boldPaint)
        boldPaint.textSize = 12f

        // Table Header
        y += 65f
        canvas.drawRect(35f, y, 560f, y + 25f, tableHeaderPaint)
        canvas.drawRect(35f, y, 560f, y + 25f, borderPaint)

        canvas.drawText("Q#", 45f, y + 17f, boldPaint)
        canvas.drawText("Question Type", 80f, y + 17f, boldPaint)
        canvas.drawText("Max", 210f, y + 17f, boldPaint)
        canvas.drawText("AI Rec.", 260f, y + 17f, boldPaint)
        canvas.drawText("Teacher Final", 325f, y + 17f, boldPaint)
        canvas.drawText("Remarks & Teacher Feedback", 415f, y + 17f, boldPaint)

        // Table Rows
        y += 25f
        summary.questionEvaluations.forEach { qEval ->
            val rowY = y
            canvas.drawLine(35f, rowY + 28f, 560f, rowY + 28f, borderPaint)

            canvas.drawText("Q${qEval.question.questionNumber}", 45f, rowY + 18f, boldPaint)
            val typeShort = if (qEval.question.questionType.length > 14) qEval.question.questionType.take(12) + ".." else qEval.question.questionType
            canvas.drawText(typeShort, 80f, rowY + 18f, textPaint)
            canvas.drawText("${qEval.question.maximumMarks}", 210f, rowY + 18f, textPaint)

            val aiMarks = qEval.finalMark?.aiSuggestedMarks ?: qEval.aiEvaluation?.suggestedMarks ?: 0.0
            val teacherMarks = qEval.finalMark?.teacherFinalMarks ?: aiMarks
            canvas.drawText(String.format("%.1f", aiMarks), 260f, rowY + 18f, textPaint)
            canvas.drawText(String.format("%.1f", teacherMarks), 335f, rowY + 18f, boldPaint)

            val comment = qEval.finalMark?.teacherComment?.ifBlank { qEval.aiEvaluation?.explanation ?: "Verified" } ?: "Verified"
            val trimmedComment = if (comment.length > 25) comment.take(23) + "..." else comment
            canvas.drawText(trimmedComment, 415f, rowY + 18f, textPaint)

            y += 28f
        }

        // Audit Trail & Policy Statement
        y += 35f
        val notePaint = Paint().apply {
            color = Color.rgb(90, 90, 90)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Policy Compliance Notice: AI marks serve strictly as automated suggestions.", 40f, y, notePaint)
        y += 14f
        canvas.drawText("The Teacher holds full and final authority over all awarded marks. Handwriting style is not penalized.", 40f, y, notePaint)

        // Signature Lines
        y += 60f
        canvas.drawLine(50f, y, 200f, y, borderPaint)
        canvas.drawText("Evaluator / Teacher Signature", 50f, y + 15f, textPaint)

        canvas.drawLine(400f, y, 550f, y, borderPaint)
        canvas.drawText("Principal / School Stamp", 410f, y + 15f, textPaint)

        pdfDocument.finishPage(page)

        // Save File
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, "Result_${summary.student.rollNumber}_${summary.exam.examName.replace(" ", "_")}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return pdfFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Student Result PDF"))
    }
}
