package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val teacher by viewModel.currentTeacher.collectAsStateWithLifecycle()
    val isOnline by viewModel.isAiOnline.collectAsStateWithLifecycle()
    val pendingReviews by viewModel.pendingReviewsCount.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI ANSWER SHEET CHECKER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AcademicNavyPrimary
                        )
                        Text(
                            text = teacher?.schoolName ?: "Smart Exam Evaluation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Settings) },
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Online / Offline AI Status Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                StatusBanner(isOnline = isOnline)
            }

            // 2. Statistics Grid
            item {
                Text(
                    text = "Overview & Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Students",
                        value = "${allStudents.size}",
                        icon = Icons.Default.Groups,
                        color = AcademicNavyPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentManagement) }
                    )
                    StatCard(
                        title = "Exams",
                        value = "${allExams.size}",
                        icon = Icons.Default.Description,
                        color = AcademicTertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ExamList) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Papers Checked",
                        value = "4",
                        icon = Icons.Default.CheckCircle,
                        color = ConfidenceHighGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ClassResults) }
                    )
                    StatCard(
                        title = "Pending Reviews",
                        value = "$pendingReviews",
                        icon = Icons.Default.RateReview,
                        color = if (pendingReviews > 0) ConfidenceMediumAmber else AcademicSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.loadEvaluationForStudent(1, 1)
                            viewModel.navigateTo(AppScreen.TeacherReview(1, 1))
                        }
                    )
                }
            }

            // 3. Primary Actions Grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Create Exam",
                            subtitle = "Add paper & marking scheme",
                            icon = Icons.Default.EditNote,
                            color = AcademicNavyPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.CreateExam) }
                        )
                        ActionCard(
                            title = "Scan Sheet",
                            subtitle = "Camera / gallery upload",
                            icon = Icons.Default.CameraAlt,
                            color = AcademicSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.ScanSheet) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "AI Check & Review",
                            subtitle = "Evaluate with teacher rubric",
                            icon = Icons.Default.AutoAwesome,
                            color = AcademicTertiary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.loadEvaluationForStudent(1, 1)
                                viewModel.navigateTo(AppScreen.TeacherReview(1, 1))
                            }
                        )
                        ActionCard(
                            title = "Bulk Check",
                            subtitle = "Batch grade multiple sheets",
                            icon = Icons.Default.Layers,
                            color = Color(0xFFE65100),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.BulkCheck) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Class Results",
                            subtitle = "Score tables & analytics",
                            icon = Icons.Default.Assessment,
                            color = Color(0xFF00695C),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.ClassResults) }
                        )
                        ActionCard(
                            title = "Students List",
                            subtitle = "Manage rolls & sections",
                            icon = Icons.Default.People,
                            color = Color(0xFF283593),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.StudentManagement) }
                        )
                    }
                }
            }

            // 4. Recent Examinations
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Examinations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.ExamList) }) {
                        Text("View All")
                    }
                }
            }

            items(allExams) { exam ->
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
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exam.examName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Class ${exam.className}-${exam.section}  •  ${exam.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total Marks: ${exam.totalMarks.toInt()}  |  Passing: ${exam.passingMarks.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AcademicNavyPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.selectExam(exam.id)
                                viewModel.navigateTo(AppScreen.ScanSheet)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan", fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatusBanner(isOnline: Boolean) {
    val (bgColor, borderColor, dotColor, text) = if (isOnline) {
        Quadruple(
            ConfidenceHighGreenBg,
            ConfidenceHighGreen.copy(alpha = 0.4f),
            ConfidenceHighGreen,
            "🟢 Online — AI & Gemini Handwriting OCR available"
        )
    } else {
        Quadruple(
            ConfidenceMediumAmberBg,
            ConfidenceMediumAmber.copy(alpha = 0.4f),
            ConfidenceMediumAmber,
            "🔴 Offline Mode — Manual marking & local rubric evaluation active"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
