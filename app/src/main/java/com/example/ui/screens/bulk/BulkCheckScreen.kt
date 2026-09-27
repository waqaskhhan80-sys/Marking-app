package com.example.ui.screens.bulk

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BulkCheckScreen(viewModel: MainViewModel) {
    var selectedBatchSize by remember { mutableIntStateOf(10) }
    var isProcessing by remember { mutableStateOf(false) }
    var processedCount by remember { mutableIntStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    val batchItems = remember {
        listOf(
            BulkStudentItem("01", "Muhammad Abdul Daim", "Class 10th-A", isIdentified = true, isCertain = true, marks = 48.0, total = 50.0),
            BulkStudentItem("02", "Ali Khan", "Class 10th-A", isIdentified = true, isCertain = true, marks = 42.5, total = 50.0),
            BulkStudentItem("03", "Ahmed Raza", "Class 10th-A", isIdentified = true, isCertain = true, marks = 38.0, total = 50.0),
            BulkStudentItem("04", "Hassan Tariq", "Class 10th-A", isIdentified = true, isCertain = true, marks = 45.5, total = 50.0),
            BulkStudentItem("??", "Unrecognized Header", "Class 10th-A", isIdentified = false, isCertain = false, marks = 35.0, total = 50.0)
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Bulk Examination Check",
                subtitle = "Batch Process Multi-Student Papers",
                onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) }
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
            // Batch Size Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Batch Examination Size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upload scanned answer sheets for multiple students. The AI pipeline will identify student headers, extract questions, and evaluate against the rubric.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 20, 30, 50).forEach { size ->
                            FilterChip(
                                selected = selectedBatchSize == size,
                                onClick = { selectedBatchSize = size },
                                label = { Text("$size Papers", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Start Batch Button
            Button(
                onClick = {
                    isProcessing = true
                    showResults = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_bulk_check_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicNavyPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Bulk AI Evaluation ($selectedBatchSize Papers)", fontWeight = FontWeight.Bold)
            }

            if (showResults) {
                Text(
                    text = "Batch Results (${batchItems.size} Answer Sheets)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(batchItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!item.isCertain) ConfidenceLowRedBg.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (!item.isCertain) androidx.compose.foundation.BorderStroke(1.dp, ConfidenceLowRed) else null
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${item.rollNumber} - ${item.name}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.className,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (item.isCertain) {
                                        Text(
                                            text = "${item.marks} / ${item.total.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            color = ConfidenceHighGreen
                                        )
                                    } else {
                                        Badge(containerColor = ConfidenceLowRed) {
                                            Text("Review", color = Color.White)
                                        }
                                    }
                                }

                                if (!item.isCertain) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ConfidenceLowRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "⚠️ Student Identification Required — Tap to map roll number",
                                            color = ConfidenceLowRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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
}

data class BulkStudentItem(
    val rollNumber: String,
    val name: String,
    val className: String,
    val isIdentified: Boolean,
    val isCertain: Boolean,
    val marks: Double,
    val total: Double
)
