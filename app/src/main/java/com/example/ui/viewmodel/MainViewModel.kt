package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AIEvaluationEngine
import com.example.data.ai.GeminiClient
import com.example.data.local.AppDatabase
import com.example.data.local.DemoDataInitializer
import com.example.data.model.*
import com.example.data.pdf.CsvExportManager
import com.example.data.pdf.PdfReportGenerator
import com.example.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val examRepository = ExamRepository(database.examDao(), database.questionDao())
    private val studentRepository = StudentRepository(database.studentDao())
    private val evaluationRepository = EvaluationRepository(
        database.answerSheetDao(),
        database.evaluationDao(),
        database.questionDao(),
        database.studentDao(),
        database.examDao()
    )
    private val authRepository = AuthRepository(database.teacherDao())

    // SharedPreferences for settings
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Current Teacher / Auth state
    private val _currentTeacher = MutableStateFlow<Teacher?>(null)
    val currentTeacher: StateFlow<Teacher?> = _currentTeacher.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Custom API Key from settings if user enters one
    private val _userApiKey = MutableStateFlow(prefs.getString("user_api_key", "") ?: "")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    private val aiEngine: AIEvaluationEngine
        get() = AIEvaluationEngine(_userApiKey.value.ifBlank { null })

    // Online AI Status
    val isAiOnline: StateFlow<Boolean> = MutableStateFlow(
        GeminiClient.hasApiKey() || _userApiKey.value.isNotBlank()
    ).asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected Entities for Detail Views
    private val _selectedExamId = MutableStateFlow<Long?>(1L)
    val selectedExamId: StateFlow<Long?> = _selectedExamId.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long?>(1L)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    // Database flows
    val allExams: StateFlow<List<Exam>> = examRepository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = studentRepository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReviewsCount: StateFlow<Int> = evaluationRepository.pendingReviewsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Selected Exam Questions
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedExamQuestions: StateFlow<List<Question>> = _selectedExamId
        .flatMapLatest { id ->
            if (id != null) examRepository.getQuestionsForExam(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Exam Details
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedExam: StateFlow<Exam?> = _selectedExamId
        .flatMapLatest { id ->
            if (id != null) examRepository.getExamByIdFlow(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Processing Pipeline State for Scanning
    private val _processingState = MutableStateFlow(ProcessingState())
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    // Active Student Exam Evaluation Summary
    private val _currentResultSummary = MutableStateFlow<StudentExamResultSummary?>(null)
    val currentResultSummary: StateFlow<StudentExamResultSummary?> = _currentResultSummary.asStateFlow()

    // Review List of Questions
    private val _reviewQuestions = MutableStateFlow<List<FullQuestionEvaluation>>(emptyList())
    val reviewQuestions: StateFlow<List<FullQuestionEvaluation>> = _reviewQuestions.asStateFlow()

    // Current Side-by-Side Question under review
    private val _activeReviewQuestion = MutableStateFlow<FullQuestionEvaluation?>(null)
    val activeReviewQuestion: StateFlow<FullQuestionEvaluation?> = _activeReviewQuestion.asStateFlow()

    // Scaffold Snackbar message
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        // Initialize Demo Data automatically if needed
        viewModelScope.launch {
            DemoDataInitializer.populateDemoData(getApplication(), database)
            val teacher = authRepository.getCurrentTeacher()
            if (teacher != null) {
                _currentTeacher.value = teacher
                _isAuthenticated.value = true
            }
            loadEvaluationForStudent(1, 1)
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectExam(examId: Long) {
        _selectedExamId.value = examId
    }

    fun selectStudent(studentId: Long) {
        _selectedStudentId.value = studentId
    }

    // --- Authentication ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val teacher = authRepository.login(email, pass)
            if (teacher != null) {
                _currentTeacher.value = teacher
                _isAuthenticated.value = true
                _currentScreen.value = AppScreen.Dashboard
                _snackbarMessage.emit("Welcome back, ${teacher.name}!")
            } else {
                _snackbarMessage.emit("Invalid credentials. Try demo login.")
            }
        }
    }

    fun register(name: String, school: String, email: String, pass: String) {
        viewModelScope.launch {
            val teacher = authRepository.register(name, school, email, pass)
            _currentTeacher.value = teacher
            _isAuthenticated.value = true
            _currentScreen.value = AppScreen.Dashboard
            _snackbarMessage.emit("Account created successfully!")
        }
    }

    fun loginAsDemo() {
        viewModelScope.launch {
            DemoDataInitializer.populateDemoData(getApplication(), database, force = false)
            val teacher = authRepository.getCurrentTeacher()
            _currentTeacher.value = teacher
            _isAuthenticated.value = true
            _currentScreen.value = AppScreen.Dashboard
            _snackbarMessage.emit("Logged in Demo Mode. Sample 10th-A exam loaded!")
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentScreen.value = AppScreen.Auth
    }

    // --- Exam Management ---
    fun createExam(
        name: String,
        className: String,
        section: String,
        subject: String,
        date: String,
        session: String,
        totalMarks: Double,
        passingMarks: Double
    ) {
        viewModelScope.launch {
            val exam = Exam(
                examName = name,
                className = className,
                section = section,
                subject = subject,
                date = date,
                session = session,
                totalMarks = totalMarks,
                passingMarks = passingMarks
            )
            val id = examRepository.createExam(exam)
            _selectedExamId.value = id
            _currentScreen.value = AppScreen.ExamDetail(id)
            _snackbarMessage.emit("Exam created! Add questions and answer keys.")
        }
    }

    fun addQuestionWithCriteria(
        examId: Long,
        questionNumber: Int,
        questionText: String,
        questionType: String,
        maximumMarks: Double,
        expectedAnswer: String,
        importantConcepts: String,
        keywords: String,
        requiredPoints: String,
        criteria: List<MarkingCriterion>
    ) {
        viewModelScope.launch {
            val q = Question(
                examId = examId,
                questionNumber = questionNumber,
                questionText = questionText,
                questionType = questionType,
                maximumMarks = maximumMarks,
                expectedAnswer = expectedAnswer,
                importantConcepts = importantConcepts,
                keywords = keywords,
                requiredPoints = requiredPoints
            )
            examRepository.addQuestion(q, criteria)
            _snackbarMessage.emit("Question $questionNumber added successfully!")
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            examRepository.deleteQuestion(question)
            _snackbarMessage.emit("Question deleted.")
        }
    }

    // --- Student Management ---
    fun addStudent(name: String, rollNumber: String, className: String, section: String) {
        viewModelScope.launch {
            val s = Student(name = name, rollNumber = rollNumber, className = className, section = section)
            studentRepository.addStudent(s)
            _snackbarMessage.emit("Student $name added!")
        }
    }

    // --- Scanning & Evaluation Pipeline ---
    fun startEvaluationPipeline(
        examId: Long,
        studentId: Long,
        imagePaths: List<String>
    ) {
        _selectedExamId.value = examId
        _selectedStudentId.value = studentId
        _currentScreen.value = AppScreen.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _processingState.value = ProcessingState(
                    step = 1,
                    statusText = "Preprocessing & enhancing ${imagePaths.size} answer sheet page(s)...",
                    progress = 0.2f
                )
                kotlinx.coroutines.delay(1000)

                // Save AnswerSheet records
                imagePaths.forEachIndexed { index, path ->
                    evaluationRepository.saveAnswerSheet(
                        AnswerSheet(
                            studentId = studentId,
                            examId = examId,
                            pageNumber = index + 1,
                            originalImagePath = path,
                            processedImagePath = path
                        )
                    )
                }

                _processingState.value = ProcessingState(
                    step = 2,
                    statusText = "Performing AI Handwriting OCR recognition...",
                    progress = 0.45f
                )

                val questions = examRepository.getQuestionsForExamSync(examId)
                val firstPage = imagePaths.firstOrNull() ?: ""

                // AI Handwriting OCR
                val ocrSegments = aiEngine.performHandwritingOcr(firstPage, questions)

                _processingState.value = ProcessingState(
                    step = 3,
                    statusText = "Detecting question boundaries and segmenting answers...",
                    progress = 0.65f
                )
                kotlinx.coroutines.delay(800)

                // Create Student Answers
                val studentAnswers = ocrSegments.map { seg ->
                    val matchedQ = questions.firstOrNull { it.questionNumber == seg.questionNumber }
                    StudentAnswer(
                        answerSheetId = 1,
                        examId = examId,
                        studentId = studentId,
                        questionId = matchedQ?.id ?: seg.questionNumber.toLong(),
                        extractedText = seg.extractedText,
                        ocrConfidence = seg.confidence,
                        isQuestionIdentified = seg.isQuestionIdentified,
                        pageNumber = seg.pageNumber
                    )
                }
                val answerIds = evaluationRepository.saveStudentAnswers(studentAnswers)

                _processingState.value = ProcessingState(
                    step = 4,
                    statusText = "Evaluating concepts against teacher marking rubric...",
                    progress = 0.85f
                )

                // AI Answer Evaluation against criteria
                val finalMarksToSave = mutableListOf<FinalMark>()
                for ((index, ans) in studentAnswers.withIndex()) {
                    val q = questions.firstOrNull { it.id == ans.questionId } ?: continue
                    val criteria = examRepository.getCriteriaForQuestionSync(q.id)
                    val evalResult = aiEngine.evaluateAnswer(q, criteria, ans.extractedText)

                    val savedAnsId = answerIds.getOrElse(index) { ans.id }
                    evaluationRepository.saveAIEvaluation(
                        AIEvaluation(
                            studentAnswerId = savedAnsId,
                            suggestedMarks = evalResult.suggestedMarks,
                            confidence = evalResult.confidence,
                            explanation = evalResult.overallReason,
                            evaluationJson = "{}",
                            requiresTeacherReview = evalResult.requiresTeacherReview
                        )
                    )

                    finalMarksToSave.add(
                        FinalMark(
                            studentAnswerId = savedAnsId,
                            examId = examId,
                            studentId = studentId,
                            questionId = q.id,
                            aiSuggestedMarks = evalResult.suggestedMarks,
                            teacherFinalMarks = evalResult.suggestedMarks,
                            teacherComment = evalResult.overallReason,
                            reviewed = !evalResult.requiresTeacherReview,
                            finalized = false
                        )
                    )
                }

                evaluationRepository.saveFinalMarks(finalMarksToSave)

                _processingState.value = ProcessingState(
                    step = 5,
                    statusText = "Generating question-wise marks for Teacher Review...",
                    progress = 1.0f,
                    isCompleted = true
                )
                kotlinx.coroutines.delay(600)

                // Navigate to Teacher Review Screen
                loadEvaluationForStudent(examId, studentId)
                _currentScreen.value = AppScreen.TeacherReview(examId, studentId)

            } catch (e: Exception) {
                _processingState.value = ProcessingState(
                    step = 1,
                    statusText = "Error during processing: ${e.message}",
                    progress = 0f,
                    isError = true
                )
            }
        }
    }

    // --- Teacher Review & Manual Override ---
    fun loadEvaluationForStudent(examId: Long, studentId: Long) {
        viewModelScope.launch {
            val list = evaluationRepository.getFullQuestionEvaluations(examId, studentId)
            _reviewQuestions.value = list
            _currentResultSummary.value = evaluationRepository.getStudentExamResultSummary(examId, studentId)
        }
    }

    fun openSideBySideReview(qEval: FullQuestionEvaluation) {
        _activeReviewQuestion.value = qEval
        _currentScreen.value = AppScreen.SideBySideReview
    }

    fun updateTeacherMark(
        studentAnswerId: Long,
        examId: Long,
        studentId: Long,
        questionId: Long,
        aiSuggestedMarks: Double,
        newTeacherFinalMarks: Double,
        comment: String,
        updatedOcrText: String? = null
    ) {
        viewModelScope.launch {
            if (!updatedOcrText.isNullOrBlank()) {
                val studentAns = StudentAnswer(
                    id = studentAnswerId,
                    answerSheetId = 1,
                    examId = examId,
                    studentId = studentId,
                    questionId = questionId,
                    extractedText = updatedOcrText,
                    ocrConfidence = 1.0,
                    isQuestionIdentified = true
                )
                evaluationRepository.updateStudentAnswer(studentAns)
            }

            val finalMark = FinalMark(
                studentAnswerId = studentAnswerId,
                examId = examId,
                studentId = studentId,
                questionId = questionId,
                aiSuggestedMarks = aiSuggestedMarks, // Preserved!
                teacherFinalMarks = newTeacherFinalMarks,
                teacherComment = comment,
                reviewed = true,
                finalized = false
            )
            evaluationRepository.updateFinalMark(finalMark)
            loadEvaluationForStudent(examId, studentId)
            _snackbarMessage.emit("Marks saved! Teacher final mark: $newTeacherFinalMarks")
        }
    }

    fun finalizeStudentMarks(examId: Long, studentId: Long) {
        viewModelScope.launch {
            evaluationRepository.finalizeAllMarksForStudent(examId, studentId)
            loadEvaluationForStudent(examId, studentId)
            _currentScreen.value = AppScreen.StudentResult(examId, studentId)
            _snackbarMessage.emit("Marks officially finalized by teacher!")
        }
    }

    // --- Export PDF & CSV ---
    fun exportAndSharePdf(context: Context, summary: StudentExamResultSummary) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = PdfReportGenerator.generateStudentReportPdf(context, summary)
                withContext(Dispatchers.Main) {
                    PdfReportGenerator.sharePdf(context, file)
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to generate PDF: ${e.message}")
            }
        }
    }

    fun exportAndShareClassCsv(context: Context, exam: Exam, summaries: List<StudentExamResultSummary>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = CsvExportManager.exportClassResultsToCsv(
                    context,
                    exam.examName,
                    exam.className,
                    exam.section,
                    summaries
                )
                withContext(Dispatchers.Main) {
                    CsvExportManager.shareCsv(context, file)
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to export CSV: ${e.message}")
            }
        }
    }

    // --- Settings & Reset ---
    fun saveUserApiKey(key: String) {
        prefs.edit().putString("user_api_key", key).apply()
        _userApiKey.value = key
        viewModelScope.launch {
            _snackbarMessage.emit("AI API Key saved.")
        }
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            DemoDataInitializer.populateDemoData(getApplication(), database, force = true)
            loadEvaluationForStudent(1, 1)
            _snackbarMessage.emit("Demo examination reset and reloaded!")
        }
    }
}

data class ProcessingState(
    val step: Int = 1,
    val statusText: String = "Ready",
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val isError: Boolean = false
)

sealed class AppScreen {
    object Auth : AppScreen()
    object Dashboard : AppScreen()
    object ExamList : AppScreen()
    object CreateExam : AppScreen()
    data class ExamDetail(val examId: Long) : AppScreen()
    data class AddQuestion(val examId: Long) : AppScreen()
    object ScanSheet : AppScreen()
    object Processing : AppScreen()
    data class TeacherReview(val examId: Long, val studentId: Long) : AppScreen()
    object SideBySideReview : AppScreen()
    data class StudentResult(val examId: Long, val studentId: Long) : AppScreen()
    object ClassResults : AppScreen()
    object StudentManagement : AppScreen()
    object BulkCheck : AppScreen()
    object Settings : AppScreen()
}
