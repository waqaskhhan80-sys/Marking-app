package com.example.ui.screens.scan

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.AppHeader
import com.example.ui.theme.AcademicNavyPrimary
import com.example.ui.theme.ConfidenceHighGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAnswerSheetScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val exams by viewModel.allExams.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val selectedExamId by viewModel.selectedExamId.collectAsStateWithLifecycle()
    val selectedStudentId by viewModel.selectedStudentId.collectAsStateWithLifecycle()

    var examExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }

    // Preloaded sample sheet path
    val defaultSampleSheet = remember {
        val sample = File(context.filesDir, "answer_sheets/student_01_page_1.png")
        if (sample.exists()) sample.absolutePath else ""
    }

    // Scanned pages list
    val scannedPages = remember { mutableStateListOf(defaultSampleSheet) }
    var activePageIndex by remember { mutableIntStateOf(0) }

    // Image processing toggles
    var contrastBoost by remember { mutableStateOf(true) }
    var shadowReduction by remember { mutableStateOf(true) }
    var autoPerspectiveCrop by remember { mutableStateOf(true) }

    val currentExam = exams.firstOrNull { it.id == selectedExamId } ?: exams.firstOrNull()
    val currentStudent = students.firstOrNull { it.id == selectedStudentId } ?: students.firstOrNull()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Scan Answer Sheet",
                subtitle = "Handwriting OCR & Document Capture",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selectors row: Exam & Student
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Exam Selector
                ExposedDropdownMenuBox(
                    expanded = examExpanded,
                    onExpandedChange = { examExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = currentExam?.examName ?: "Select Exam",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exam") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = examExpanded,
                        onDismissRequest = { examExpanded = false }
                    ) {
                        exams.forEach { e ->
                            DropdownMenuItem(
                                text = { Text(e.examName) },
                                onClick = {
                                    viewModel.selectExam(e.id)
                                    examExpanded = false
                                }
                            )
                        }
                    }
                }

                // Student Selector
                ExposedDropdownMenuBox(
                    expanded = studentExpanded,
                    onExpandedChange = { studentExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = currentStudent?.let { "${it.rollNumber} - ${it.name}" } ?: "Select Student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Student") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = studentExpanded,
                        onDismissRequest = { studentExpanded = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.rollNumber} - ${s.name}") },
                                onClick = {
                                    viewModel.selectStudent(s.id)
                                    studentExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Scanning Viewport with document alignment frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val activePath = scannedPages.getOrNull(activePageIndex) ?: ""

                    if (activePath.isNotBlank() && File(activePath).exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(File(activePath)),
                            contentDescription = "Scanned Page",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Align answer sheet inside the frame",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Multi-page handwriting recognition enabled",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Viewport guidelines overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                    )

                    // Page tag badge
                    Badge(
                        containerColor = AcademicNavyPrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Page ${activePageIndex + 1} of ${scannedPages.size}",
                            color = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    // Alignment hint
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "📷 Document boundaries detected & cropped",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Thumbnail strip for Multi-page
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(scannedPages) { index, path ->
                    Box(
                        modifier = Modifier
                            .size(60.dp, 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp,
                                if (index == activePageIndex) AcademicNavyPrimary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { activePageIndex = index }
                    ) {
                        if (path.isNotBlank() && File(path).exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(File(path)),
                                contentDescription = "Page thumbnail",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "P${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Add Page Button
                item {
                    OutlinedButton(
                        onClick = {
                            scannedPages.add(defaultSampleSheet)
                            activePageIndex = scannedPages.lastIndex
                        },
                        modifier = Modifier.size(60.dp, 80.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add page", modifier = Modifier.size(20.dp))
                            Text("+ Page", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Image Processing filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = autoPerspectiveCrop,
                    onClick = { autoPerspectiveCrop = !autoPerspectiveCrop },
                    label = { Text("Auto Crop", fontSize = 11.sp) },
                    leadingIcon = { if (autoPerspectiveCrop) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) else null }
                )
                FilterChip(
                    selected = shadowReduction,
                    onClick = { shadowReduction = !shadowReduction },
                    label = { Text("Reduce Shadows", fontSize = 11.sp) },
                    leadingIcon = { if (shadowReduction) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) else null }
                )
                FilterChip(
                    selected = contrastBoost,
                    onClick = { contrastBoost = !contrastBoost },
                    label = { Text("Contrast Boost", fontSize = 11.sp) },
                    leadingIcon = { if (contrastBoost) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) else null }
                )
            }

            // Primary Start AI Check Button
            Button(
                onClick = {
                    val eId = currentExam?.id ?: 1L
                    val sId = currentStudent?.id ?: 1L
                    val validPages = scannedPages.filter { it.isNotBlank() }
                    viewModel.startEvaluationPipeline(eId, sId, if (validPages.isNotEmpty()) validPages else listOf(defaultSampleSheet))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_ai_evaluation_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("🤖 START AI EVALUATION", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProcessingScreen(viewModel: MainViewModel) {
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(AcademicNavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AcademicNavyPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Checking Answer Sheet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AcademicNavyPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = processingState.statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { processingState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AcademicNavyPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProcessingStepItem(title = "Image processing & boundary correction", isDone = processingState.step > 1, isActive = processingState.step == 1)
                    ProcessingStepItem(title = "Handwriting OCR recognition", isDone = processingState.step > 2, isActive = processingState.step == 2)
                    ProcessingStepItem(title = "Question detection & segmentation", isDone = processingState.step > 3, isActive = processingState.step == 3)
                    ProcessingStepItem(title = "AI evaluation against teacher criteria", isDone = processingState.step > 4, isActive = processingState.step == 4)
                    ProcessingStepItem(title = "Calculating question-wise marks", isDone = processingState.step >= 5, isActive = processingState.step == 5)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "🔒 Evaluated strictly using teacher marking rubrics. Handwriting appearance is never penalized.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProcessingStepItem(title: String, isDone: Boolean, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = ConfidenceHighGreen,
                modifier = Modifier.size(20.dp)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = AcademicNavyPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Default.RadioButtonUnchecked,
                contentDescription = "Pending",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
