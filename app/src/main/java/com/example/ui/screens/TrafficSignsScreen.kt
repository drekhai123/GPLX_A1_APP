package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.TrafficSign
import com.example.ui.MainViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.ImageViewerDialog
import com.example.ui.theme.*

@Composable
fun TrafficSignsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allSigns = viewModel.repository.signs
    var selectedCategoryCode by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedSignForDetail by remember { mutableStateOf<TrafficSign?>(null) }

    val categories = listOf(
        Pair("ALL", "Tất cả (${allSigns.size})"),
        Pair("prohibition", "Biển cấm (${allSigns.count { it.categoryCode == "prohibition" }})"),
        Pair("danger", "Biển nguy hiểm (${allSigns.count { it.categoryCode == "danger" }})"),
        Pair("command", "Biển hiệu lệnh (${allSigns.count { it.categoryCode == "command" }})"),
        Pair("guide", "Biển chỉ dẫn (${allSigns.count { it.categoryCode == "guide" }})"),
        Pair("auxiliary", "Biển phụ (${allSigns.count { it.categoryCode == "auxiliary" }})"),
        Pair("marking", "Vạch kẻ đường (${allSigns.count { it.categoryCode == "marking" }})"),
        Pair("other", "Khác (${allSigns.count { it.categoryCode == "other" }})")
    )

    val filteredSigns = remember(selectedCategoryCode, searchQuery, allSigns) {
        allSigns.filter { sign ->
            val matchesCategory = (selectedCategoryCode == "ALL" || sign.categoryCode == selectedCategoryCode)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                sign.name.contains(searchQuery, ignoreCase = true) ||
                        sign.description.contains(searchQuery, ignoreCase = true) ||
                        sign.categoryName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AppHeader(
            title = "Hệ thống biển báo",
            subtitle = "185 BIỂN BÁO & VẠCH KẺ ĐƯỜNG",
            showBack = true,
            badgeText = "${filteredSigns.size} BIỂN",
            onBackClick = { viewModel.navigateBack() }
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm kiếm biển báo, vạch kẻ đường...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = BorderSubtle
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_signs_input")
        )

        // Category pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (code, label) ->
                val isSelected = selectedCategoryCode == code
                Surface(
                    color = if (isSelected) NavyPrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyPrimary else BorderSubtle),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedCategoryCode = code }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of Traffic Signs
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSigns) { sign ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedSignForDetail = sign }
                        .testTag("sign_card_${sign.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sign.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = sign.imageUrl,
                                    contentDescription = sign.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = when (sign.categoryCode) {
                                "prohibition" -> RedLight
                                "danger" -> GoldLight
                                "command" -> Color(0xFFEFF6FF)
                                else -> Color(0xFFF1F5F9)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = sign.categoryName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (sign.categoryCode) {
                                    "prohibition" -> RedDark
                                    "danger" -> GoldDark
                                    "command" -> BlueAccent
                                    else -> TextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = sign.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog when clicking a sign
    selectedSignForDetail?.let { sign ->
        AlertDialog(
            onDismissRequest = { selectedSignForDetail = null },
            title = {
                Text(text = sign.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (sign.imageUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { zoomImageUrl = sign.imageUrl }
                        ) {
                            AsyncImage(
                                model = sign.imageUrl,
                                contentDescription = sign.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = "Ý nghĩa: ${sign.description.ifEmpty { sign.name }}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSignForDetail = null }) {
                    Text("Đóng")
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
