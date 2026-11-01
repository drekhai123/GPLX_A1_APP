package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Exam
import com.example.ui.MainViewModel
import com.example.ui.components.ImageViewerDialog
import com.example.ui.theme.*

@Composable
fun ExamTakingScreen(
    viewModel: MainViewModel,
    exam: Exam,
    modifier: Modifier = Modifier
) {
    val examState by viewModel.activeExamState.collectAsState()
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    // Intercept hardware/system back button
    BackHandler {
        showExitDialog = true
    }

    val currentQuestionIndex = examState.currentQuestionIndex
    val questions = exam.questions
    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    val remainingSecs = examState.remainingSeconds
    val minutes = remainingSecs / 60
    val seconds = remainingSecs % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    val isTimerLow = remainingSecs < 180 // less than 3 mins

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        // Sticky Exam Top Bar
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Thoát thi thử",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = exam.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isTimerLow) RedAlert else GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timerText,
                                color = if (isTimerLow) RedAlert else GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_submit_exam")
                    ) {
                        Text(
                            text = "Nộp bài",
                            color = NavyDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Strip (1 to 25)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    questions.forEachIndexed { idx, q ->
                        val isCurrent = idx == currentQuestionIndex
                        val hasAnswer = examState.userAnswers.containsKey(q.questionNumber)
                        val isFlagged = examState.flaggedQuestionNumbers.contains(q.questionNumber)

                        val bg = when {
                            isCurrent -> GoldAccent
                            isFlagged -> Color(0xFFF97316)
                            hasAnswer -> BlueAccent
                            else -> Color.White.copy(alpha = 0.2f)
                        }

                        val textColor = when {
                            isCurrent -> NavyDark
                            isFlagged || hasAnswer -> Color.White
                            else -> Color.White.copy(alpha = 0.8f)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { viewModel.setExamQuestionIndex(idx) }
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Question Content
        if (currentQuestion != null) {
            val q = currentQuestion
            val selectedOption = examState.userAnswers[q.questionNumber]
            val isFlagged = examState.flaggedQuestionNumbers.contains(q.questionNumber)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Câu ${currentQuestionIndex + 1} / ${questions.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleFlagQuestion(q.questionNumber) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                        contentDescription = "Đánh dấu câu hỏi",
                                        tint = if (isFlagged) Color(0xFFF97316) else TextMuted
                                    )
                                }
                            }

                            if (q.isCritical) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = RedLight,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = RedAlert,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CÂU ĐIỂM LIỆT — Sai câu này bị trượt ngay lập tức!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RedDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = q.question,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                lineHeight = 22.sp
                            )

                            if (q.images.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val img = q.images.first()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
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
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Options
                items(q.options) { option ->
                    val isSelected = selectedOption == option.id

                    val bg = if (isSelected) Color(0xFFEFF6FF) else Color.White
                    val border = if (isSelected) BlueAccent else BorderSubtle

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectExamAnswer(q.questionNumber, option.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BlueAccent else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${option.id}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else NavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.text,
                                fontSize = 14.sp,
                                color = if (isSelected) NavyPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Controls
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setExamQuestionIndex(currentQuestionIndex - 1) },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Câu trước")
                    }

                    Text(
                        text = "Đã làm ${examState.userAnswers.size}/${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                viewModel.setExamQuestionIndex(currentQuestionIndex + 1)
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text(if (currentQuestionIndex < questions.size - 1) "Câu tiếp" else "Nộp bài")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val answeredCount = examState.userAnswers.size
        val totalCount = exam.questions.size
        val unansweredCount = totalCount - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Text(text = "Xác nhận nộp bài thi?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Bạn đã hoàn thành: $answeredCount / $totalCount câu hỏi.")
                    if (unansweredCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cảnh báo: Còn $unansweredCount câu bạn chưa chọn đáp án!",
                            color = RedAlert,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Thời gian còn lại: $timerText. Bạn có chắc chắn muốn nộp bài ngay bây giờ?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Nộp bài ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Làm tiếp")
                }
            }
        )
    }

    // Exit Exam Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(text = "Thoát khỏi bài thi?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Nếu bạn thoát ngay bây giờ, tiến trình làm bài thi này sẽ không được lưu.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.navigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
                ) {
                    Text("Thoát bài thi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục làm")
                }
            }
        )
    }

    if (zoomImageUrl != null) {
        ImageViewerDialog(
            imageUrl = zoomImageUrl ?: "",
            onDismiss = { zoomImageUrl = null }
        )
    }
}
