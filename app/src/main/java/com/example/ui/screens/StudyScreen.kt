package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.example.model.Question
import com.example.ui.MainViewModel
import com.example.ui.StudyFilterMode
import com.example.ui.components.AppHeader
import com.example.ui.components.ImageViewerDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    initialFilterMode: StudyFilterMode = StudyFilterMode.ALL,
    initialCategory: String = "",
    initialQuestionIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val allQuestions = viewModel.repository.questions

    var selectedMode by remember { mutableStateOf(initialFilterMode) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var showExplanationInstantly by remember { mutableStateOf(false) }
    var showGridSheet by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter questions based on mode & search
    val filteredQuestions = remember(selectedMode, selectedCategory, userProgress, searchQuery) {
        val baseList = when (selectedMode) {
            StudyFilterMode.ALL -> allQuestions
            StudyFilterMode.CRITICAL_ONLY -> allQuestions.filter { it.isCritical }
            StudyFilterMode.CATEGORY -> if (selectedCategory.isNotEmpty()) {
                allQuestions.filter { it.category == selectedCategory }
            } else allQuestions
            StudyFilterMode.WRONG_ONLY -> allQuestions.filter { userProgress.wrongQuestionIds.contains(it.id) }
            StudyFilterMode.BOOKMARKED_ONLY -> allQuestions.filter { userProgress.bookmarkedQuestionIds.contains(it.id) }
            StudyFilterMode.TRICKY_ONLY -> viewModel.repository.getTrickyQuestions()
        }

        if (searchQuery.isNotBlank()) {
            baseList.filter {
                it.question.contains(searchQuery, ignoreCase = true) ||
                        it.explanation.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
            }
        } else {
            baseList
        }
    }

    var currentIndex by remember(filteredQuestions) {
        val safeIndex = initialQuestionIndex.coerceIn(0, (filteredQuestions.size - 1).coerceAtLeast(0))
        mutableIntStateOf(safeIndex)
    }

    // sync index when questions list changes
    if (currentIndex >= filteredQuestions.size && filteredQuestions.isNotEmpty()) {
        currentIndex = filteredQuestions.size - 1
    }

    val currentQuestion = filteredQuestions.getOrNull(currentIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AppHeader(
            title = when (selectedMode) {
                StudyFilterMode.ALL -> "250 Câu hỏi ôn luyện"
                StudyFilterMode.CRITICAL_ONLY -> "20 Câu điểm liệt"
                StudyFilterMode.CATEGORY -> selectedCategory.ifEmpty { "Ôn tập theo chủ đề" }
                StudyFilterMode.WRONG_ONLY -> "Câu hỏi làm sai"
                StudyFilterMode.BOOKMARKED_ONLY -> "Câu hỏi đã lưu"
                StudyFilterMode.TRICKY_ONLY -> "Câu người mới hay sai"
            },
            subtitle = "HỌC TẬP LÝ THUYẾT A1",
            showBack = true,
            badgeText = "${filteredQuestions.size} CÂU",
            onBackClick = { viewModel.navigateBack() }
        )

        // Filter chips row (hidden in WRONG_ONLY mode as requested)
        if (selectedMode != StudyFilterMode.WRONG_ONLY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill(
                    label = "Tất cả (${allQuestions.size})",
                    isSelected = selectedMode == StudyFilterMode.ALL,
                    onClick = { selectedMode = StudyFilterMode.ALL; currentIndex = 0 }
                )
                FilterPill(
                    label = "20 Câu liệt ⚠️",
                    isSelected = selectedMode == StudyFilterMode.CRITICAL_ONLY,
                    highlight = true,
                    onClick = { selectedMode = StudyFilterMode.CRITICAL_ONLY; currentIndex = 0 }
                )
                FilterPill(
                    label = "Biển báo (${allQuestions.count { it.category == "Biển báo" }})",
                    isSelected = selectedMode == StudyFilterMode.CATEGORY && selectedCategory == "Biển báo",
                    onClick = { selectedMode = StudyFilterMode.CATEGORY; selectedCategory = "Biển báo"; currentIndex = 0 }
                )
                FilterPill(
                    label = "Sa hình (${allQuestions.count { it.category == "Sa hình" }})",
                    isSelected = selectedMode == StudyFilterMode.CATEGORY && selectedCategory == "Sa hình",
                    onClick = { selectedMode = StudyFilterMode.CATEGORY; selectedCategory = "Sa hình"; currentIndex = 0 }
                )
                FilterPill(
                    label = "Khái niệm & Quy tắc (${allQuestions.count { it.category == "Khái niệm và quy tắc" }})",
                    isSelected = selectedMode == StudyFilterMode.CATEGORY && selectedCategory == "Khái niệm và quy tắc",
                    onClick = { selectedMode = StudyFilterMode.CATEGORY; selectedCategory = "Khái niệm và quy tắc"; currentIndex = 0 }
                )
                FilterPill(
                    label = "Kỹ thuật lái xe (${allQuestions.count { it.category == "Kỹ thuật lái xe" }})",
                    isSelected = selectedMode == StudyFilterMode.CATEGORY && selectedCategory == "Kỹ thuật lái xe",
                    onClick = { selectedMode = StudyFilterMode.CATEGORY; selectedCategory = "Kỹ thuật lái xe"; currentIndex = 0 }
                )
                FilterPill(
                    label = "Văn hóa GT (${allQuestions.count { it.category == "Văn hóa giao thông" }})",
                    isSelected = selectedMode == StudyFilterMode.CATEGORY && selectedCategory == "Văn hóa giao thông",
                    onClick = { selectedMode = StudyFilterMode.CATEGORY; selectedCategory = "Văn hóa giao thông"; currentIndex = 0 }
                )
                FilterPill(
                    label = "Câu làm sai (${userProgress.wrongQuestionIds.size})",
                    isSelected = selectedMode == StudyFilterMode.WRONG_ONLY,
                    onClick = { selectedMode = StudyFilterMode.WRONG_ONLY; currentIndex = 0 }
                )
                FilterPill(
                    label = "Đã lưu (${userProgress.bookmarkedQuestionIds.size})",
                    isSelected = selectedMode == StudyFilterMode.BOOKMARKED_ONLY,
                    onClick = { selectedMode = StudyFilterMode.BOOKMARKED_ONLY; currentIndex = 0 }
                )
                FilterPill(
                    label = "Hay bẫy",
                    isSelected = selectedMode == StudyFilterMode.TRICKY_ONLY,
                    onClick = { selectedMode = StudyFilterMode.TRICKY_ONLY; currentIndex = 0 }
                )
            }
        }

        // Search & Toggle Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick Jump button
            OutlinedButton(
                onClick = { showGridSheet = false },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("study_grid_button")
            ) {
                Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (filteredQuestions.isNotEmpty()) "Câu ${currentIndex + 1}/${filteredQuestions.size}" else "0/0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            // Toggle show explanation immediately
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showExplanationInstantly = !showExplanationInstantly }
                    .padding(4.dp)
            ) {
                Text(
                    text = "Hiện mẹo ngay",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (showExplanationInstantly) NavyPrimary else TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = showExplanationInstantly,
                    onCheckedChange = { showExplanationInstantly = it },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        if (filteredQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Không có câu hỏi nào trong mục này!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy chọn mục khác hoặc quay lại 'Tất cả câu hỏi'.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedMode = StudyFilterMode.ALL },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Xem tất cả 250 câu")
                    }
                }
            }
        } else if (currentQuestion != null) {
            val q = currentQuestion
            val userSelectedOpt = userProgress.answeredMap[q.id]
            val isBookmarked = userProgress.bookmarkedQuestionIds.contains(q.id)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    // Question Header Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = q.category,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleBookmark(q.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Lưu câu hỏi",
                                        tint = if (isBookmarked) GoldAccent else TextMuted
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
                                            text = "CÂU ĐIỂM LIỆT — Trả lời sai bị TRƯỢT NGAY LẬP TỨC!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RedDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "${currentIndex + 1}. ${q.question}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                lineHeight = 22.sp
                            )

                            // Image if available
                            if (q.images.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val img = q.images.first()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
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
                                    Surface(
                                        color = NavyPrimary.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Phóng to",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Options list
                items(q.options) { option ->
                    val isSelected = (userSelectedOpt == option.id)
                    val isThisCorrect = (option.id == q.correctAnswer)

                    val isSelectedAndWrong = isSelected && !isThisCorrect
                    val isSelectedAndCorrect = isSelected && isThisCorrect
                    val shouldHighlightCorrect = (showExplanationInstantly && isThisCorrect) || isSelectedAndCorrect

                    val (containerColor, borderColor, textColor) = when {
                        shouldHighlightCorrect -> Triple(Color(0xFFECFDF5), GreenSuccess, GreenDark)
                        isSelectedAndWrong -> Triple(Color(0xFFFEF2F2), RedAlert, RedDark)
                        else -> Triple(Color.White, BorderSubtle, TextPrimary)
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.submitStudyAnswer(q, option.id)
                                viewModel.repository.saveLastStudyIndex(currentIndex)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option Letter / Number Circle
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            shouldHighlightCorrect -> GreenSuccess
                                            isSelectedAndWrong -> RedAlert
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    shouldHighlightCorrect -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    isSelectedAndWrong -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "${option.id}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option.text,
                                fontSize = 14.sp,
                                fontWeight = if (shouldHighlightCorrect || isSelectedAndWrong) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Explanation & Golden Tip Card
                item {
                    val showExplanation = showExplanationInstantly
                    AnimatedVisibility(visible = showExplanation && q.explanation.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = GoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mẹo ghi nhớ & Giải thích:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = q.explanation,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Controller
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) {
                                currentIndex--
                                viewModel.repository.saveLastStudyIndex(currentIndex)
                            }
                        },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Câu trước", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Text(
                        text = "${currentIndex + 1} / ${filteredQuestions.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Button(
                        onClick = {
                            if (currentIndex < filteredQuestions.size - 1) {
                                currentIndex++
                                viewModel.repository.saveLastStudyIndex(currentIndex)
                            }
                        },
                        enabled = currentIndex < filteredQuestions.size - 1,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Câu tiếp", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    // Grid Jumper Bottom Sheet
    if (showGridSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGridSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Danh sách câu hỏi (${filteredQuestions.size} câu)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    itemsIndexed(filteredQuestions) { idx, q ->
                        val ans = userProgress.answeredMap[q.id]
                        val isCorrect = ans != null && ans == q.correctAnswer
                        val isWrong = ans != null && ans != q.correctAnswer
                        val isCurrent = idx == currentIndex

                        val bg = when {
                            isCurrent -> NavyPrimary
                            isCorrect -> GreenSuccess
                            isWrong -> RedAlert
                            q.isCritical -> Color(0xFFFEE2E2)
                            else -> Color(0xFFF1F5F9)
                        }

                        val textColor = when {
                            isCurrent || isCorrect || isWrong -> Color.White
                            q.isCritical -> RedDark
                            else -> TextPrimary
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable {
                                    currentIndex = idx
                                    showGridSheet = false
                                }
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

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Zoom Image Dialog
    if (zoomImageUrl != null) {
        ImageViewerDialog(
            imageUrl = zoomImageUrl ?: "",
            onDismiss = { zoomImageUrl = null }
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected && highlight -> RedAlert
        isSelected -> NavyPrimary
        highlight -> Color(0xFFFEE2E2)
        else -> Color(0xFFF1F5F9)
    }

    val textCol = when {
        isSelected -> Color.White
        highlight -> RedDark
        else -> TextSecondary
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
