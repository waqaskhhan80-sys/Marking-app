package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AIEvaluationEngine
import com.example.data.model.MarkingCriterion
import com.example.data.model.Question
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Answer Sheet Checker", appName)
    }

    @Test
    fun `test fallback rubric concept evaluation`() = runBlocking {
        val engine = AIEvaluationEngine()
        val question = Question(
            id = 2,
            examId = 1,
            questionNumber = 2,
            questionText = "Define RAM.",
            questionType = "Short Question",
            maximumMarks = 3.0,
            expectedAnswer = "RAM is volatile primary memory that temporarily stores data and instructions for active CPU processes.",
            importantConcepts = "Volatile, primary, CPU data storage"
        )
        val criteria = listOf(
            MarkingCriterion(questionId = 2, criterion = "Volatile memory nature", maximumMarks = 1.0),
            MarkingCriterion(questionId = 2, criterion = "Primary read-write access", maximumMarks = 1.0),
            MarkingCriterion(questionId = 2, criterion = "Stores active program data for CPU", maximumMarks = 1.0)
        )
        val studentAnswer = "RAM stands for Random Access Memory. It is volatile memory where the CPU stores data and program instructions while running tasks."

        val result = engine.evaluateAnswer(question, criteria, studentAnswer)

        assertTrue(result.suggestedMarks > 0.0)
        assertEquals(3, result.criteria.size)
        assertTrue(result.confidence >= 0.70)
    }
}
