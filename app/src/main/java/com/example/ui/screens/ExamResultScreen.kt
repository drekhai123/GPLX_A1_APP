package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Exam
import com.example.model.ExamAttempt
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.ImageViewerDialog
import com.example.ui.theme.*

@Composable
fun ExamResultScreen(
    viewModel: MainViewModel,
    exam: Exam,
    attempt: ExamAttempt,
    modifier: Modifier = Modifier
) {
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("Tất cả") } // "Tất cả", "Câu sai", "Câu điểm liệt"

    BackHandler {
        viewModel.navigateTo(AppScreen.ExamList, clearStack = true)
    }

    val minutes = attempt.durationSeconds / 60
    val seconds = attempt.durationSeconds % 60
    val durationText = "${minutes}p ${seconds}s"

    val correctCount = attempt.score
    val wrongCount = attempt.totalQuestions - correctCount

    val reviewQuestions = remember(selectedFilter, exam.questions, attempt.userAnswers) {
        when (selectedFilter) {
            "Câu sai" -> exam.questions.filter {
                val ans = attempt.userAnswers[it.questionNumber]
                ans == null || ans != it.correctAnswer
            }
            "Câu điểm liệt" -> exam.questions.filter { it.isCritical }
            else -> exam.questions
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AppHeader(
            title = "Kết quả sát hạch",
            subtitle = exam.title.uppercase(),
            showBack = true,
            badgeText = if (attempt.isPassed) "ĐẠT" else "TRƯỢT",
            onBackClick = { viewModel.navigateTo(AppScreen.ExamList, clearStack = true) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Result Banner Card
            item {
                val isPass = attempt.isPassed
                val bannerBg = if (isPass) GreenSuccess else RedAlert

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_result_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isPass) "CHÚC MỪNG BẠN ĐÃ ĐẠT! 🎉" else "RẤT TIẾC, BẠN CHƯA ĐẠT!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isPass) {
                                "Bạn đã hoàn thành xuất sắc bài thi sát hạch lý thuyết A1!"
                            } else if (attempt.failedCriticalQuestion) {
                                "LÝ DO: Làm sai câu hỏi điểm liệt! (Quy chế: Sai 01 câu liệt bị đánh trượt ngay lập tức)"
                            } else {
                                "Điểm số đạt ${attempt.score}/${attempt.totalQuestions} (yêu cầu tối thiểu 21/${attempt.totalQuestions} câu đúng)"
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${attempt.score}/25", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "Điểm số", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = durationText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "Thời gian làm", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (attempt.failedCriticalQuestion) "Sai ⚠️" else "Đạt chuẩn",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attempt.failedCriticalQuestion) GoldLight else Color.White
                                )
                                Text(text = "Câu điểm liệt", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startExam(exam) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_retake_exam")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thi lại đề này", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.ExamList, clearStack = true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Danh sách đề thi", fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    }
                }
            }

            // Filter Tabs for Review
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "Tất cả",
                        onClick = { selectedFilter = "Tất cả" },
                        label = { Text("Tất cả (${exam.questions.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Câu sai",
                        onClick = { selectedFilter = "Câu sai" },
                        label = { Text("Câu sai ($wrongCount)") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Câu điểm liệt",
                        onClick = { selectedFilter = "Câu điểm liệt" },
                        label = { Text("Câu điểm liệt") }
                    )
                }
            }

            // Detailed question reviews
            itemsIndexed(reviewQuestions) { _, q ->
                val userChoice = attempt.userAnswers[q.questionNumber]
                val isCorrect = userChoice != null && userChoice == q.correctAnswer
                val isUnanswered = userChoice == null

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isCorrect -> GreenSuccess.copy(alpha = 0.5f)
                            q.isCritical -> RedAlert.copy(alpha = 0.6f)
                            else -> BorderSubtle
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = when {
                                    isCorrect -> GreenLight
                                    isUnanswered -> Color(0xFFF1F5F9)
                                    else -> RedLight
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Câu ${q.questionNumber}: " + when {
                                        isCorrect -> "ĐÚNG"
                                        isUnanswered -> "CHƯA TRẢ LỜI"
                                        else -> "SAI"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isCorrect -> GreenDark
                                        isUnanswered -> TextSecondary
                                        else -> RedDark
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (q.isCritical) {
                                Surface(
                                    color = RedLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ĐIỂM LIỆT ⚠️",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedAlert,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = q.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )

                        if (q.images.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val img = q.images.first()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable { zoomImageUrl = img.src }
                            ) {
                                AsyncImage(
                                    model = img.src,
                                    contentDescription = img.alt,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // List answers
                        q.options.forEach { opt ->
                            val isUserPicked = userChoice == opt.id
                            val isCorrectOpt = opt.id == q.correctAnswer

                            val (bg, textCol) = when {
                                isCorrectOpt -> Pair(Color(0xFFECFDF5), GreenDark)
                                isUserPicked && !isCorrectOpt -> Pair(Color(0xFFFEF2F2), RedDark)
                                else -> Pair(Color(0xFFF8FAFC), TextSecondary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isCorrectOpt) GreenSuccess else if (isUserPicked) RedAlert else Color(0xFFCBD5E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCorrectOpt) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    } else if (isUserPicked) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    } else {
                                        Text(text = "${opt.id}", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = opt.text,
                                    fontSize = 13.sp,
                                    color = textCol,
                                    fontWeight = if (isCorrectOpt || isUserPicked) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (q.explanation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mẹo & Giải thích: ${q.explanation}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (zoomImageUrl != null) {
        ImageViewerDialog(
            imageUrl = zoomImageUrl ?: "",
            onDismiss = { zoomImageUrl = null }
        )
    }
}
