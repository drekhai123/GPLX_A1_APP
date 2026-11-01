package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GplxRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    object Home : AppScreen()
    data class Study(
        val filterMode: StudyFilterMode = StudyFilterMode.ALL,
        val categoryName: String = "",
        val initialQuestionIndex: Int = 0
    ) : AppScreen()
    object ExamList : AppScreen()
    data class ExamTaking(val exam: Exam) : AppScreen()
    data class ExamResultReview(val exam: Exam, val attempt: ExamAttempt) : AppScreen()
    object TrafficSigns : AppScreen()
    object MemoryTips : AppScreen()
    object ExamHistory : AppScreen()
    object Stats : AppScreen()
}

enum class BottomNavTab {
    HOME,
    STUDY,
    EXAMS,
    STATS
}

enum class StudyFilterMode {
    ALL,                // 250 câu hỏi
    CRITICAL_ONLY,      // 20 câu điểm liệt
    CATEGORY,           // Theo chủ đề
    WRONG_ONLY,         // Câu làm sai
    BOOKMARKED_ONLY,    // Câu đã lưu / đánh dấu
    TRICKY_ONLY         // Câu người mới hay sai
}

data class ActiveExamState(
    val exam: Exam? = null,
    val remainingSeconds: Int = 19 * 60,
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionNumber -> optionId
    val flaggedQuestionNumbers: Set<Int> = emptySet(),
    val isTimerRunning: Boolean = false,
    val isFinished: Boolean = false,
    val latestAttempt: ExamAttempt? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = GplxRepository(application.applicationContext)

    // Navigation BackStack
    private val _screenStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Home))
    val currentScreen: StateFlow<AppScreen> = MutableStateFlow(AppScreen.Home)

    private val _currentScreenState = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreenState: StateFlow<AppScreen> = _currentScreenState.asStateFlow()

    private val _activeTab = MutableStateFlow(BottomNavTab.HOME)
    val activeTab: StateFlow<BottomNavTab> = _activeTab.asStateFlow()

    // Active Exam
    private val _activeExamState = MutableStateFlow(ActiveExamState())
    val activeExamState: StateFlow<ActiveExamState> = _activeExamState.asStateFlow()

    private var timerJob: Job? = null

    val userProgress = repository.userProgressFlow
    val examAttempts = repository.examAttemptsFlow

    fun navigateTo(screen: AppScreen, clearStack: Boolean = false) {
        val stack = if (clearStack) mutableListOf(screen) else _screenStack.value.toMutableList().apply { add(screen) }
        _screenStack.value = stack
        _currentScreenState.value = screen

        // update active bottom nav tab if top level
        when (screen) {
            is AppScreen.Home -> _activeTab.value = BottomNavTab.HOME
            is AppScreen.Study -> if (screen.filterMode == StudyFilterMode.ALL) _activeTab.value = BottomNavTab.STUDY
            is AppScreen.ExamList -> _activeTab.value = BottomNavTab.EXAMS
            is AppScreen.Stats -> _activeTab.value = BottomNavTab.STATS
            else -> {}
        }
    }

    fun navigateBack(): Boolean {
        val stack = _screenStack.value.toMutableList()
        if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
            _screenStack.value = stack
            val prev = stack.last()
            _currentScreenState.value = prev

            // synchronize tab
            when (prev) {
                is AppScreen.Home -> _activeTab.value = BottomNavTab.HOME
                is AppScreen.Study -> if (prev.filterMode == StudyFilterMode.ALL) _activeTab.value = BottomNavTab.STUDY
                is AppScreen.ExamList -> _activeTab.value = BottomNavTab.EXAMS
                is AppScreen.Stats -> _activeTab.value = BottomNavTab.STATS
                else -> {}
            }
            return true
        }
        return false
    }

    fun selectBottomTab(tab: BottomNavTab) {
        _activeTab.value = tab
        when (tab) {
            BottomNavTab.HOME -> navigateTo(AppScreen.Home, clearStack = true)
            BottomNavTab.STUDY -> navigateTo(AppScreen.Study(StudyFilterMode.ALL), clearStack = true)
            BottomNavTab.EXAMS -> navigateTo(AppScreen.ExamList, clearStack = true)
            BottomNavTab.STATS -> navigateTo(AppScreen.Stats, clearStack = true)
        }
    }

    // Exam Management
    fun startExam(exam: Exam) {
        timerJob?.cancel()
        val totalSecs = exam.durationMinutes * 60
        _activeExamState.value = ActiveExamState(
            exam = exam,
            remainingSeconds = totalSecs,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            flaggedQuestionNumbers = emptySet(),
            isTimerRunning = true,
            isFinished = false,
            latestAttempt = null
        )
        navigateTo(AppScreen.ExamTaking(exam))
        startExamTimer()
    }

    private fun startExamTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeExamState.value
                if (!current.isTimerRunning || current.isFinished) break

                if (current.remainingSeconds <= 1) {
                    _activeExamState.value = current.copy(remainingSeconds = 0)
                    submitExam(autoSubmitted = true)
                    break
                } else {
                    _activeExamState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                }
            }
        }
    }

    fun selectExamAnswer(questionNumber: Int, optionId: Int) {
        val current = _activeExamState.value
        val newAnswers = current.userAnswers.toMutableMap()
        newAnswers[questionNumber] = optionId
        _activeExamState.value = current.copy(userAnswers = newAnswers)
    }

    fun toggleFlagQuestion(questionNumber: Int) {
        val current = _activeExamState.value
        val flags = current.flaggedQuestionNumbers.toMutableSet()
        if (flags.contains(questionNumber)) {
            flags.remove(questionNumber)
        } else {
            flags.add(questionNumber)
        }
        _activeExamState.value = current.copy(flaggedQuestionNumbers = flags)
    }

    fun setExamQuestionIndex(index: Int) {
        val current = _activeExamState.value
        val total = current.exam?.questions?.size ?: 25
        if (index in 0 until total) {
            _activeExamState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun submitExam(autoSubmitted: Boolean = false) {
        timerJob?.cancel()
        val current = _activeExamState.value
        val exam = current.exam ?: return

        var score = 0
        var failedCritical = false

        for (q in exam.questions) {
            val userAns = current.userAnswers[q.questionNumber]
            val isCorrect = (userAns != null && userAns == q.correctAnswer)

            if (isCorrect) {
                score++
            } else if (q.isCritical) {
                // If user didn't answer or answered incorrectly on a critical question
                failedCritical = true
            }
        }

        // Must score >= 21 AND not fail any critical question
        val isPassed = (score >= exam.passingScore) && !failedCritical
        val elapsedSeconds = (exam.durationMinutes * 60) - current.remainingSeconds

        val attempt = ExamAttempt(
            id = UUID.randomUUID().toString(),
            examId = exam.examId,
            examTitle = exam.title,
            score = score,
            totalQuestions = exam.totalQuestions,
            isPassed = isPassed,
            failedCriticalQuestion = failedCritical,
            durationSeconds = elapsedSeconds.toLong().coerceAtLeast(0),
            timestamp = System.currentTimeMillis(),
            userAnswers = current.userAnswers
        )

        repository.saveExamAttempt(attempt)

        _activeExamState.value = current.copy(
            isFinished = true,
            isTimerRunning = false,
            latestAttempt = attempt
        )

        navigateTo(AppScreen.ExamResultReview(exam, attempt))
    }

    fun submitStudyAnswer(question: Question, selectedOptionId: Int) {
        val isCorrect = (selectedOptionId == question.correctAnswer)
        repository.submitAnswer(question.id, selectedOptionId, isCorrect)
    }

    fun toggleBookmark(questionId: String) {
        repository.toggleBookmark(questionId)
    }

    fun clearHistory() {
        repository.clearHistory()
    }

    fun resetProgress() {
        repository.resetAllProgress()
    }
}
