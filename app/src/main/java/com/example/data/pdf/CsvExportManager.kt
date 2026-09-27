package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.StudentExamResultSummary
import java.io.File
import java.io.FileWriter

object CsvExportManager {

    fun exportClassResultsToCsv(
        context: Context,
        examName: String,
        className: String,
        section: String,
        summaries: List<StudentExamResultSummary>
    ): File {
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val csvFile = File(reportsDir, "Class_Result_${className}_${section}_${examName.replace(" ", "_")}.csv")

        FileWriter(csvFile).use { writer ->
            writer.append("Roll Number,Student Name,Class,Section,Exam,Subject,Total Marks,Obtained Marks,Percentage,Grade,Status,Finalized\n")
            for (s in summaries) {
                writer.append("\"${s.student.rollNumber}\",")
                writer.append("\"${s.student.name}\",")
                writer.append("\"${s.student.className}\",")
                writer.append("\"${s.student.section}\",")
                writer.append("\"${s.exam.examName}\",")
                writer.append("\"${s.exam.subject}\",")
                writer.append("${s.totalMarks},")
                writer.append("${s.obtainedMarks},")
                writer.append("${String.format("%.2f", s.percentage)}%,")
                writer.append("\"${s.grade}\",")
                writer.append("\"${if (s.passed) "PASSED" else "FAILED"}\",")
                writer.append("\"${if (s.isFinalized) "YES" else "PENDING"}\"\n")
            }
        }

        return csvFile
    }

    fun shareCsv(context: Context, csvFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Class Results CSV"))
    }
}
