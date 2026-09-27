package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.bulk.BulkCheckScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.exam.AddQuestionScreen
import com.example.ui.screens.exam.CreateExamScreen
import com.example.ui.screens.exam.ExamDetailScreen
import com.example.ui.screens.exam.ExamListScreen
import com.example.ui.screens.results.ClassResultsScreen
import com.example.ui.screens.results.StudentResultScreen
import com.example.ui.screens.review.SideBySideReviewScreen
import com.example.ui.screens.review.TeacherReviewScreen
import com.example.ui.screens.scan.ProcessingScreen
import com.example.ui.screens.scan.ScanAnswerSheetScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.students.StudentManagementScreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = if (!isAuthenticated) AppScreen.Auth else currentScreen,
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    is AppScreen.Auth -> {
                        AuthScreen(viewModel = viewModel)
                    }

                    is AppScreen.Dashboard -> {
                        DashboardScreen(viewModel = viewModel)
                    }

                    is AppScreen.ExamList -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        ExamListScreen(viewModel = viewModel)
                    }

                    is AppScreen.CreateExam -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        CreateExamScreen(viewModel = viewModel)
                    }

                    is AppScreen.ExamDetail -> {
                        BackHandler { viewModel.navigateTo(AppScreen.ExamList) }
                        ExamDetailScreen(viewModel = viewModel, examId = screen.examId)
                    }

                    is AppScreen.AddQuestion -> {
                        BackHandler { viewModel.navigateTo(AppScreen.ExamDetail(screen.examId)) }
                        AddQuestionScreen(viewModel = viewModel, examId = screen.examId)
                    }

                    is AppScreen.ScanSheet -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        ScanAnswerSheetScreen(viewModel = viewModel)
                    }

                    is AppScreen.Processing -> {
                        // Keep on processing until complete
                        ProcessingScreen(viewModel = viewModel)
                    }

                    is AppScreen.TeacherReview -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        TeacherReviewScreen(viewModel = viewModel, examId = screen.examId, studentId = screen.studentId)
                    }

                    is AppScreen.SideBySideReview -> {
                        BackHandler {
                            val eId = viewModel.selectedExamId.value ?: 1L
                            val sId = viewModel.selectedStudentId.value ?: 1L
                            viewModel.navigateTo(AppScreen.TeacherReview(eId, sId))
                        }
                        SideBySideReviewScreen(viewModel = viewModel)
                    }

                    is AppScreen.StudentResult -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        StudentResultScreen(viewModel = viewModel, examId = screen.examId, studentId = screen.studentId)
                    }

                    is AppScreen.ClassResults -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        ClassResultsScreen(viewModel = viewModel)
                    }

                    is AppScreen.StudentManagement -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        StudentManagementScreen(viewModel = viewModel)
                    }

                    is AppScreen.BulkCheck -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        BulkCheckScreen(viewModel = viewModel)
                    }

                    is AppScreen.Settings -> {
                        BackHandler { viewModel.navigateTo(AppScreen.Dashboard) }
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
