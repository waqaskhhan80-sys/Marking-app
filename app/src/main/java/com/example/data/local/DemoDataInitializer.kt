package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DemoDataInitializer {

    suspend fun populateDemoData(context: Context, database: AppDatabase, force: Boolean = false) = withContext(Dispatchers.IO) {
        val teacherDao = database.teacherDao()
        val examDao = database.examDao()
        val questionDao = database.questionDao()
        val studentDao = database.studentDao()
        val answerSheetDao = database.answerSheetDao()
        val evaluationDao = database.evaluationDao()

        val existingTeacher = teacherDao.getTeacherById(1)
        if (existingTeacher != null && !force) {
            return@withContext
        }

        // 1. Teacher
        val teacher = Teacher(
            id = 1,
            name = "Prof. Waqas Khan",
            schoolName = "Model Science High School",
            email = "waqaskhhan@gmail.com",
            passwordHash = "demo123"
        )
        teacherDao.insertTeacher(teacher)

        // 2. Students
        val students = listOf(
            Student(id = 1, teacherId = 1, name = "Muhammad Abdul Daim", rollNumber = "01", className = "10th", section = "A"),
            Student(id = 2, teacherId = 1, name = "Ali Khan", rollNumber = "02", className = "10th", section = "A"),
            Student(id = 3, teacherId = 1, name = "Ahmed Raza", rollNumber = "03", className = "10th", section = "A"),
            Student(id = 4, teacherId = 1, name = "Hassan Tariq", rollNumber = "04", className = "10th", section = "A")
        )
        studentDao.insertStudents(students)

        // 3. Exam
        val exam = Exam(
            id = 1,
            teacherId = 1,
            examName = "Mid Term Examination",
            className = "10th",
            section = "A",
            subject = "Computer Science",
            date = "2026-09-20",
            session = "2026-27",
            totalMarks = 50.0,
            passingMarks = 25.0
        )
        examDao.insertExam(exam)

        // 4. Questions & Marking Schemes
        val q1 = Question(
            id = 1,
            examId = 1,
            questionNumber = 1,
            questionText = "What is the main function of an operating system?",
            questionType = "MCQ",
            maximumMarks = 1.0,
            expectedAnswer = "B) Manage system resources and act as intermediary between user and hardware",
            importantConcepts = "Resource management, hardware intermediary",
            keywords = "hardware, resources, intermediary, system software",
            requiredPoints = "Option B or resource management"
        )
        questionDao.insertQuestion(q1)
        questionDao.insertCriteria(listOf(
            MarkingCriterion(questionId = 1, criterion = "Correct option identification (B)", maximumMarks = 1.0)
        ))

        val q2 = Question(
            id = 2,
            examId = 1,
            questionNumber = 2,
            questionText = "Define RAM.",
            questionType = "Short Question",
            maximumMarks = 3.0,
            expectedAnswer = "Random Access Memory (RAM) is primary volatile memory that holds active program instructions and data currently being processed by the CPU.",
            importantConcepts = "Volatile memory, random access, holds active processes/data for CPU",
            keywords = "volatile, primary memory, temporary, read/write, CPU",
            requiredPoints = "1. Volatile/temporary nature (1m)\n2. Primary read-write memory (1m)\n3. Stores data/instructions for currently running programs (1m)"
        )
        questionDao.insertQuestion(q2)
        questionDao.insertCriteria(listOf(
            MarkingCriterion(questionId = 2, criterion = "Volatile / temporary memory nature", maximumMarks = 1.0),
            MarkingCriterion(questionId = 2, criterion = "Primary read-write access", maximumMarks = 1.0),
            MarkingCriterion(questionId = 2, criterion = "Stores active programs and CPU data", maximumMarks = 1.0)
        ))

        val q3 = Question(
            id = 3,
            examId = 1,
            questionNumber = 3,
            questionText = "Explain the functions of an operating system.",
            questionType = "Long Question",
            maximumMarks = 5.0,
            expectedAnswer = "An operating system performs key functions: 1. Processor/Process Management (scheduling CPU tasks). 2. Memory Management (allocating RAM space). 3. File System Management (organizing storage and directories). 4. Device Management (controlling I/O hardware via drivers). 5. Security & User Interface (protecting data and providing CLI/GUI).",
            importantConcepts = "Process management, memory management, file management, I/O device management, user interface/security",
            keywords = "scheduling, allocation, drivers, files, CLI, GUI, security, multitasking",
            requiredPoints = "Processor scheduling (1m), Memory allocation (1m), File handling (1m), Device/hardware control (1m), User interface & security (1m)"
        )
        questionDao.insertQuestion(q3)
        questionDao.insertCriteria(listOf(
            MarkingCriterion(questionId = 3, criterion = "Processor / CPU Scheduling management", maximumMarks = 1.0),
            MarkingCriterion(questionId = 3, criterion = "Memory (RAM) allocation and deallocation", maximumMarks = 1.0),
            MarkingCriterion(questionId = 3, criterion = "File system and storage management", maximumMarks = 1.0),
            MarkingCriterion(questionId = 3, criterion = "Device and I/O driver coordination", maximumMarks = 1.0),
            MarkingCriterion(questionId = 3, criterion = "Security and user interface (GUI/CLI)", maximumMarks = 1.0)
        ))

        val q4 = Question(
            id = 4,
            examId = 1,
            questionNumber = 4,
            questionText = "A processor executes a program with 2,000,000 instructions. The average Cycles Per Instruction (CPI) is 2.5 and clock cycle time is 2 nanoseconds. Calculate CPU Execution Time.",
            questionType = "Numerical",
            maximumMarks = 4.0,
            expectedAnswer = "Formula: CPU Time = Instruction Count × CPI × Clock Cycle Time.\nSubstitution: 2,000,000 × 2.5 × 2 × 10^-9 s = 5,000,000 × 2 × 10^-9 = 0.01 seconds (or 10 milliseconds).",
            importantConcepts = "CPU performance equation, formula identification, step-by-step substitution, correct SI units",
            keywords = "CPU Time, Instruction Count, CPI, nanoseconds, seconds, formula",
            requiredPoints = "Formula (1m), Substitution (1m), Calculation step (1m), Final answer with units (1m)"
        )
        questionDao.insertQuestion(q4)
        questionDao.insertCriteria(listOf(
            MarkingCriterion(questionId = 4, criterion = "Correct formula identification", maximumMarks = 1.0),
            MarkingCriterion(questionId = 4, criterion = "Correct value substitution", maximumMarks = 1.0),
            MarkingCriterion(questionId = 4, criterion = "Mathematical steps and calculation", maximumMarks = 1.0),
            MarkingCriterion(questionId = 4, criterion = "Accurate final value with proper units (seconds/ms)", maximumMarks = 1.0)
        ))

        val q5 = Question(
            id = 5,
            examId = 1,
            questionNumber = 5,
            questionText = "Write a C++ program to find the largest of two numbers.",
            questionType = "Programming",
            maximumMarks = 5.0,
            expectedAnswer = "#include <iostream>\nusing namespace std;\nint main() {\n  int a, b;\n  cout << \"Enter two numbers: \";\n  cin >> a >> b;\n  if (a > b) cout << \"Largest: \" << a;\n  else cout << \"Largest: \" << b;\n  return 0;\n}",
            importantConcepts = "Basic C++ structure, input handling, conditional if-else logic, correct output formatting. Note: Alternate valid logic (ternary operator) is accepted.",
            keywords = "iostream, cin, cout, if, else, main",
            requiredPoints = "Headers & main function (1m), Variable declaration & input (1m), Conditional logic (2m), Proper output (1m)"
        )
        questionDao.insertQuestion(q5)
        questionDao.insertCriteria(listOf(
            MarkingCriterion(questionId = 5, criterion = "Syntax, libraries (#include) and main declaration", maximumMarks = 1.0),
            MarkingCriterion(questionId = 5, criterion = "Variable declaration and user input (cin)", maximumMarks = 1.0),
            MarkingCriterion(questionId = 5, criterion = "Conditional logic (if-else or ternary) to determine max", maximumMarks = 2.0),
            MarkingCriterion(questionId = 5, criterion = "Correct display of result (cout) & return 0", maximumMarks = 1.0)
        ))

        // Create sample paper bitmap and save locally
        val answerSheetDir = File(context.filesDir, "answer_sheets").apply { mkdirs() }
        val sampleSheetFile = File(answerSheetDir, "student_01_page_1.png")
        if (!sampleSheetFile.exists()) {
            createSampleAnswerSheetBitmap(sampleSheetFile)
        }

        // 5. AnswerSheet record for Muhammad Abdul Daim
        val sheetId = answerSheetDao.insertAnswerSheet(
            AnswerSheet(
                id = 1,
                studentId = 1,
                examId = 1,
                pageNumber = 1,
                originalImagePath = sampleSheetFile.absolutePath,
                processedImagePath = sampleSheetFile.absolutePath
            )
        )

        // 6. Student answers extracted from handwriting
        val ans1 = StudentAnswer(
            id = 1,
            answerSheetId = sheetId,
            examId = 1,
            studentId = 1,
            questionId = 1,
            extractedText = "Ans 1: Option (B) - The main function of an operating system is to manage computer hardware and resources, and provide interface for user programs.",
            ocrConfidence = 0.96,
            isQuestionIdentified = true,
            pageNumber = 1
        )
        val ans2 = StudentAnswer(
            id = 2,
            answerSheetId = sheetId,
            examId = 1,
            studentId = 1,
            questionId = 2,
            extractedText = "Ans 2: RAM stands for Random Access Memory. It is volatile primary memory where the CPU stores data and program instructions while running tasks. When computer is turned off, its contents are cleared.",
            ocrConfidence = 0.94,
            isQuestionIdentified = true,
            pageNumber = 1
        )
        val ans3 = StudentAnswer(
            id = 3,
            answerSheetId = sheetId,
            examId = 1,
            studentId = 1,
            questionId = 3,
            extractedText = "Ans 3: OS functions:\n1. Process Management: CPU scheduling and managing active tasks.\n2. Memory Management: Keeping track of RAM addresses and memory allocation.\n3. File System: Organizing folders, read/write permissions.\n4. Device Management: Using device drivers to control printer, monitor, disk.\n5. Security: Authentication and protecting files from unauthorized access.",
            ocrConfidence = 0.92,
            isQuestionIdentified = true,
            pageNumber = 1
        )
        val ans4 = StudentAnswer(
            id = 4,
            answerSheetId = sheetId,
            examId = 1,
            studentId = 1,
            questionId = 4,
            extractedText = "Ans 4:\nGiven:\nInstruction Count = 2,000,000\nCPI = 2.5\nClock Cycle = 2 ns = 2 * 10^-9 sec\nFormula: CPU Execution Time = Instructions * CPI * Clock Cycle Time\n= 2,000,000 * 2.5 * 2 * 10^-9\n= 5,000,000 * 2 * 10^-9 = 0.01 seconds (10 ms)",
            ocrConfidence = 0.88,
            isQuestionIdentified = true,
            pageNumber = 1
        )
        val ans5 = StudentAnswer(
            id = 5,
            answerSheetId = sheetId,
            examId = 1,
            studentId = 1,
            questionId = 5,
            extractedText = "Ans 5:\n#include <iostream>\nusing namespace std;\n\nint main() {\n  int num1, num2;\n  cout << \"Enter two numbers: \";\n  cin >> num1 >> num2;\n  if (num1 > num2) {\n    cout << \"Maximum number is: \" << num1 << endl;\n  } else {\n    cout << \"Maximum number is: \" << num2 << endl;\n  }\n  return 0;\n}",
            ocrConfidence = 0.95,
            isQuestionIdentified = true,
            pageNumber = 1
        )
        evaluationDao.insertStudentAnswers(listOf(ans1, ans2, ans3, ans4, ans5))

        // 7. AI Evaluations with Criteria Breakdown
        val ev1 = AIEvaluation(
            id = 1,
            studentAnswerId = 1,
            suggestedMarks = 1.0,
            confidence = 0.98,
            explanation = "Correct option B selected with accurate conceptual explanation of resource management.",
            evaluationJson = """{"criterionScores":[{"criterion":"Correct option identification (B)","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Identified option B correctly"}]}""",
            requiresTeacherReview = false
        )
        val ev2 = AIEvaluation(
            id = 2,
            studentAnswerId = 2,
            suggestedMarks = 3.0,
            confidence = 0.95,
            explanation = "Excellent explanation. Student explicitly stated volatility, primary RAM nature, and holding data for CPU execution.",
            evaluationJson = """{"criterionScores":[{"criterion":"Volatile / temporary memory nature","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Correctly mentioned volatile and cleared on power-off"},{"criterion":"Primary read-write access","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Identified as primary memory"},{"criterion":"Stores active programs and CPU data","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Accurately explained CPU data storage"}]}""",
            requiresTeacherReview = false
        )
        val ev3 = AIEvaluation(
            id = 3,
            studentAnswerId = 3,
            suggestedMarks = 4.5,
            confidence = 0.89,
            explanation = "Student explained all 5 main OS functions well. Partial credit on point 5 as GUI/CLI was omitted though security was clearly articulated.",
            evaluationJson = """{"criterionScores":[{"criterion":"Processor / CPU Scheduling management","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Scheduling & active tasks noted"},{"criterion":"Memory (RAM) allocation and deallocation","maximumMarks":1.0,"awardedMarks":1.0,"reason":"RAM tracking present"},{"criterion":"File system and storage management","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Organizing folders & permissions"},{"criterion":"Device and I/O driver coordination","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Driver control explained"},{"criterion":"Security and user interface","maximumMarks":1.0,"awardedMarks":0.5,"reason":"Security mentioned, user interface/CLI not detailed"}]}""",
            requiresTeacherReview = false
        )
        val ev4 = AIEvaluation(
            id = 4,
            studentAnswerId = 4,
            suggestedMarks = 4.0,
            confidence = 0.93,
            explanation = "Full marks. Formula, conversion of nanoseconds to seconds, mathematical product, and final unit (0.01 s / 10 ms) are all exact.",
            evaluationJson = """{"criterionScores":[{"criterion":"Correct formula identification","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Formula explicitly written"},{"criterion":"Correct value substitution","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Substituted 2*10^-9 correctly"},{"criterion":"Mathematical steps and calculation","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Correct intermediate steps"},{"criterion":"Accurate final value with proper units","maximumMarks":1.0,"awardedMarks":1.0,"reason":"0.01 seconds given"}]}""",
            requiresTeacherReview = false
        )
        val ev5 = AIEvaluation(
            id = 5,
            studentAnswerId = 5,
            suggestedMarks = 5.0,
            confidence = 0.96,
            explanation = "Clean and fully functional C++ implementation following all requirements with proper cin/cout and condition.",
            evaluationJson = """{"criterionScores":[{"criterion":"Syntax, libraries (#include) and main declaration","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Standard C++ headers and main"},{"criterion":"Variable declaration and user input","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Proper input prompts"},{"criterion":"Conditional logic","maximumMarks":2.0,"awardedMarks":2.0,"reason":"Correct if-else condition"},{"criterion":"Correct display of result","maximumMarks":1.0,"awardedMarks":1.0,"reason":"Outputs largest number cleanly"}]}""",
            requiresTeacherReview = false
        )
        evaluationDao.insertAIEvaluations(listOf(ev1, ev2, ev3, ev4, ev5))

        // 8. Final Marks (Demonstrates Teacher Review & Override principle)
        val fm1 = FinalMark(
            id = 1,
            studentAnswerId = 1,
            examId = 1,
            studentId = 1,
            questionId = 1,
            aiSuggestedMarks = 1.0,
            teacherFinalMarks = 1.0,
            teacherComment = "Verified correct.",
            reviewed = true,
            finalized = true
        )
        val fm2 = FinalMark(
            id = 2,
            studentAnswerId = 2,
            examId = 1,
            studentId = 1,
            questionId = 2,
            aiSuggestedMarks = 3.0,
            teacherFinalMarks = 3.0,
            teacherComment = "Complete and accurate definition.",
            reviewed = true,
            finalized = true
        )
        val fm3 = FinalMark(
            id = 3,
            studentAnswerId = 3,
            examId = 1,
            studentId = 1,
            questionId = 3,
            aiSuggestedMarks = 4.5,
            teacherFinalMarks = 5.0, // Teacher gave full mark override!
            teacherComment = "Teacher override: Student clearly covered security which is a major OS component.",
            reviewed = true,
            finalized = true
        )
        val fm4 = FinalMark(
            id = 4,
            studentAnswerId = 4,
            examId = 1,
            studentId = 1,
            questionId = 4,
            aiSuggestedMarks = 4.0,
            teacherFinalMarks = 4.0,
            teacherComment = "Correct calculation and units.",
            reviewed = true,
            finalized = true
        )
        val fm5 = FinalMark(
            id = 5,
            studentAnswerId = 5,
            examId = 1,
            studentId = 1,
            questionId = 5,
            aiSuggestedMarks = 5.0,
            teacherFinalMarks = 5.0,
            teacherComment = "Great program.",
            reviewed = true,
            finalized = true
        )
        evaluationDao.insertFinalMarks(listOf(fm1, fm2, fm3, fm4, fm5))

        // Also add sample evaluated marks for the other 3 students so Class Results table is vibrant!
        addSampleStudentResults(evaluationDao, examId = 1, studentId = 2, total = 14.5, finalTotal = 15.0)
        addSampleStudentResults(evaluationDao, examId = 1, studentId = 3, total = 12.0, finalTotal = 12.5)
        addSampleStudentResults(evaluationDao, examId = 1, studentId = 4, total = 16.5, finalTotal = 17.0)
    }

    private suspend fun addSampleStudentResults(evaluationDao: EvaluationDao, examId: Long, studentId: Long, total: Double, finalTotal: Double) {
        val qMarks = listOf(1.0, 2.5, 4.0, 3.5, 4.0)
        val marksList = mutableListOf<FinalMark>()
        for (i in 1..5) {
            val sMark = qMarks.getOrElse(i - 1) { 3.0 }
            marksList.add(
                FinalMark(
                    studentAnswerId = (studentId * 10 + i),
                    examId = examId,
                    studentId = studentId,
                    questionId = i.toLong(),
                    aiSuggestedMarks = sMark,
                    teacherFinalMarks = sMark,
                    teacherComment = "Evaluated successfully.",
                    reviewed = true,
                    finalized = true
                )
            )
        }
        evaluationDao.insertFinalMarks(marksList)
    }

    private fun createSampleAnswerSheetBitmap(targetFile: File) {
        val width = 1200
        val height = 1700
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Light notebook paper background
        canvas.drawColor(Color.rgb(253, 252, 248))

        val linePaint = Paint().apply {
            color = Color.rgb(215, 230, 245)
            strokeWidth = 2f
        }
        val marginPaint = Paint().apply {
            color = Color.rgb(240, 180, 180)
            strokeWidth = 3f
        }
        val textPaint = Paint().apply {
            color = Color.rgb(24, 43, 73)
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 30, 50)
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val inkPaint = Paint().apply {
            color = Color.rgb(18, 52, 120) // Deep blue fountain pen ink
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        // Horizontal ruled lines
        for (y in 240..height step 48) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
        }
        // Left margin line
        canvas.drawLine(150f, 0f, 150f, height.toFloat(), marginPaint)

        // Header info
        canvas.drawText("EXAMINATION ANSWER SHEET - MID TERM 2026", 180f, 80f, headerPaint)
        canvas.drawText("Class: 10th-A  |  Subject: Computer Science", 180f, 130f, textPaint)
        canvas.drawText("Roll No: 01   |  Student: Muhammad Abdul Daim", 180f, 175f, textPaint)

        // Handwritten answers simulated
        var currentY = 280f
        val lines = listOf(
            "Q1. Ans: Option (B)",
            "The primary role of OS is managing hardware resources and providing",
            "an interface for application programs.",
            "",
            "Q2. RAM (Random Access Memory):",
            "RAM is high-speed primary memory of the computer. It is volatile in nature,",
            "meaning data is erased once the system shuts down. CPU reads instructions",
            "from RAM directly during processing.",
            "",
            "Q3. Functions of Operating System:",
            "1. Process Management: CPU scheduling of tasks and threads.",
            "2. Memory Management: Allocation of RAM space dynamically.",
            "3. File Management: Organizing directory trees, storage, file tables.",
            "4. I/O Device Control: Driving hardware like disks, network, display.",
            "5. System Security: Password authentication & protection against malware.",
            "",
            "Q4. Numerical Problem:",
            "Given: IC = 2,000,000 ; CPI = 2.5 ; Clock Cycle = 2 ns = 2 × 10⁻⁹ s",
            "Formula: CPU Execution Time = IC × CPI × Clock Cycle",
            "CPU Time = 2,000,000 × 2.5 × (2 × 10⁻⁹) = 5,000,000 × 2 × 10⁻⁹ = 0.01 sec (10 ms)",
            "",
            "Q5. Programming Solution:",
            "#include <iostream>",
            "using namespace std;",
            "int main() {",
            "    int num1, num2;",
            "    cout << \"Enter two numbers: \";",
            "    cin >> num1 >> num2;",
            "    if (num1 > num2) {",
            "        cout << \"Maximum is: \" << num1 << endl;",
            "    } else {",
            "        cout << \"Maximum is: \" << num2 << endl;",
            "    }",
            "    return 0;",
            "}"
        )

        for (line in lines) {
            if (line.startsWith("Q")) {
                canvas.drawText(line, 170f, currentY, headerPaint)
            } else {
                canvas.drawText(line, 170f, currentY, inkPaint)
            }
            currentY += 48f
        }

        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
    }
}
