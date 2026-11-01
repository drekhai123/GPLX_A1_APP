package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.StudyFilterMode
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun StatsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val examAttempts by viewModel.examAttempts.collectAsState()
    val categoryStats = remember(userProgress) { viewModel.repository.getCategoryStats() }

    val totalQuestions = 250
    val answeredCount = userProgress.totalAnswered
    val learnedCount = userProgress.correctCount
    val wrongCount = userProgress.wrongQuestionIds.size

    val criticalTotal = 20
    val criticalAnswered = viewModel.repository.questions
        .filter { it.isCritical && userProgress.answeredMap.containsKey(it.id) }.size
    val criticalCorrect = viewModel.repository.questions
        .filter { it.isCritical && userProgress.answeredMap[it.id] == it.correctAnswer }.size

    // Overall readiness formula
    val examPassCount = examAttempts.count { it.isPassed }
    val examTotal = examAttempts.size

    val readinessPercent = remember(answeredCount, learnedCount, criticalCorrect, examPassCount, examTotal) {
        val coverageScore = (answeredCount * 30 / totalQuestions).coerceIn(0, 30)
        val accuracyScore = if (answeredCount > 0) (learnedCount * 30 / answeredCount).coerceIn(0, 30) else 0
        val criticalScore = (criticalCorrect * 25 / criticalTotal).coerceIn(0, 25)
        val examScore = if (examTotal > 0) (examPassCount * 15 / examTotal).coerceIn(0, 15) else 0
        coverageScore + accuracyScore + criticalScore + examScore
    }

    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AppHeader(
            title = "Thống kê tiến độ",
            subtitle = "KẾT QUẢ RÈN LUYỆN",
            showBack = false,
            badgeText = "HẠNG A1"
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big Readiness Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("stats_readiness_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ĐỘ SẴN SÀNG THI SÁT HẠCH",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Circular Gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color.White.copy(alpha = 0.15f),
                                strokeWidth = 10.dp
                            )
                            CircularProgressIndicator(
                                progress = { (readinessPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxSize(),
                                color = if (readinessPercent >= 80) GreenSuccess else GoldAccent,
                                strokeWidth = 10.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$readinessPercent%",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tự tin đỗ",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when {
                                readinessPercent >= 85 -> "Tuyệt vời! Bạn đã sẵn sàng 100% để thi đỗ bằng lái A1."
                                readinessPercent >= 60 -> "Khá tốt! Hãy ôn thêm các câu hỏi điểm liệt và làm đủ 10 đề thi."
                                else -> "Cần cố gắng thêm! Hãy học theo từng chủ đề và luyện đề mỗi ngày."
                            },
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Critical Questions Alert Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (criticalCorrect == 20) GreenSuccess else Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (criticalCorrect == 20) GreenSuccess else RedAlert,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "20 Câu điểm liệt chống trượt",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }

                            Text(
                                text = "$criticalCorrect / $criticalTotal câu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (criticalCorrect == 20) GreenSuccess else RedAlert
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (criticalCorrect.toFloat() / criticalTotal).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (criticalCorrect == 20) GreenSuccess else RedAlert,
                            trackColor = Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (criticalCorrect == 20) {
                                "✓ Bạn đã nắm vững toàn bộ 20 câu điểm liệt!"
                            } else {
                                "Còn ${criticalTotal - criticalCorrect} câu điểm liệt chưa làm hoặc chưa đúng. Hãy ôn ngay!"
                            },
                            fontSize = 12.sp,
                            color = if (criticalCorrect == 20) GreenDark else RedDark
                        )
                    }
                }
            }

            // Category progress breakdown
            item {
                Text(
                    text = "Tiến độ theo 5 chủ đề câu hỏi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            items(categoryStats.size) { idx ->
                val stat = categoryStats[idx]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.categoryName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "${stat.answered}/${stat.total} câu (${stat.progressPercent}%)",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (stat.progressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NavyPrimary,
                            trackColor = Color(0xFFF1F5F9)
                        )
                    }
                }
            }

            // Exam summary card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kết quả thi thử sát hạch",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$examTotal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Text(text = "Đã thi", fontSize = 11.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$examPassCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                Text(text = "Số đề ĐẠT", fontSize = 11.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val passRate = if (examTotal > 0) (examPassCount * 100 / examTotal) else 0
                                Text(text = "$passRate%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                                Text(text = "Tỉ lệ đỗ", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (wrongCount > 0) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.WRONG_ONLY)) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ôn lại $wrongCount câu làm sai", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAlert),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đặt lại toàn bộ tiến độ")
                    }
                }
            }
        }
        StandardBannerAd()
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Đặt lại tiến độ học tập?") },
            text = { Text("Toàn bộ số câu đã làm, câu trả lời sai, câu đã lưu và lịch sử thi sẽ được xóa và đặt lại từ đầu.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        viewModel.resetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
                ) {
                    Text("Đặt lại ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
