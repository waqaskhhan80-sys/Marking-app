package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Teacher::class,
        Exam::class,
        Question::class,
        MarkingCriterion::class,
        Student::class,
        AnswerSheet::class,
        StudentAnswer::class,
        AIEvaluation::class,
        FinalMark::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun examDao(): ExamDao
    abstract fun questionDao(): QuestionDao
    abstract fun studentDao(): StudentDao
    abstract fun answerSheetDao(): AnswerSheetDao
    abstract fun evaluationDao(): EvaluationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_answersheet_checker.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
