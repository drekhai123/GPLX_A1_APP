package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val totalQuestions = 250
    val answeredCount = userProgress.totalAnswered
    val learnedCount = userProgress.correctCount
    val wrongCount = userProgress.wrongQuestionIds.size
    val progressPercent = if (totalQuestions > 0) (answeredCount * 100 / totalQuestions).coerceIn(0, 100) else 0
    val accuracyPercent = if (answeredCount > 0) (learnedCount * 100 / answeredCount).coerceIn(0, 100) else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AppHeader(
            title = "Ôn Thi GPLX A1",
            subtitle = "BẰNG LÁI XE MÁY A1 · A",
            badgeText = "HẠNG A1"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Roadmap Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_roadmap_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "LỘ TRÌNH CỦA BẠN",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vững lý thuyết.\nTự tin cầm lái.",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mỗi ngày một chút — tiến gần hơn đến bằng lái A1.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$progressPercent%",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hoàn thành bộ câu hỏi",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }

                            // Circular Progress Ring
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White.copy(alpha = 0.15f),
                                    strokeWidth = 6.dp
                                )
                                CircularProgressIndicator(
                                    progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxSize(),
                                    color = GoldAccent,
                                    strokeWidth = 6.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$answeredCount/$totalQuestions",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3 Stat Cards in a row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Đã thuộc",
                        value = "$learnedCount",
                        valueColor = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Cần ôn lại",
                        value = "$wrongCount",
                        valueColor = if (wrongCount > 0) RedAlert else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Chính xác",
                        value = "$accuracyPercent%",
                        valueColor = if (accuracyPercent >= 84) GreenSuccess else NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary Action Button "Tiếp tục học lý thuyết"
            item {
                Button(
                    onClick = {
                        viewModel.navigateTo(
                            AppScreen.Study(
                                filterMode = StudyFilterMode.ALL,
                                initialQuestionIndex = userProgress.lastStudyQuestionIndex
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("continue_study_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiếp tục học lý thuyết",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            // Section "Học tập & Rèn luyện" title
            item {
                Text(
                    text = "Học tập & Rèn luyện",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Grid of Study features
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: 250 câu hỏi & Thi thử A1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            icon = Icons.Default.MenuBook,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = NavyPrimary,
                            title = "250 câu hỏi",
                            subtitle = "Ôn luyện theo chủ đề",
                            testTag = "action_250_questions",
                            onClick = { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.ALL)) },
                            modifier = Modifier.weight(1f)
                        )

                        ActionCard(
                            icon = Icons.Default.Timer,
                            iconBg = Color(0xFFFEF3C7),
                            iconTint = GoldDark,
                            title = "Thi thử A1",
                            subtitle = "25 câu · 19 phút",
                            testTag = "action_mock_exam",
                            onClick = { viewModel.navigateTo(AppScreen.ExamList) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: 20 câu điểm liệt & Câu làm sai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            icon = Icons.Default.Warning,
                            iconBg = Color(0xFFFEE2E2),
                            iconTint = RedAlert,
                            title = "20 câu điểm liệt",
                            subtitle = "Sai là trượt ngay!",
                            subtitleColor = RedAlert,
                            testTag = "action_critical_questions",
                            onClick = { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.CRITICAL_ONLY)) },
                            modifier = Modifier.weight(1f)
                        )

                        ActionCard(
                            icon = Icons.Default.Cancel,
                            iconBg = Color(0xFFF1F5F9),
                            iconTint = if (wrongCount > 0) RedAlert else TextSecondary,
                            title = "Câu làm sai",
                            subtitle = "$wrongCount câu cần sửa",
                            testTag = "action_wrong_questions",
                            onClick = { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.WRONG_ONLY)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Tất cả biển báo & Mẹo thi 100% đậu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            icon = Icons.Default.Signpost,
                            iconBg = Color(0xFFE0F2FE),
                            iconTint = BlueCyan,
                            title = "Tất cả biển báo",
                            subtitle = "185 biển báo & vạch kẻ",
                            testTag = "action_traffic_signs",
                            onClick = { viewModel.navigateTo(AppScreen.TrafficSigns) },
                            modifier = Modifier.weight(1f)
                        )

                        ActionCard(
                            icon = Icons.Default.AutoAwesome,
                            iconBg = Color(0xFFFEF3C7),
                            iconTint = GoldDark,
                            title = "Mẹo thi 100% đậu",
                            subtitle = "Ghi nhớ thần tốc",
                            testTag = "action_memory_tips",
                            onClick = { viewModel.navigateTo(AppScreen.MemoryTips) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 4: Lịch sử thi thử & Sa hình & Kỹ thuật
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            icon = Icons.Default.History,
                            iconBg = Color(0xFFF1F5F9),
                            iconTint = NavyPrimary,
                            title = "Lịch sử thi thử",
                            subtitle = "Xem lại điểm & bài làm",
                            testTag = "action_exam_history",
                            onClick = { viewModel.navigateTo(AppScreen.ExamHistory) },
                            modifier = Modifier.weight(1f)
                        )

                        ActionCard(
                            icon = Icons.Default.DirectionsCar,
                            iconBg = Color(0xFFF1F5F9),
                            iconTint = NavyPrimary,
                            title = "Sa hình & Kỹ thuật",
                            subtitle = "Quy tắc nhường đường",
                            testTag = "action_sa_hinh",
                            onClick = { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.CATEGORY, categoryName = "Sa hình")) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 5: Những câu người mới thường sai
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { viewModel.navigateTo(AppScreen.Study(StudyFilterMode.TRICKY_ONLY)) }
                            .testTag("action_tricky_questions")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF2F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = RedAlert,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Những câu người mới thường sai",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "35 câu hỏi 'bẫy' nhiều học viên mất điểm nhất",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color = TextSecondary,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = subtitleColor,
                maxLines = 1
            )
        }
    }
}
