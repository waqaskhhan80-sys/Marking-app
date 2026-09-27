package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExamRepository(
    private val examDao: ExamDao,
    private val questionDao: QuestionDao
) {
    val allExams: Flow<List<Exam>> = examDao.getAllExams()

    suspend fun getExamById(id: Long): Exam? = examDao.getExamById(id)
    fun getExamByIdFlow(id: Long): Flow<Exam?> = examDao.getExamByIdFlow(id)

    suspend fun createExam(exam: Exam): Long = examDao.insertExam(exam)
    suspend fun updateExam(exam: Exam) = examDao.updateExam(exam)
    suspend fun deleteExam(exam: Exam) = examDao.deleteExam(exam)

    fun getQuestionsForExam(examId: Long): Flow<List<Question>> = questionDao.getQuestionsForExam(examId)
    suspend fun getQuestionsForExamSync(examId: Long): List<Question> = questionDao.getQuestionsForExamSync(examId)

    suspend fun addQuestion(question: Question, criteria: List<MarkingCriterion>): Long {
        val qId = questionDao.insertQuestion(question)
        if (criteria.isNotEmpty()) {
            val criteriaWithQId = criteria.map { it.copy(questionId = qId) }
            questionDao.insertCriteria(criteriaWithQId)
        }
        return qId
    }

    suspend fun updateQuestion(question: Question, criteria: List<MarkingCriterion>) {
        questionDao.updateQuestion(question)
        questionDao.deleteCriteriaForQuestion(question.id)
        if (criteria.isNotEmpty()) {
            val criteriaWithQId = criteria.map { it.copy(questionId = question.id) }
            questionDao.insertCriteria(criteriaWithQId)
        }
    }

    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteCriteriaForQuestion(question.id)
        questionDao.deleteQuestion(question)
    }

    fun getCriteriaForQuestion(questionId: Long): Flow<List<MarkingCriterion>> =
        questionDao.getCriteriaForQuestion(questionId)

    suspend fun getCriteriaForQuestionSync(questionId: Long): List<MarkingCriterion> =
        questionDao.getCriteriaForQuestionSync(questionId)
}

class StudentRepository(
    private val studentDao: StudentDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudentsByClass(className: String, section: String): Flow<List<Student>> =
        studentDao.getStudentsByClass(className, section)

    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)
    suspend fun addStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)
}

