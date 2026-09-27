package com.example.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudentExamResultSummary
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StudentResultScreen(viewModel: MainViewModel, examId: Long, studentId: Long) {
    val context = LocalContext.current
    val summary by viewModel.currentResultSummary.collectAsStateWithLifecycle()

    LaunchedEffect(examId, studentId) {
        viewModel.loadEvaluationForStudent(examId, studentId)
    }

    if (summary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AcademicNavyPrimary)
        }
        return
    }

    val res = summary!!

    Scaffold(
        topBar = {
            AppHeader(
                title = "Student Examination Result",
                subtitle = "Official Score Card",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportAndSharePdf(context, res) },
                        modifier = Modifier.testTag("share_pdf_icon_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = AcademicNavyPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student & Exam Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = res.student.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicNavyPrimary
                                )
                                Text(
                                    text = "Roll No: ${res.student.rollNumber}  •  Class: ${res.student.className}-${res.student.section}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Badge(
                                containerColor = if (res.passed) ConfidenceHighGreenBg else ConfidenceLowRedBg
                            ) {
                                Text(
                                    text = if (res.passed) "PASSED" else "NEEDS IMPROVEMENT",
                                    color = if (res.passed) ConfidenceHighGreen else ConfidenceLowRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Exam: ${res.exam.examName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Subject: ${res.exam.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Big Score Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AcademicNavyPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Obtained Marks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${String.format("%.1f", res.obtainedMarks)} / ${res.totalMarks.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%.1f", res.percentage)}% Score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF80CBC4),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GRADE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = res.grade,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Performance Breakdown Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreStatChip(
                        title = "Correct",
                        count = res.correctCount,
                        color = ConfidenceHighGreen,
                        bgColor = ConfidenceHighGreenBg,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreStatChip(
                        title = "Partial",
                        count = res.partialCount,
                        color = ConfidenceMediumAmber,
                        bgColor = ConfidenceMediumAmberBg,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreStatChip(
                        title = "Incorrect",
                        count = res.incorrectCount,
                        color = ConfidenceLowRed,
                        bgColor = ConfidenceLowRedBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action Buttons: Download PDF & Share
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportAndSharePdf(context, res) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportAndSharePdf(context, res) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_pdf_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Print", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Question-wise Marks Breakdown Table
            item {
                Text(
                    text = "Question-wise Marks & Teacher Comments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(res.questionEvaluations) { qEval ->
                val q = qEval.question
                val aiMarks = qEval.finalMark?.aiSuggestedMarks ?: qEval.aiEvaluation?.suggestedMarks ?: 0.0
                val teacherMarks = qEval.finalMark?.teacherFinalMarks ?: aiMarks

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${q.questionNumber}: ${q.questionType}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AcademicNavyPrimary
                            )
                            Text(
                                text = "${String.format("%.1f", teacherMarks)} / ${q.maximumMarks} Marks",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ConfidenceHighGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AI Suggested: ${String.format("%.1f", aiMarks)}  •  Teacher Approved: ${String.format("%.1f", teacherMarks)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (!qEval.finalMark?.teacherComment.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Teacher Feedback: \"${qEval.finalMark!!.teacherComment}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = AcademicSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassResultsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val selectedExam by viewModel.selectedExam.collectAsStateWithLifecycle()

    var filterQuery by remember { mutableStateOf("") }

    val currentExam = selectedExam ?: allExams.firstOrNull()

    // Sample class results for table
    val sampleClassResults = remember {
        listOf(
            ClassResultRow("01", "Muhammad Abdul Daim", 48.0, 50.0, 96.0, "A+", true),
            ClassResultRow("02", "Ali Khan", 42.5, 50.0, 85.0, "A", true),
            ClassResultRow("03", "Ahmed Raza", 38.0, 50.0, 76.0, "B", true),
            ClassResultRow("04", "Hassan Tariq", 45.5, 50.0, 91.0, "A+", true)
        )
    }

    val filteredRows = sampleClassResults.filter {
        it.studentName.contains(filterQuery, ignoreCase = true) || it.rollNumber.contains(filterQuery)
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Class Examination Results",
                subtitle = "Class 10th-A • Computer Science",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentExam != null) {
                                val dummySummaries = emptyList<StudentExamResultSummary>()
                                viewModel.exportAndShareClassCsv(context, currentExam, dummySummaries)
                            }
                        },
                        modifier = Modifier.testTag("export_csv_icon_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = AcademicNavyPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search / Filter
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                label = { Text("Filter by student name or roll number") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("class_results_search_input"),
                singleLine = true
            )

            // Export Button Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredRows.size} Students Ranked",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        if (currentExam != null) {
                            val dummySummaries = emptyList<StudentExamResultSummary>()
                            viewModel.exportAndShareClassCsv(context, currentExam, dummySummaries)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV / Excel", fontSize = 12.sp)
                }
            }

            // Results Table
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Roll", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(45.dp))
                            Text("Student Name", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("Marks", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                            Text("Grade", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                        }
                    }
                }

                items(filteredRows) { row ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectStudent(row.rollNumber.toLongOrNull() ?: 1L)
                                viewModel.navigateTo(AppScreen.StudentResult(1, row.rollNumber.toLongOrNull() ?: 1L))
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.rollNumber, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), color = AcademicNavyPrimary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.studentName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${String.format("%.1f", row.percentage)}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${row.obtained}/${row.total.toInt()}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                            Badge(
                                containerColor = if (row.percentage >= 80) ConfidenceHighGreenBg else ConfidenceMediumAmberBg,
                                modifier = Modifier.width(55.dp)
                            ) {
                                Text(
                                    row.grade,
                                    fontWeight = FontWeight.Bold,
                                    color = if (row.percentage >= 80) ConfidenceHighGreen else ConfidenceMediumAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreStatChip(title: String, count: Int, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

data class ClassResultRow(
    val rollNumber: String,
    val studentName: String,
    val obtained: Double,
    val total: Double,
    val percentage: Double,
    val grade: String,
    val passed: Boolean
)
