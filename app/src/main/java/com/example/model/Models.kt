package com.example.model

data class Question(
    val id: String,
    val licenseClass: String,
    val category: String,
    val isCritical: Boolean,
    val question: String,
    val options: List<QuestionOption>,
    val correctAnswer: Int,
    val explanation: String,
    val images: List<QuestionImage> = emptyList()
)

data class QuestionOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

data class QuestionImage(
    val src: String,
    val alt: String
)

data class Exam(
    val examId: Int,
    val title: String,
    val examType: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val passingScore: Int,
    val requireNoCriticalError: Boolean,
    val questions: List<ExamQuestion>
)

data class ExamQuestion(
    val questionNumber: Int,
    val question: String,
    val isCritical: Boolean,
    val correctAnswer: Int,
    val options: List<QuestionOption>,
    val images: List<QuestionImage> = emptyList(),
    val explanation: String = ""
)

data class TrafficSign(
    val id: String,
    val order: Int,
    val name: String,
    val description: String,
    val categoryCode: String,
    val categoryName: String,
    val imageUrl: String,
    val relatedQuestionId: String = ""
)

data class MemoryTip(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "Cấu trúc đề", "Từ khóa vàng", "Quy tắc", "Biển báo", "Sa hình"
    val badge: String,
    val points: List<String>,
    val highlightRule: String = ""
)

data class ExamAttempt(
    val id: String,
    val examId: Int,
    val examTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val isPassed: Boolean,
    val failedCriticalQuestion: Boolean,
    val durationSeconds: Long,
    val timestamp: Long,
    val userAnswers: Map<Int, Int> // questionNumber -> selectedOptionId
)

data class UserProgress(
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val wrongQuestionIds: Set<String> = emptySet(),
    val bookmarkedQuestionIds: Set<String> = emptySet(),
    val answeredMap: Map<String, Int> = emptyMap(), // questionId -> selectedOptionId
    val lastStudyQuestionIndex: Int = 0
)

data class CategoryStat(
    val categoryName: String,
    val total: Int,
    val answered: Int,
    val correct: Int
) {
    val progressPercent: Int
        get() = if (total > 0) (answered * 100 / total) else 0

    val accuracyPercent: Int
        get() = if (answered > 0) (correct * 100 / answered) else 0
}
