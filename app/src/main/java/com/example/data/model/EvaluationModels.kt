package com.example.data.model

data class CriterionScore(
    val criterion: String,
    val maximumMarks: Double,
    val awardedMarks: Double,
    val reason: String
)

data class EvaluationResult(
    val questionNumber: Int,
    val maximumMarks: Double,
    val suggestedMarks: Double,
    val confidence: Double, // 0.0 to 1.0
    val criteria: List<CriterionScore>,
    val overallReason: String,
    val requiresTeacherReview: Boolean
)

data class OCRAnswerSegment(
    val questionNumber: Int,
    val extractedText: String,
    val confidence: Double,
    val pageNumber: Int,
    val isQuestionIdentified: Boolean = true
)

data class FullQuestionEvaluation(
    val question: Question,
    val criteria: List<MarkingCriterion>,
    val studentAnswer: StudentAnswer?,
    val aiEvaluation: AIEvaluation?,
    val finalMark: FinalMark?
)

data class StudentExamResultSummary(
    val student: Student,
    val exam: Exam,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val percentage: Double,
    val grade: String,
    val passed: Boolean,
    val correctCount: Int,
    val partialCount: Int,
    val incorrectCount: Int,
    val questionEvaluations: List<FullQuestionEvaluation>,
    val isFinalized: Boolean
)

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

fun Double.toConfidenceLevel(): ConfidenceLevel {
    return when {
        this >= 0.85 -> ConfidenceLevel.HIGH
        this >= 0.70 -> ConfidenceLevel.MEDIUM
        else -> ConfidenceLevel.LOW
    }
}