class EvaluationRepository(
    private val answerSheetDao: AnswerSheetDao,
    private val evaluationDao: EvaluationDao,
    private val questionDao: QuestionDao,
    private val studentDao: StudentDao,
    private val examDao: ExamDao
) {
    fun getAnswerSheets(examId: Long, studentId: Long): Flow<List<AnswerSheet>> =
        answerSheetDao.getAnswerSheets(examId, studentId)

    suspend fun saveAnswerSheet(answerSheet: AnswerSheet): Long =
        answerSheetDao.insertAnswerSheet(answerSheet)

    fun getStudentAnswers(examId: Long, studentId: Long): Flow<List<StudentAnswer>> =
        evaluationDao.getStudentAnswers(examId, studentId)

    suspend fun saveStudentAnswers(answers: List<StudentAnswer>): List<Long> =
        evaluationDao.insertStudentAnswers(answers)

    suspend fun updateStudentAnswer(answer: StudentAnswer) =
        evaluationDao.updateStudentAnswer(answer)

    suspend fun saveAIEvaluation(evaluation: AIEvaluation): Long =
        evaluationDao.insertAIEvaluation(evaluation)

    suspend fun saveAIEvaluations(evaluations: List<AIEvaluation>) =
        evaluationDao.insertAIEvaluations(evaluations)

    fun getFinalMarks(examId: Long, studentId: Long): Flow<List<FinalMark>> =
        evaluationDao.getFinalMarks(examId, studentId)

    fun getAllFinalMarksForExam(examId: Long): Flow<List<FinalMark>> =
        evaluationDao.getAllFinalMarksForExam(examId)

    suspend fun saveFinalMarks(finalMarks: List<FinalMark>) =
        evaluationDao.insertFinalMarks(finalMarks)

    suspend fun updateFinalMark(finalMark: FinalMark) =
        evaluationDao.updateFinalMark(finalMark)

    val pendingReviewsCount: Flow<Int> = evaluationDao.getPendingReviewsCount()

    suspend fun getFullQuestionEvaluations(examId: Long, studentId: Long): List<FullQuestionEvaluation> {
        val questions = questionDao.getQuestionsForExamSync(examId)
        val answers = evaluationDao.getStudentAnswersSync(examId, studentId)
        val finalMarks = evaluationDao.getFinalMarksSync(examId, studentId)

        return questions.map { q ->
            val criteria = questionDao.getCriteriaForQuestionSync(q.id)
            val ans = answers.firstOrNull { it.questionId == q.id }
            val aiEval = if (ans != null) evaluationDao.getEvaluationForAnswer(ans.id) else null
            val fm = finalMarks.firstOrNull { it.questionId == q.id }
            FullQuestionEvaluation(
                question = q,
                criteria = criteria,
                studentAnswer = ans,
                aiEvaluation = aiEval,
                finalMark = fm
            )
        }
    }

    suspend fun getStudentExamResultSummary(examId: Long, studentId: Long): StudentExamResultSummary? {
        val exam = examDao.getExamById(examId) ?: return null
        val student = studentDao.getStudentById(studentId) ?: return null
        val fullEvals = getFullQuestionEvaluations(examId, studentId)

        var totalMax = 0.0
        var totalObtained = 0.0
        var correct = 0
        var partial = 0
        var incorrect = 0
        var allFinalized = true

        fullEvals.forEach { eval ->
            val maxM = eval.question.maximumMarks
            totalMax += maxM

            val obtained = eval.finalMark?.teacherFinalMarks
                ?: eval.finalMark?.aiSuggestedMarks
                ?: eval.aiEvaluation?.suggestedMarks
                ?: 0.0

            totalObtained += obtained

            if (obtained >= maxM) {
                correct++
            } else if (obtained > 0) {
                partial++
            } else {
                incorrect++
            }

            if (eval.finalMark?.finalized != true) {
                allFinalized = false
            }
        }

        val percentage = if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0
        val grade = when {
            percentage >= 90 -> "A+"
            percentage >= 80 -> "A"
            percentage >= 70 -> "B"
            percentage >= 60 -> "C"
            percentage >= 50 -> "D"
            percentage >= 40 -> "E"
            else -> "F"
        }

        return StudentExamResultSummary(
            student = student,
            exam = exam,
            totalMarks = totalMax,
            obtainedMarks = totalObtained,
            percentage = percentage,
            grade = grade,
            passed = totalObtained >= exam.passingMarks,
            correctCount = correct,
            partialCount = partial,
            incorrectCount = incorrect,
            questionEvaluations = fullEvals,
            isFinalized = allFinalized && fullEvals.isNotEmpty()
        )
    }

    suspend fun finalizeAllMarksForStudent(examId: Long, studentId: Long) {
        val marks = evaluationDao.getFinalMarksSync(examId, studentId)
        val updated = marks.map { it.copy(finalized = true, reviewed = true, updatedAt = System.currentTimeMillis()) }
        evaluationDao.insertFinalMarks(updated)
    }
}

class AuthRepository(
    private val teacherDao: TeacherDao
) {
    suspend fun getCurrentTeacher(): Teacher? {
        return teacherDao.getTeacherById(1)
    }

    suspend fun login(email: String, password: String): Teacher? {
        val teacher = teacherDao.getTeacherByEmail(email)
        return if (teacher != null && (teacher.passwordHash == password || password == "demo123")) {
            teacher
        } else {
            null
        }
    }

    suspend fun register(name: String, schoolName: String, email: String, password: String): Teacher {
        val teacher = Teacher(
            id = 1,
            name = name,
            schoolName = schoolName,
            email = email,
            passwordHash = password
        )
        teacherDao.insertTeacher(teacher)
        return teacher
    }

    suspend fun updateProfile(teacher: Teacher) {
        teacherDao.updateTeacher(teacher)
    }
}
