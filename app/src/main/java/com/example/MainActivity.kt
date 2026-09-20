package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SessionManager
import com.example.data.remote.ApiClient
import com.example.data.repository.AttendanceRepository
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.TopToastBanner
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        val apiClient = ApiClient(sessionManager)
        val repository = AttendanceRepository(sessionManager, apiClient)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(sessionManager, repository) as T
            }
        }

        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
            val uiState by mainViewModel.uiState.collectAsState()

            val layoutDirection = if (uiState.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (uiState.isLoggedIn) {
                                AppTopBar(
                                    currentScreen = uiState.currentScreen,
                                    currentLang = uiState.language,
                                    userName = uiState.currentUser?.fullName?.ifEmpty { uiState.currentUser?.username },
                                    userRole = uiState.currentUser?.role,
                                    onToggleLang = {
                                        mainViewModel.setLanguage(if (uiState.language == "ar") "fr" else "ar")
                                    },
                                    onLogout = { mainViewModel.logout() }
                                )
                            }
                        },
                        bottomBar = {
                            if (uiState.isLoggedIn) {
                                AppBottomBar(
                                    currentScreen = uiState.currentScreen,
                                    currentLang = uiState.language,
                                    userRole = uiState.currentUser?.role,
                                    onSelectScreen = { screen -> mainViewModel.navigateTo(screen) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.currentScreen) {
                                AppScreen.LOGIN -> {
                                    LoginScreen(
                                        state = uiState,
                                        onLogin = { u, p -> mainViewModel.login(u, p) },
                                        onToggleLang = {
                                            mainViewModel.setLanguage(if (uiState.language == "ar") "fr" else "ar")
                                        },
                                        onUpdateServerUrl = { url -> mainViewModel.setBaseUrl(url) }
                                    )
                                }
                                AppScreen.DASHBOARD -> {
                                    DashboardScreen(
                                        state = uiState,
                                        onNavigate = { screen -> mainViewModel.navigateTo(screen) },
                                        onRefresh = { mainViewModel.fetchDashboardData() },
                                        onOpenAttendanceDetail = { type -> mainViewModel.openAttendanceDetail(type) },
                                        onCloseAttendanceDetail = { mainViewModel.closeAttendanceDetail() },
                                        onMarkAttendance = { student -> mainViewModel.markStudentAttendance(student) },
                                        attendanceList = mainViewModel.getAttendanceList()
                                    )
                                }
                                AppScreen.SCANNER -> {
                                    ScannerScreen(
                                        state = uiState,
                                        onScanToken = { token -> mainViewModel.scanToken(token) }
                                    )
                                }
                                AppScreen.STUDENTS -> {
                                    StudentsScreen(
                                        state = uiState,
                                        onSearch = { query -> mainViewModel.fetchStudents(search = query) },
                                        onSaveStudent = { id, req, onSuccess ->
                                            mainViewModel.saveStudent(id, req, onSuccess)
                                        },
                                        onDeleteStudent = { id -> mainViewModel.deleteStudent(id) },
                                        attendanceList = mainViewModel.getAttendanceList(),
                                        onMarkAttendance = { student -> mainViewModel.markStudentAttendance(student) },
                                        onSetStatusFilter = { filter -> mainViewModel.setAttendanceStatusFilter(filter) }
                                    )
                                }
                                AppScreen.ROOMS -> {
                                    RoomsScreen(
                                        state = uiState,
                                        onSaveRoom = { id, req, onSuccess ->
                                            mainViewModel.saveRoom(id, req, onSuccess)
                                        },
                                        onDeleteRoom = { id -> mainViewModel.deleteRoom(id) }
                                    )
                                }
                                AppScreen.REPORTS -> {
                                    ReportsScreen(
                                        state = uiState,
                                        onSelectPeriod = { period -> mainViewModel.fetchReports(period) },
                                        onRefresh = { mainViewModel.fetchReports(uiState.selectedReportPeriod) }
                                    )
                                }
                                AppScreen.SETTINGS -> {
                                    SettingsScreen(
                                        state = uiState,
                                        onSaveSettings = { payload -> mainViewModel.updateSiteSettings(payload) },
                                        onUpdateServerUrl = { url -> mainViewModel.setBaseUrl(url) }
                                    )
                                }
                            }

                            // Floating Toast Banner
                            TopToastBanner(
                                message = uiState.snackbarMessage,
                                isSuccess = uiState.isSuccessMessage,
                                onDismiss = { mainViewModel.clearToast() },
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}
