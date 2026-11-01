package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GplxRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gplx_a1_prefs", Context.MODE_PRIVATE)

    private val _questions = mutableListOf<Question>()
    val questions: List<Question> get() = _questions

    private val _exams = mutableListOf<Exam>()
    val exams: List<Exam> get() = _exams

    private val _signs = mutableListOf<TrafficSign>()
    val signs: List<TrafficSign> get() = _signs

    private val _memoryTips = mutableListOf<MemoryTip>()
    val memoryTips: List<MemoryTip> get() = _memoryTips

    private val _userProgressFlow = MutableStateFlow(UserProgress())
    val userProgressFlow: StateFlow<UserProgress> = _userProgressFlow.asStateFlow()

    private val _examAttemptsFlow = MutableStateFlow<List<ExamAttempt>>(emptyList())
    val examAttemptsFlow: StateFlow<List<ExamAttempt>> = _examAttemptsFlow.asStateFlow()

    init {
        loadDataFromAssets()
        loadMemoryTips()
        loadUserProgress()
        loadExamAttempts()
    }

    private fun loadDataFromAssets() {
        try {
            // 1. Load Questions
            val questionsJson = context.assets.open("questions_a1_optimized_e874b2cc.json")
                .bufferedReader().use { it.readText() }
            val qRoot = JSONObject(questionsJson)
            val qArray = qRoot.getJSONArray("questions")
            _questions.clear()

            for (i in 0 until qArray.length()) {
                val obj = qArray.getJSONObject(i)
                val id = obj.optString("id", "A1-Q${i + 1}")
                val licenseClass = obj.optString("license_class", "A1")
                val category = obj.optString("category", "Khái niệm và quy tắc")
                val isCritical = obj.optBoolean("is_critical_question", false)
                val questionText = obj.optString("question", "")
                val correctAnswer = obj.optInt("correct_answer", 1)
                val explanation = obj.optString("explanation", "")

                val optionsList = mutableListOf<QuestionOption>()
                val optArray = obj.optJSONArray("options") ?: JSONArray()
                for (j in 0 until optArray.length()) {
                    val optObj = optArray.getJSONObject(j)
                    optionsList.add(
                        QuestionOption(
                            id = optObj.optInt("id", j + 1),
                            text = optObj.optString("text", ""),
                            isCorrect = optObj.optBoolean("is_correct", false)
                        )
                    )
                }

                val imagesList = mutableListOf<QuestionImage>()
                val imgArray = obj.optJSONArray("images") ?: JSONArray()
                for (k in 0 until imgArray.length()) {
                    val imgObj = imgArray.getJSONObject(k)
                    val src = imgObj.optString("src", "")
                    val alt = imgObj.optString("alt", "")
                    if (src.isNotEmpty()) {
                        imagesList.add(QuestionImage(src, alt))
                    }
                }

                _questions.add(
                    Question(
                        id = id,
                        licenseClass = licenseClass,
                        category = category,
                        isCritical = isCritical,
                        question = questionText,
                        options = optionsList,
                        correctAnswer = correctAnswer,
                        explanation = explanation,
                        images = imagesList
                    )
                )
            }

            // 2. Load 10 Exams
            val examsJson = context.assets.open("10-bo-de-thi-co-dinh_fb189600.json")
                .bufferedReader().use { it.readText() }
            val eRoot = JSONObject(examsJson)
            val eArray = eRoot.getJSONArray("exams")
            _exams.clear()

            for (i in 0 until eArray.length()) {
                val eObj = eArray.getJSONObject(i)
                val examId = eObj.optInt("exam_id", i + 1)
                val title = eObj.optString("title", "Đề số ${i + 1}")
                val examType = eObj.optString("exam_type", "fixed_exam")
                val totalQuestions = eObj.optInt("total_questions", 25)
                val durationMinutes = eObj.optInt("duration_minutes", 19)
                val passingScore = eObj.optInt("passing_score", 21)
                val requireNoCritical = eObj.optBoolean("require_no_critical_error", true)

                val examQuestions = mutableListOf<ExamQuestion>()
                val eqArray = eObj.optJSONArray("questions") ?: JSONArray()
                for (j in 0 until eqArray.length()) {
                    val qObj = eqArray.getJSONObject(j)
                    val qNum = qObj.optInt("question_number", j + 1)
                    val qText = qObj.optString("question", "").removePrefix("*").trim()
                    val isCrit = qObj.optBoolean("is_critical", false)
                    val correctStr = qObj.optString("correct_answer", "1")
                    val correctInt = correctStr.toIntOrNull() ?: 1

                    val answersList = mutableListOf<QuestionOption>()
                    val ansArray = qObj.optJSONArray("answers") ?: JSONArray()
                    for (k in 0 until ansArray.length()) {
                        val aObj = ansArray.getJSONObject(k)
                        val aId = aObj.optString("id", "${k + 1}").toIntOrNull() ?: (k + 1)
                        val aText = aObj.optString("text", "")
                        val aIsCorrect = aObj.optBoolean("is_correct", aId == correctInt)
                        answersList.add(QuestionOption(aId, aText, aIsCorrect))
                    }

                    val imagesList = mutableListOf<QuestionImage>()
                    val imgArray = qObj.optJSONArray("images") ?: JSONArray()
                    for (k in 0 until imgArray.length()) {
                        val imgObj = imgArray.getJSONObject(k)
                        val src = imgObj.optString("src", "")
                        val alt = imgObj.optString("alt", "")
                        if (src.isNotEmpty()) {
                            imagesList.add(QuestionImage(src, alt))
                        }
                    }

                    // find explanation from questions bank if available
                    val matchingQuestion = _questions.find { it.question.contains(qText.take(20)) }
                    val explanation = matchingQuestion?.explanation ?: ""

                    examQuestions.add(
                        ExamQuestion(
                            questionNumber = qNum,
                            question = qText,
                            isCritical = isCrit,
                            correctAnswer = correctInt,
                            options = answersList,
                            images = imagesList,
                            explanation = explanation
                        )
                    )
                }

                _exams.add(
                    Exam(
                        examId = examId,
                        title = title,
                        examType = examType,
                        totalQuestions = totalQuestions,
                        durationMinutes = durationMinutes,
                        passingScore = passingScore,
                        requireNoCriticalError = requireNoCritical,
                        questions = examQuestions
                    )
                )
            }

            // 3. Load Traffic Signs
            val signsJson = context.assets.open("traffic_signs_f1a31fe2.json")
                .bufferedReader().use { it.readText() }
            val sRoot = JSONObject(signsJson)
            val sArray = sRoot.getJSONArray("signs")
            _signs.clear()

            for (i in 0 until sArray.length()) {
                val sObj = sArray.getJSONObject(i)
                val id = sObj.optString("id", "sign-$i")
                val order = sObj.optInt("order", i + 1)
                val name = sObj.optString("name", "")
                val description = sObj.optString("description", "")

                val catObj = sObj.optJSONObject("category")
                val catCode = catObj?.optString("code", "other") ?: "other"
                val catName = catObj?.optString("name", "BIỂN BÁO KHÁC") ?: "BIỂN BÁO KHÁC"

                val imgObj = sObj.optJSONObject("image")
                val imgUrl = imgObj?.optString("url", "") ?: ""

                val relObj = sObj.optJSONObject("related_question")
                val relQId = relObj?.optString("question_id", "") ?: ""

                _signs.add(
                    TrafficSign(
                        id = id,
                        order = order,
                        name = name,
                        description = description,
                        categoryCode = catCode,
                        categoryName = catName,
                        imageUrl = imgUrl,
                        relatedQuestionId = relQId
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMemoryTips() {
        _memoryTips.clear()
        _memoryTips.addAll(
            listOf(
                MemoryTip(
                    id = "tip-structure",
                    title = "1. CẤU TRÚC BỘ ĐỀ THI A1 MỚI NHẤT BẠN CẦN NẮM VỮNG",
                    subtitle = "Hiểu rõ 'luật chơi' trước khi bắt đầu học mẹo",
                    category = "Cấu trúc đề",
                    badge = "Cấu trúc chuẩn",
                    points = listOf(
                        "Tổng số câu hỏi trong ngân hàng: 250 câu (chia làm 5 phần chính: Luật GTĐB, Hệ thống Biển báo, Kỹ năng Sa hình, Kỹ thuật và Văn hóa GT).",
                        "Số câu hỏi trong 1 đề thi sát hạch: 25 câu hỏi trắc nghiệm ngẫu nhiên.",
                        "Thời gian làm bài thi: 19 phút.",
                        "Điều kiện ĐẠT: Đúng từ 21/25 câu trở lên VÀ KHÔNG ĐƯỢC SAI BẤT KỲ CÂU ĐIỂM LIỆT NÀO.",
                        "LƯU Ý CỰC KỲ QUAN TRỌNG: Kể cả khi bạn làm đúng 24/25 câu, nhưng câu sai duy nhất là câu điểm liệt thì bài thi bị CHẤM TRƯỢT NGAY LẬP TỨC!"
                    ),
                    highlightRule = "Đúng 21/25 câu + Tuyệt đối không sai câu điểm liệt = ĐỖ BẰNG A1!"
                ),
                MemoryTip(
                    id = "tip-keywords-gold",
                    title = "2. CỤM TỪ 'VÀNG' — THẤY LÀ CHỌN NGAY",
                    subtitle = "Mẹo trả lời nhanh các câu hỏi về Quy tắc & Khái niệm",
                    category = "Từ khóa vàng",
                    badge = "Từ khóa vàng",
                    points = listOf(
                        "\"Bị nghiêm cấm\" hoặc \"Không được...\": Nếu trong các phương án có chứa từ này, hãy chọn ngay mà không cần đọc hết câu hỏi (ví dụ: Không được phép, không được quay đầu, không được mang vác, tuyệt đối cấm).",
                        "\"UBND cấp tỉnh\": Thẩm quyền quy định các tuyến đường cấm, đường đi một chiều thuộc UBND cấp tỉnh.",
                        "\"Cơ quan có thẩm quyền cấp phép\": Xe quá khổ, quá tải muốn lưu thông phải được cơ quan có thẩm quyền cấp phép.",
                        "\"Chấp hành hiệu lệnh của người điều khiển giao thông\": Luôn ưu tiên hiệu lệnh của Cảnh sát giao thông trên biển báo và đèn tín hiệu.",
                        "\"Bên phải\": Luôn đi về bên phải theo chiều đi của mình, đi đúng làn đường quy định."
                    ),
                    highlightRule = "Thấy \"BỊ NGHIÊM CẤM\" hoặc \"KHÔNG ĐƯỢC PHÉP\" -> CHỌN NGAY ĐÁP ÁN ĐÓ!"
                ),
                MemoryTip(
                    id = "tip-speed-distance",
                    title = "3. MẸO VỀ TỐC ĐỘ, ĐỘ TUỔI VÀ KHOẢNG CÁCH",
                    subtitle = "Các con số vàng cần nhớ chính xác",
                    category = "Quy tắc",
                    badge = "Số liệu chuẩn",
                    points = listOf(
                        "Độ tuổi: Đủ 16 tuổi -> xe dưới 50cm3. Đủ 18 tuổi -> xe từ 50cm3 trở lên (Hạng A1, A2, B1, B2).",
                        "Tốc độ trong khu vực đông dân cư: Đường đôi hoặc đường 1 chiều có từ 2 làn xe cơ giới trở lên: Tối đa 60 km/h. Đường 2 chiều hoặc đường 1 chiều có 1 làn xe cơ giới: Tối đa 50 km/h.",
                        "Tốc độ ngoài khu vực đông dân cư (với mô tô): Tối đa 70 km/h (đường đôi) và 60 km/h (đường 2 chiều).",
                        "Xe máy kéo nhỏ, xe máy chuyên dùng: Tốc độ tối đa không quá 40 km/h.",
                        "Khoảng cách an toàn tối thiểu tại nơi đường sắt giao cắt: Cách ray ngoài cùng tối thiểu 5m khi có chuông báo/hàng rào đóng."
                    ),
                    highlightRule = "Tuổi lái xe A1: 18 tuổi. Xe chuyên dùng: max 40km/h. Cách đường sắt: 5m."
                ),
                MemoryTip(
                    id = "tip-signs",
                    title = "4. NGUYÊN TẮC 'BẤT BIẾN' VỀ BIỂN BÁO GIAO THÔNG",
                    subtitle = "Cách nhận diện nhanh 5 nhóm biển báo đường bộ",
                    category = "Biển báo",
                    badge = "Hệ thống biển",
                    points = listOf(
                        "Biển CẤM: Hình tròn, viền đỏ, nền trắng, hình vẽ màu đen (hoặc nền xanh vẽ vạch đỏ) -> Báo điều cấm.",
                        "Biển NGUY HIỂM: Hình tam giác đều, viền đỏ, nền vàng, hình vẽ màu đen -> Cảnh báo nguy hiểm phía trước để giảm tốc độ.",
                        "Biển HIỆU LỆNH: Hình tròn, nền xanh lam, hình vẽ màu trắng -> Bắt buộc người đi đường phải thi hành.",
                        "Biển CHỈ DẪN: Hình chữ nhật hoặc hình vuông, nền xanh lam -> Hướng dẫn hướng đi hoặc điều có ích.",
                        "Quy tắc cấm xe: Cấm nhỏ thì cấm lớn, cấm lớn không cấm nhỏ (Cấm mô tô 2 bánh -> Cấm mô tô 3 bánh; Cấm ô tô con -> Cấm tải, khách, kéo)."
                    ),
                    highlightRule = "Tròn đỏ = CẤM · Tam giác vàng = NGUY HIỂM · Tròn xanh = HIỆU LỆNH · Vuông/Chữ nhật xanh = CHỈ DẪN"
                ),
                MemoryTip(
                    id = "tip-diagrams",
                    title = "5. THẦN CHÚ GIẢI SA HÌNH CỰC NHANH TRONG 5 GIÂY",
                    subtitle = "Áp dụng theo thứ tự ưu tiên từ 1 đến 5 để không bao giờ làm sai",
                    category = "Sa hình",
                    badge = "Thần chú sa hình",
                    points = listOf(
                        "Bước 1 (Nhất chớm): Xe nào đã vào giao lộ (vượt qua vạch người đi bộ) trước thì được đi trước.",
                        "Bước 2 (Nhì ưu): Xe ưu tiên đi trước theo thứ tự: HỎA (Cứu hỏa) -> SỰ (Quân sự) -> CÔNG (Công an) -> THƯƠNG (Cứu thương).",
                        "Bước 3 (Tam đường): Xe đi trên đường ưu tiên (thấy biển hình thoi màu vàng hoặc tam giác viền đỏ hướng đỉnh lên) được đi trước.",
                        "Bước 4 (Tứ phải): Xe nào có bên PHẢI KHÔNG VƯỚNG (bên phải trống không có xe) được quyền đi trước (áp dụng tại ngã 4 đồng cấp).",
                        "Bước 5 (Ngũ hướng): Hướng rẽ ưu tiên: RẼ PHẢI -> ĐI THẲNG -> RẼ TRÁI."
                    ),
                    highlightRule = "\"NHẤT CHỚM - NHÌ ƯU - TAM ĐƯỜNG - TỨ PHẢI - NGŨ HƯỚNG\""
                )
            )
        )
    }

    private fun loadUserProgress() {
        val totalAnswered = prefs.getInt("total_answered", 0)
        val correctCount = prefs.getInt("correct_count", 0)
        val wrongSet = prefs.getStringSet("wrong_questions", emptySet())?.toSet() ?: emptySet()
        val bookmarkSet = prefs.getStringSet("bookmarked_questions", emptySet())?.toSet() ?: emptySet()
        val lastIndex = prefs.getInt("last_study_index", 0)

        val answeredMap = mutableMapOf<String, Int>()
        val ansJson = prefs.getString("answered_map_json", "{}") ?: "{}"
        try {
            val obj = JSONObject(ansJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                answeredMap[k] = obj.getInt(k)
            }
        } catch (_: Exception) {}

        _userProgressFlow.value = UserProgress(
            totalAnswered = totalAnswered,
            correctCount = correctCount,
            wrongQuestionIds = wrongSet,
            bookmarkedQuestionIds = bookmarkSet,
            answeredMap = answeredMap,
            lastStudyQuestionIndex = lastIndex
        )
    }

    private fun loadExamAttempts() {
        val attemptsJson = prefs.getString("exam_attempts_json", "[]") ?: "[]"
        val list = mutableListOf<ExamAttempt>()
        try {
            val array = JSONArray(attemptsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val userAnsObj = obj.optJSONObject("userAnswers") ?: JSONObject()
                val userAnsMap = mutableMapOf<Int, Int>()
                val keys = userAnsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    k.toIntOrNull()?.let { qNum ->
                        userAnsMap[qNum] = userAnsObj.getInt(k)
                    }
                }

                list.add(
                    ExamAttempt(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        examId = obj.optInt("examId", 1),
                        examTitle = obj.optString("examTitle", "Đề thi"),
                        score = obj.optInt("score", 0),
                        totalQuestions = obj.optInt("totalQuestions", 25),
                        isPassed = obj.optBoolean("isPassed", false),
                        failedCriticalQuestion = obj.optBoolean("failedCriticalQuestion", false),
                        durationSeconds = obj.optLong("durationSeconds", 0),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        userAnswers = userAnsMap
                    )
                )
            }
        } catch (_: Exception) {}
        _examAttemptsFlow.value = list

        // Aggregate wrong questions from all saved fixed exam attempts into wrongQuestionIds
        val wrongIdsFromExams = mutableSetOf<String>()
        for (attempt in list) {
            val exam = _exams.find { it.examId == attempt.examId } ?: continue
            for (eq in exam.questions) {
                val userAns = attempt.userAnswers[eq.questionNumber]
                if (userAns != null && userAns != eq.correctAnswer) {
                    val matchingQ = _questions.find {
                        it.question.trim().equals(eq.question.trim(), ignoreCase = true) ||
                                it.question.contains(eq.question.take(25))
                    }
                    if (matchingQ != null) {
                        wrongIdsFromExams.add(matchingQ.id)
                    }
                }
            }
        }
        if (wrongIdsFromExams.isNotEmpty()) {
            addWrongQuestions(wrongIdsFromExams)
        }
    }

    fun addWrongQuestions(newIds: Set<String>) {
        if (newIds.isEmpty()) return
        val current = _userProgressFlow.value
        val updatedSet = current.wrongQuestionIds + newIds
        if (updatedSet.size != current.wrongQuestionIds.size) {
            _userProgressFlow.value = current.copy(wrongQuestionIds = updatedSet)
            prefs.edit().putStringSet("wrong_questions", updatedSet).apply()
        }
    }

    fun submitAnswer(questionId: String, selectedOptionId: Int, isCorrect: Boolean) {
        val current = _userProgressFlow.value
        val newAnsweredMap = current.answeredMap.toMutableMap()
        newAnsweredMap[questionId] = selectedOptionId

        val newWrongSet = current.wrongQuestionIds.toMutableSet()
        if (isCorrect) {
            newWrongSet.remove(questionId)
        } else {
            newWrongSet.add(questionId)
        }

        val newTotalAnswered = newAnsweredMap.size
        // calculate accurate correct count
        var correctTotal = 0
        for ((qId, selectedOpt) in newAnsweredMap) {
            val q = _questions.find { it.id == qId }
            if (q != null && q.correctAnswer == selectedOpt) {
                correctTotal++
            }
        }

        val updated = current.copy(
            totalAnswered = newTotalAnswered,
            correctCount = correctTotal,
            wrongQuestionIds = newWrongSet,
            answeredMap = newAnsweredMap
        )
        _userProgressFlow.value = updated

        // Persist
        val ansJson = JSONObject()
        for ((k, v) in newAnsweredMap) {
            ansJson.put(k, v)
        }
        prefs.edit()
            .putInt("total_answered", newTotalAnswered)
            .putInt("correct_count", correctTotal)
            .putStringSet("wrong_questions", newWrongSet)
            .putString("answered_map_json", ansJson.toString())
            .apply()
    }

    fun toggleBookmark(questionId: String) {
        val current = _userProgressFlow.value
        val newBookmarks = current.bookmarkedQuestionIds.toMutableSet()
        if (newBookmarks.contains(questionId)) {
            newBookmarks.remove(questionId)
        } else {
            newBookmarks.add(questionId)
        }
        val updated = current.copy(bookmarkedQuestionIds = newBookmarks)
        _userProgressFlow.value = updated
        prefs.edit().putStringSet("bookmarked_questions", newBookmarks).apply()
    }

    fun saveLastStudyIndex(index: Int) {
        _userProgressFlow.value = _userProgressFlow.value.copy(lastStudyQuestionIndex = index)
        prefs.edit().putInt("last_study_index", index).apply()
    }

    fun saveExamAttempt(attempt: ExamAttempt) {
        val currentList = _examAttemptsFlow.value.toMutableList()
        currentList.add(0, attempt)
        _examAttemptsFlow.value = currentList

        // persist
        val array = JSONArray()
        for (item in currentList.take(50)) { // keep last 50
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("examId", item.examId)
            obj.put("examTitle", item.examTitle)
            obj.put("score", item.score)
            obj.put("totalQuestions", item.totalQuestions)
            obj.put("isPassed", item.isPassed)
            obj.put("failedCriticalQuestion", item.failedCriticalQuestion)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("timestamp", item.timestamp)
            val userAnsJson = JSONObject()
            for ((qNum, optId) in item.userAnswers) {
                userAnsJson.put(qNum.toString(), optId)
            }
            obj.put("userAnswers", userAnsJson)
            array.put(obj)
        }
        prefs.edit().putString("exam_attempts_json", array.toString()).apply()

        // Also record wrong questions from this exam attempt into wrongQuestionIds
        val exam = _exams.find { it.examId == attempt.examId }
        val examQuestions = exam?.questions ?: emptyList()
        val wrongIdsFromThisExam = mutableSetOf<String>()
        for (eq in examQuestions) {
            val userAns = attempt.userAnswers[eq.questionNumber]
            if (userAns != null && userAns != eq.correctAnswer) {
                val matchingQ = _questions.find {
                    it.question.trim().equals(eq.question.trim(), ignoreCase = true) ||
                            it.question.contains(eq.question.take(25))
                }
                if (matchingQ != null) {
                    wrongIdsFromThisExam.add(matchingQ.id)
                }
            }
        }
        if (wrongIdsFromThisExam.isNotEmpty()) {
            addWrongQuestions(wrongIdsFromThisExam)
        }
    }

    fun generateRandomExam(): Exam {
        // Build a randomized exam with 25 questions following GTVT standard
        // ~2 to 4 critical questions, ~10 law/rules, ~1-2 cultural/skills, ~7 signs, ~6 diagrams
        val criticalQuestions = _questions.filter { it.isCritical }.shuffled()
        val nonCriticalQuestions = _questions.filter { !it.isCritical }

        val selectedCritical = criticalQuestions.take(3)
        val selectedOthers = nonCriticalQuestions.shuffled().take(22)

        val combined = (selectedCritical + selectedOthers).shuffled()

        val examQuestions = combined.mapIndexed { idx, q ->
            ExamQuestion(
                questionNumber = idx + 1,
                question = q.question,
                isCritical = q.isCritical,
                correctAnswer = q.correctAnswer,
                options = q.options,
                images = q.images,
                explanation = q.explanation
            )
        }

        return Exam(
            examId = 999,
            title = "Đề thi ngẫu nhiên",
            examType = "random_exam",
            totalQuestions = 25,
            durationMinutes = 19,
            passingScore = 21,
            requireNoCriticalError = true,
            questions = examQuestions
        )
    }

    fun getCategoryStats(): List<CategoryStat> {
        val categories = listOf(
            "Khái niệm và quy tắc",
            "Văn hóa giao thông",
            "Kỹ thuật lái xe",
            "Biển báo",
            "Sa hình"
        )
        val currentProgress = _userProgressFlow.value

        return categories.map { cat ->
            val catQuestions = _questions.filter { it.category == cat }
            val total = catQuestions.size
            var answered = 0
            var correct = 0
            for (q in catQuestions) {
                val ans = currentProgress.answeredMap[q.id]
                if (ans != null) {
                    answered++
                    if (ans == q.correctAnswer) {
                        correct++
                    }
                }
            }
            CategoryStat(
                categoryName = cat,
                total = total,
                answered = answered,
                correct = correct
            )
        }
    }

    fun getTrickyQuestions(): List<Question> {
        // High failure rate questions: all critical questions + confusing signs + roundabout right-of-way
        val criticals = _questions.filter { it.isCritical }
        val trickyKeywords = listOf("vòng xuyến", "quay đầu", "còi", "kéo", "nồng độ cồn", "CSGT", "đường đôi", "đường cao tốc", "gương chiếu hậu", "phanh")
        val trickyGeneral = _questions.filter { q ->
            !q.isCritical && trickyKeywords.any { kw -> q.question.contains(kw, ignoreCase = true) }
        }
        return (criticals + trickyGeneral).distinctBy { it.id }.take(35)
    }

    fun clearHistory() {
        _examAttemptsFlow.value = emptyList()
        prefs.edit().remove("exam_attempts_json").apply()
    }

    fun resetAllProgress() {
        _userProgressFlow.value = UserProgress()
        _examAttemptsFlow.value = emptyList()
        prefs.edit().clear().apply()
    }
}
