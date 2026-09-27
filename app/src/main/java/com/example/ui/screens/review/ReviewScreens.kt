package com.example.ui.screens.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.FullQuestionEvaluation
import com.example.ui.components.AppHeader
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.TeacherReviewRequiredBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherReviewScreen(viewModel: MainViewModel, examId: Long, studentId: Long) {
    val reviewQuestions by viewModel.reviewQuestions.collectAsStateWithLifecycle()
    val summary by viewModel.currentResultSummary.collectAsStateWithLifecycle()
    var showFinalizeDialog by remember { mutableStateOf(false) }

    val pendingCount = reviewQuestions.count { it.finalMark?.reviewed != true }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Teacher Marks Review",
                subtitle = "${summary?.student?.name ?: "Student"} • Roll ${summary?.student?.rollNumber ?: "01"}",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                actions = {
                    TextButton(
                        onClick = { showFinalizeDialog = true },
                        modifier = Modifier.testTag("finalize_marks_top_button")
                    ) {
                        Text("Finalize", fontWeight = FontWeight.Bold, color = AcademicNavyPrimary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total: ${summary?.obtainedMarks ?: 0.0} / ${summary?.totalMarks ?: 50.0}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", summary?.percentage ?: 0.0)}% • Grade: ${summary?.grade ?: "A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AcademicSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { showFinalizeDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary),
                        modifier = Modifier.testTag("finalize_marks_button")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finalize Result", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notice Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = AcademicNavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Teacher Holds Final Authority",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "AI marks are recommendations only. Tap any question to verify, adjust marks, or edit extracted handwriting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (pendingCount > 0) {
                item {
                    TeacherReviewRequiredBadge(modifier = Modifier.fillMaxWidth())
                }
            }

            // Question review cards
            items(reviewQuestions) { qEval ->
                val q = qEval.question
                val aiMarks = qEval.finalMark?.aiSuggestedMarks ?: qEval.aiEvaluation?.suggestedMarks ?: 0.0
                val teacherMarks = qEval.finalMark?.teacherFinalMarks ?: aiMarks
                val isOverride = qEval.finalMark != null && qEval.finalMark.teacherFinalMarks != qEval.finalMark.aiSuggestedMarks
                val confidence = qEval.aiEvaluation?.confidence ?: 0.90
                val requiresReview = qEval.aiEvaluation?.requiresTeacherReview == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openSideBySideReview(qEval) }
                        .testTag("review_question_${q.questionNumber}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (requiresReview) ConfidenceLowRedBg.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (requiresReview) androidx.compose.foundation.BorderStroke(1.dp, ConfidenceLowRed) else null
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${q.questionNumber} • ${q.questionType}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AcademicNavyPrimary
                            )
                            ConfidenceBadge(confidence = confidence)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI: ${String.format("%.1f", aiMarks)} / ${q.maximumMarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Final: ${String.format("%.1f", teacherMarks)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverride) ConfidenceMediumAmber else ConfidenceHighGreen
                                )
                                if (isOverride) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = ConfidenceMediumAmberBg) {
                                        Text("Override", color = ConfidenceMediumAmber, fontSize = 10.sp)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Review",
                                    tint = AcademicNavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verify",
                                    fontSize = 12.sp,
                                    color = AcademicNavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Finalize Confirmation Dialog
    if (showFinalizeDialog) {
        AlertDialog(
            onDismissRequest = { showFinalizeDialog = false },
            icon = { Icon(Icons.Default.Verified, contentDescription = null, tint = AcademicNavyPrimary) },
            title = { Text("Finalize Result?") },
            text = {
                Text("After finalization, teacher-approved marks will become the official result. An immutable audit trail preserving AI recommendations and teacher overrides will be stored.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalizeDialog = false
                        viewModel.finalizeStudentMarks(examId, studentId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary),
                    modifier = Modifier.testTag("confirm_finalize_button")
                ) {
                    Text("Finalize Result")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SideBySideReviewScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val qEval by viewModel.activeReviewQuestion.collectAsStateWithLifecycle()
    val summary by viewModel.currentResultSummary.collectAsStateWithLifecycle()

    if (qEval == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No question selected.")
        }
        return
    }

    val q = qEval!!.question
    val ans = qEval!!.studentAnswer
    val aiEval = qEval!!.aiEvaluation
    val currentFm = qEval!!.finalMark

    var teacherMarksInput by remember(qEval) {
        mutableStateOf("${currentFm?.teacherFinalMarks ?: aiEval?.suggestedMarks ?: 0.0}")
    }
    var teacherComment by remember(qEval) {
        mutableStateOf(currentFm?.teacherComment ?: "")
    }
    var editableOcrText by remember(qEval) {
        mutableStateOf(ans?.extractedText ?: "")
    }

    val samplePaperFile = remember {
        File(context.filesDir, "answer_sheets/student_01_page_1.png")
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Side-by-Side Verification",
                subtitle = "Q${q.questionNumber}: ${q.questionType} (${q.maximumMarks} Marks)",
                onBackClick = {
                    val eId = summary?.exam?.id ?: 1L
                    val sId = summary?.student?.id ?: 1L
                    viewModel.navigateTo(AppScreen.TeacherReview(eId, sId))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Confidence Badge & Rule Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfidenceBadge(confidence = aiEval?.confidence ?: 0.90)
                Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Max: ${q.maximumMarks} Marks", fontWeight = FontWeight.Bold)
                }
            }

            // 1. Student Handwritten Original Image
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Original Handwritten Answer Sheet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                    ) {
                        if (samplePaperFile.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(samplePaperFile),
                                contentDescription = "Original Answer Sheet Crop",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Handwritten answer sheet view",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 2. AI Reading (Transcribed OCR) - Teacher can edit!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Reading (OCR Transcribed)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Editable",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcademicSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editableOcrText,
                        onValueChange = { editableOcrText = it },
                        modifier = Modifier.fillMaxWidth().testTag("edit_extracted_text_input"),
                        minLines = 3
                    )
                }
            }

            // 3. Teacher Answer Key & Expected Concepts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Teacher Expected Answer & Marking Scheme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = q.expectedAnswer.ifBlank { "Answer key not provided." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (q.importantConcepts.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Key Concepts: ${q.importantConcepts}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AcademicSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 4. Marking Criteria Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Marking Criteria & AI Reasoning",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    qEval!!.criteria.forEach { crit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${crit.criterion}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("${crit.maximumMarks} Mark(s)")
                            }
                        }
                    }

                    if (!aiEval?.explanation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "AI Reason: ${aiEval!!.explanation}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 5. Teacher Approval / Manual Override Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AcademicNavyPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Teacher Approved Final Mark",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = teacherMarksInput,
                            onValueChange = { teacherMarksInput = it },
                            label = { Text("Teacher Final Mark") },
                            modifier = Modifier.weight(1f).testTag("teacher_final_mark_input"),
                            singleLine = true
                        )

                        // Quick Accept AI button
                        Button(
                            onClick = {
                                teacherMarksInput = "${aiEval?.suggestedMarks ?: 0.0}"
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Accept AI")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = teacherComment,
                        onValueChange = { teacherComment = it },
                        label = { Text("Teacher Comment / Audit Note") },
                        placeholder = { Text("e.g. Concept verified, full marks awarded") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher_comment_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val markVal = teacherMarksInput.toDoubleOrNull() ?: (aiEval?.suggestedMarks ?: 0.0)
                            val aiVal = aiEval?.suggestedMarks ?: 0.0
                            val eId = summary?.exam?.id ?: 1L
                            val sId = summary?.student?.id ?: 1L

                            viewModel.updateTeacherMark(
                                studentAnswerId = ans?.id ?: 1L,
                                examId = eId,
                                studentId = sId,
                                questionId = q.id,
                                aiSuggestedMarks = aiVal,
                                newTeacherFinalMarks = markVal,
                                comment = teacherComment,
                                updatedOcrText = editableOcrText
                            )
                            viewModel.navigateTo(AppScreen.TeacherReview(eId, sId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_teacher_override_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Teacher Decision", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
