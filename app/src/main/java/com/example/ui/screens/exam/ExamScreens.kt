package com.example.ui.screens.exam

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Exam
import com.example.data.model.MarkingCriterion
import com.example.data.model.Question
import com.example.ui.components.AppHeader
import com.example.ui.theme.AcademicNavyPrimary
import com.example.ui.theme.AcademicSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ExamListScreen(viewModel: MainViewModel) {
    val exams by viewModel.allExams.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Examinations",
                subtitle = "Manage papers and marking schemes",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CreateExam) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Exam") },
                containerColor = AcademicNavyPrimary,
                modifier = Modifier.testTag("create_exam_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exams) { exam ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectExam(exam.id)
                            viewModel.navigateTo(AppScreen.ExamDetail(exam.id))
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exam.examName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("Class ${exam.className}-${exam.section}")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Subject: ${exam.subject}  |  Session: ${exam.session}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total Marks: ${exam.totalMarks.toInt()}  •  Passing Marks: ${exam.passingMarks.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AcademicNavyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateExamScreen(viewModel: MainViewModel) {
    var examName by remember { mutableStateOf("Pre-Board Examination") }
    var className by remember { mutableStateOf("10th") }
    var section by remember { mutableStateOf("A") }
    var subject by remember { mutableStateOf("Computer Science") }
    var date by remember { mutableStateOf("2026-09-26") }
    var session by remember { mutableStateOf("2026-27") }
    var totalMarks by remember { mutableStateOf("50") }
    var passingMarks by remember { mutableStateOf("25") }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Create Examination",
                subtitle = "Set up new test details",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) }
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
            OutlinedTextField(
                value = examName,
                onValueChange = { examName = it },
                label = { Text("Exam Name") },
                placeholder = { Text("e.g. Mid Term Examination") },
                modifier = Modifier.fillMaxWidth().testTag("exam_name_input"),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class") },
                    modifier = Modifier.weight(1f).testTag("exam_class_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section") },
                    modifier = Modifier.weight(1f).testTag("exam_section_input"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth().testTag("exam_subject_input"),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { totalMarks = it },
                    label = { Text("Total Marks") },
                    modifier = Modifier.weight(1f).testTag("exam_total_marks_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = passingMarks,
                    onValueChange = { passingMarks = it },
                    label = { Text("Passing Marks") },
                    modifier = Modifier.weight(1f).testTag("exam_passing_marks_input"),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    label = { Text("Session") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.createExam(
                        name = examName,
                        className = className,
                        section = section,
                        subject = subject,
                        date = date,
                        session = session,
                        totalMarks = totalMarks.toDoubleOrNull() ?: 50.0,
                        passingMarks = passingMarks.toDoubleOrNull() ?: 25.0
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_exam_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Exam & Add Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExamDetailScreen(viewModel: MainViewModel, examId: Long) {
    val exam by viewModel.selectedExam.collectAsStateWithLifecycle()
    val questions by viewModel.selectedExamQuestions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppHeader(
                title = exam?.examName ?: "Exam Details",
                subtitle = "Class ${exam?.className}-${exam?.section} • ${exam?.subject}",
                onBackClick = { viewModel.navigateTo(AppScreen.ExamList) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.AddQuestion(examId)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Question") },
                containerColor = AcademicNavyPrimary,
                modifier = Modifier.testTag("add_question_fab")
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Answer Key & Marking Scheme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${questions.size} Questions Defined • Total: ${questions.sumOf { it.maximumMarks }} Marks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.selectExam(examId)
                                    viewModel.navigateTo(AppScreen.ScanSheet)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Sheets")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Question Paper & Rubrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(questions) { q ->
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
                                text = "Q${q.questionNumber} (${q.questionType})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AcademicNavyPrimary
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("${q.maximumMarks} Marks", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (q.expectedAnswer.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Expected Answer / Rubric:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AcademicSecondary
                                    )
                                    Text(
                                        text = q.expectedAnswer,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestionScreen(viewModel: MainViewModel, examId: Long) {
    var questionNumber by remember { mutableStateOf("6") }
    var questionType by remember { mutableStateOf("Short Question") }
    var questionText by remember { mutableStateOf("") }
    var maximumMarks by remember { mutableStateOf("3") }
    var expectedAnswer by remember { mutableStateOf("") }
    var importantConcepts by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var requiredPoints by remember { mutableStateOf("") }

    // Dynamic criteria list
    val criteriaList = remember {
        mutableStateListOf(
            MarkingCriterion(questionId = 0, criterion = "Core definition", maximumMarks = 1.0),
            MarkingCriterion(questionId = 0, criterion = "Key concept/explanation", maximumMarks = 1.0),
            MarkingCriterion(questionId = 0, criterion = "Accurate example/application", maximumMarks = 1.0)
        )
    }

    val questionTypes = listOf("MCQ", "Short Question", "Long Question", "Numerical", "Programming", "Fill in the Blank", "True/False")

    Scaffold(
        topBar = {
            AppHeader(
                title = "Add Question & Rubric",
                subtitle = "Define answer key and marking scheme",
                onBackClick = { viewModel.navigateTo(AppScreen.ExamDetail(examId)) }
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = questionNumber,
                    onValueChange = { questionNumber = it },
                    label = { Text("Q. No") },
                    modifier = Modifier.weight(1f).testTag("question_number_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maximumMarks,
                    onValueChange = { maximumMarks = it },
                    label = { Text("Max Marks") },
                    modifier = Modifier.weight(1f).testTag("question_max_marks_input"),
                    singleLine = true
                )
            }

            Text("Question Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                questionTypes.take(4).forEach { type ->
                    FilterChip(
                        selected = questionType == type,
                        onClick = { questionType = type },
                        label = { Text(type, fontSize = 11.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text") },
                placeholder = { Text("e.g. Explain cache memory hierarchy") },
                modifier = Modifier.fillMaxWidth().testTag("question_text_input"),
                minLines = 2
            )

            HorizontalDivider()

            Text(
                text = "Teacher Answer Key & Concepts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AcademicNavyPrimary
            )

            OutlinedTextField(
                value = expectedAnswer,
                onValueChange = { expectedAnswer = it },
                label = { Text("Expected Teacher Answer") },
                placeholder = { Text("The complete ideal response for full marks") },
                modifier = Modifier.fillMaxWidth().testTag("expected_answer_input"),
                minLines = 3
            )

            OutlinedTextField(
                value = importantConcepts,
                onValueChange = { importantConcepts = it },
                label = { Text("Important Concepts (Evaluated by AI)") },
                placeholder = { Text("e.g. L1/L2 speed hierarchy, temporal/spatial locality") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text("Required Keywords") },
                placeholder = { Text("e.g. SRAM, latency, cache hit, bus speed") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marking Scheme Criteria",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AcademicSecondary
                )
                TextButton(onClick = {
                    criteriaList.add(MarkingCriterion(questionId = 0, criterion = "Additional criterion", maximumMarks = 1.0))
                }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Add Criterion")
                }
            }

            criteriaList.forEachIndexed { index, crit ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = crit.criterion,
                        onValueChange = { updated ->
                            criteriaList[index] = crit.copy(criterion = updated)
                        },
                        label = { Text("Criterion ${index + 1}") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = "${crit.maximumMarks}",
                        onValueChange = { updatedMarks ->
                            val m = updatedMarks.toDoubleOrNull() ?: 1.0
                            criteriaList[index] = crit.copy(maximumMarks = m)
                        },
                        label = { Text("Marks") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (criteriaList.size > 1) criteriaList.removeAt(index)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val qNum = questionNumber.toIntOrNull() ?: 1
                    val maxM = maximumMarks.toDoubleOrNull() ?: 3.0
                    viewModel.addQuestionWithCriteria(
                        examId = examId,
                        questionNumber = qNum,
                        questionText = questionText,
                        questionType = questionType,
                        maximumMarks = maxM,
                        expectedAnswer = expectedAnswer,
                        importantConcepts = importantConcepts,
                        keywords = keywords,
                        requiredPoints = requiredPoints,
                        criteria = criteriaList.toList()
                    )
                    viewModel.navigateTo(AppScreen.ExamDetail(examId))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_question_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Question & Scheme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
