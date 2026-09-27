package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Long): Teacher?

    @Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
    suspend fun getTeacherByEmail(email: String): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): Exam?

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    fun getExamByIdFlow(id: Long): Flow<Exam?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY questionNumber ASC")
    fun getQuestionsForExam(examId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY questionNumber ASC")
    suspend fun getQuestionsForExamSync(examId: Long): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    // Marking criteria
    @Query("SELECT * FROM marking_criteria WHERE questionId = :questionId")
    fun getCriteriaForQuestion(questionId: Long): Flow<List<MarkingCriterion>>

    @Query("SELECT * FROM marking_criteria WHERE questionId = :questionId")
    suspend fun getCriteriaForQuestionSync(questionId: Long): List<MarkingCriterion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriteria(criteria: List<MarkingCriterion>)

    @Query("DELETE FROM marking_criteria WHERE questionId = :questionId")
    suspend fun deleteCriteriaForQuestion(questionId: Long)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY className, section, rollNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE className = :className AND section = :section ORDER BY rollNumber ASC")
    fun getStudentsByClass(className: String, section: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber AND className = :className LIMIT 1")
    suspend fun getStudentByRollAndClass(rollNumber: String, className: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface AnswerSheetDao {
    @Query("SELECT * FROM answer_sheets WHERE examId = :examId AND studentId = :studentId ORDER BY pageNumber ASC")
    fun getAnswerSheets(examId: Long, studentId: Long): Flow<List<AnswerSheet>>

    @Query("SELECT * FROM answer_sheets WHERE examId = :examId AND studentId = :studentId ORDER BY pageNumber ASC")
    suspend fun getAnswerSheetsSync(examId: Long, studentId: Long): List<AnswerSheet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerSheet(answerSheet: AnswerSheet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerSheets(answerSheets: List<AnswerSheet>): List<Long>

    @Query("DELETE FROM answer_sheets WHERE examId = :examId AND studentId = :studentId")
    suspend fun deleteAnswerSheetsForStudent(examId: Long, studentId: Long)
}

@Dao
interface EvaluationDao {
    // Student answers
    @Query("SELECT * FROM student_answers WHERE examId = :examId AND studentId = :studentId")
    fun getStudentAnswers(examId: Long, studentId: Long): Flow<List<StudentAnswer>>

    @Query("SELECT * FROM student_answers WHERE examId = :examId AND studentId = :studentId")
    suspend fun getStudentAnswersSync(examId: Long, studentId: Long): List<StudentAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAnswer(answer: StudentAnswer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAnswers(answers: List<StudentAnswer>): List<Long>

    @Update
    suspend fun updateStudentAnswer(answer: StudentAnswer)

    // AI Evaluations
    @Query("SELECT * FROM ai_evaluations WHERE studentAnswerId = :studentAnswerId LIMIT 1")
    suspend fun getEvaluationForAnswer(studentAnswerId: Long): AIEvaluation?

    @Query("SELECT * FROM ai_evaluations")
    fun getAllEvaluations(): Flow<List<AIEvaluation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIEvaluation(evaluation: AIEvaluation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIEvaluations(evaluations: List<AIEvaluation>)

    // Final Marks
    @Query("SELECT * FROM final_marks WHERE examId = :examId AND studentId = :studentId")
    fun getFinalMarks(examId: Long, studentId: Long): Flow<List<FinalMark>>

    @Query("SELECT * FROM final_marks WHERE examId = :examId AND studentId = :studentId")
    suspend fun getFinalMarksSync(examId: Long, studentId: Long): List<FinalMark>

    @Query("SELECT * FROM final_marks WHERE examId = :examId")
    fun getAllFinalMarksForExam(examId: Long): Flow<List<FinalMark>>

    @Query("SELECT * FROM final_marks WHERE examId = :examId")
    suspend fun getAllFinalMarksForExamSync(examId: Long): List<FinalMark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinalMark(finalMark: FinalMark): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinalMarks(finalMarks: List<FinalMark>)

    @Update
    suspend fun updateFinalMark(finalMark: FinalMark)

    @Query("SELECT COUNT(DISTINCT studentId) FROM final_marks WHERE examId = :examId AND finalized = 1")
    fun getFinalizedCount(examId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM final_marks WHERE reviewed = 0")
    fun getPendingReviewsCount(): Flow<Int>
}
