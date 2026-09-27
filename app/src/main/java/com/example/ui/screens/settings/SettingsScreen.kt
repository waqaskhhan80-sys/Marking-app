package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppHeader
import com.example.ui.theme.AcademicNavyPrimary
import com.example.ui.theme.ConfidenceHighGreen
import com.example.ui.theme.ConfidenceMediumAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val teacher by viewModel.currentTeacher.collectAsStateWithLifecycle()
    val isOnline by viewModel.isAiOnline.collectAsStateWithLifecycle()
    val userKey by viewModel.userApiKey.collectAsStateWithLifecycle()

    var apiKeyInput by remember(userKey) { mutableStateOf(userKey) }
    var schoolNameInput by remember(teacher) { mutableStateOf(teacher?.schoolName ?: "Model Science High School") }
    var teacherNameInput by remember(teacher) { mutableStateOf(teacher?.name ?: "Prof. Waqas Khan") }
    var defaultPassingPercentage by remember { mutableStateOf("50%") }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Settings & Configuration",
                subtitle = "School Profile, Grade System & AI Engine",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Teacher & School Profile
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "School & Evaluator Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )

                    OutlinedTextField(
                        value = teacherNameInput,
                        onValueChange = { teacherNameInput = it },
                        label = { Text("Teacher / Evaluator Name") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_teacher_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = schoolNameInput,
                        onValueChange = { schoolNameInput = it },
                        label = { Text("School / Institute Name") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_school_name_input"),
                        singleLine = true
                    )
                }
            }

            // 2. AI Engine & API Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Services & OCR Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AcademicNavyPrimary
                        )

                        Badge(
                            containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (isOnline) "🟢 Active" else "🔴 Local Fallback",
                                color = if (isOnline) ConfidenceHighGreen else ConfidenceMediumAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Text(
                        text = "The application supports both online Gemini 3.5 multimodal handwriting recognition and an integrated offline rubric engine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("AI Studio Secret / Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.saveUserApiKey(apiKeyInput) },
                        modifier = Modifier.fillMaxWidth().testTag("save_api_key_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save API Key")
                    }
                }
            }

            // 3. Configurable Grade System
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Examination Grade Boundaries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavyPrimary
                    )

                    GradeBoundaryRow("A+", "90% – 100%", "Outstanding")
                    GradeBoundaryRow("A", "80% – 89%", "Excellent")
                    GradeBoundaryRow("B", "70% – 79%", "Very Good")
                    GradeBoundaryRow("C", "60% – 69%", "Good")
                    GradeBoundaryRow("D", "50% – 59%", "Satisfactory")
                    GradeBoundaryRow("E", "40% – 49%", "Passing")
                    GradeBoundaryRow("F", "Below 40%", "Needs Improvement")
                }
            }

            // 4. Data Management & Reset
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Data Management & Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { viewModel.reloadDemoData() },
                        modifier = Modifier.fillMaxWidth().testTag("reload_demo_data_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reload 10th-A Demo Examination")
                    }

                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth().testTag("logout_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out Teacher")
                    }
                }
            }
        }
    }
}

@Composable
fun GradeBoundaryRow(grade: String, range: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Badge(containerColor = AcademicNavyPrimary.copy(alpha = 0.1f)) {
            Text(grade, fontWeight = FontWeight.Bold, color = AcademicNavyPrimary)
        }
        Text(range, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
