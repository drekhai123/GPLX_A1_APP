package com.example

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.BottomNavTab
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this)
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreenState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = currentScreen !is AppScreen.Home && currentScreen !is AppScreen.ExamTaking) {
        viewModel.navigateBack()
    }

    val showBottomBar = when (currentScreen) {
        is AppScreen.ExamTaking -> false
        else -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentTab = activeTab,
                    onTabSelected = { tab -> viewModel.selectBottomTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Home -> HomeScreen(viewModel = viewModel)
                is AppScreen.Study -> StudyScreen(
                    viewModel = viewModel,
                    initialFilterMode = screen.filterMode,
                    initialCategory = screen.categoryName,
                    initialQuestionIndex = screen.initialQuestionIndex
                )
                is AppScreen.ExamList -> ExamListScreen(viewModel = viewModel)
                is AppScreen.ExamTaking -> ExamTakingScreen(viewModel = viewModel, exam = screen.exam)
                is AppScreen.ExamResultReview -> ExamResultScreen(
                    viewModel = viewModel,
                    exam = screen.exam,
                    attempt = screen.attempt
                )
                is AppScreen.TrafficSigns -> TrafficSignsScreen(viewModel = viewModel)
                is AppScreen.MemoryTips -> MemoryTipsScreen(viewModel = viewModel)
                is AppScreen.ExamHistory -> ExamHistoryScreen(viewModel = viewModel)
                is AppScreen.Stats -> StatsScreen(viewModel = viewModel)
            }
        }
    }
}
