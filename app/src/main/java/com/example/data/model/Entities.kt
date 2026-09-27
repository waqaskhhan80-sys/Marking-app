package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val schoolName: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long = 1,
    val examName: String,
    val className: String,
    val section: String,
    val subject: String,
    val date: String,
    val session: String = "2026-27",
    val totalMarks: Double,
    val passingMarks: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val questionNumber: Int,
    val questionText: String,
    val questionType: String, // MCQ, Short Question, Long Question, Numerical, Programming, Fill in the Blank, True/False
    val maximumMarks: Double,
    val expectedAnswer: String = "",
    val importantConcepts: String = "",
    val keywords: String = "",
    val requiredPoints: String = ""
)

@Entity(tableName = "marking_criteria")
data class MarkingCriterion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val criterion: String,
    val maximumMarks: Double
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long = 1,
    val name: String,
    val rollNumber: String,
    val className: String,
    val section: String
)

@Entity(tableName = "answer_sheets")
data class AnswerSheet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val examId: Long,
    val pageNumber: Int,
    val originalImagePath: String,
    val processedImagePath: String,
    val scannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_answers")
data class StudentAnswer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val answerSheetId: Long,
    val examId: Long,
    val studentId: Long,
    val questionId: Long,
    val extractedText: String,
    val ocrConfidence: Double, // 0.0 to 1.0
    val isQuestionIdentified: Boolean = true,
    val pageNumber: Int = 1
)

@Entity(tableName = "ai_evaluations")
data class AIEvaluation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentAnswerId: Long,
    val suggestedMarks: Double,
    val confidence: Double, // 0.0 to 1.0
    val explanation: String,
    val evaluationJson: String,
    val requiresTeacherReview: Boolean = false
)

@Entity(tableName = "final_marks")
data class FinalMark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentAnswerId: Long,
    val examId: Long,
    val studentId: Long,
    val questionId: Long,
    val aiSuggestedMarks: Double,
    val teacherFinalMarks: Double,
    val teacherComment: String = "",
    val reviewed: Boolean = false,
    val finalized: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
