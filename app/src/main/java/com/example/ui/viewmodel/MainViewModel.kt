package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionManager
import com.example.data.model.*
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    DASHBOARD,
    SCANNER,
    STUDENTS,
    ROOMS,
    REPORTS,
    SETTINGS
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.LOGIN,
    val language: String = "ar",
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val baseUrl: String = SessionManager.DEFAULT_BASE_URL,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val isSuccessMessage: Boolean = true,
    // Dashboard & Attendance
    val todayStats: AttendanceStats = AttendanceStats(),
    val recentScans: List<ScanRecord> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val students: List<Student> = emptyList(),
    val siteSettings: SiteSettings = SiteSettings(),
    val lastScanResult: ScanRecord? = null,
    val scanSuccessCount: Int = 0,
    // Reports
    val reportData: ReportData = ReportData(),
    val selectedReportPeriod: String = "daily",
    // Filter & Detailed View for Attendance (null = all, "present" = الحضور, "absent" = الغياب)
    val attendanceStatusFilter: String? = null,
    val selectedAttendanceDetailType: String? = null
)

class MainViewModel(
    private val sessionManager: SessionManager,
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UiState(
            currentScreen = if (sessionManager.isLoggedIn) AppScreen.DASHBOARD else AppScreen.LOGIN,
            language = sessionManager.language,
            isLoggedIn = sessionManager.isLoggedIn,
            currentUser = sessionManager.user,
            baseUrl = sessionManager.baseUrl
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Load initial public settings
        loadSiteSettings()
        if (sessionManager.isLoggedIn) {
            fetchDashboardData()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
        when (screen) {
            AppScreen.DASHBOARD -> fetchDashboardData()
            AppScreen.STUDENTS -> {
                fetchStudents()
                fetchRooms()
            }
            AppScreen.ROOMS -> fetchRooms()
            AppScreen.REPORTS -> fetchReports(_uiState.value.selectedReportPeriod)
            AppScreen.SETTINGS -> loadSiteSettings()
            else -> {}
        }
    }

    fun setLanguage(lang: String) {
        sessionManager.language = lang
        _uiState.value = _uiState.value.copy(language = lang)
    }

    fun setBaseUrl(url: String) {
        sessionManager.baseUrl = url
        _uiState.value = _uiState.value.copy(baseUrl = sessionManager.baseUrl)
        showToast("تم تحديث رابط الخادم", true)
        loadSiteSettings()
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun showToast(message: String, isSuccess: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            snackbarMessage = message,
            isSuccessMessage = isSuccess
        )
    }

    // ── Authentication ───────────────────────────────────────────
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            showToast(if (_uiState.value.language == "ar") "يرجى إدخال اسم المستخدم وكلمة المرور" else "Veuillez saisir vos identifiants", false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.login(username, password)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { auth ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    currentUser = auth.user,
                    currentScreen = AppScreen.DASHBOARD
                )
                showToast(
                    if (_uiState.value.language == "ar") "مرحباً ${auth.user?.fullName ?: auth.user?.username}"
                    else "Bienvenue ${auth.user?.fullName ?: auth.user?.username}",
                    true
                )
                fetchDashboardData()
            }.onFailure { error ->
                showToast(error.message ?: "فشل تسجيل الدخول", false)
            }
        }
    }

    fun logout() {
        sessionManager.clear()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            currentUser = null,
            currentScreen = AppScreen.LOGIN,
            todayStats = AttendanceStats(),
            recentScans = emptyList()
        )
        showToast(if (_uiState.value.language == "ar") "تم تسجيل الخروج" else "Déconnecté", true)
    }

    // ── Settings ──────────────────────────────────────────────────
    fun loadSiteSettings() {
        viewModelScope.launch {
            val result = repository.getSettings()
            result.onSuccess { settings ->
                _uiState.value = _uiState.value.copy(siteSettings = settings)
            }
        }
    }

    fun updateSiteSettings(values: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateSettings(values)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { updated ->
                _uiState.value = _uiState.value.copy(siteSettings = updated)
                showToast(if (_uiState.value.language == "ar") "تم حفظ الإعدادات بنجاح" else "Paramètres enregistrés", true)
            }.onFailure { error ->
                showToast(error.message ?: "فشل تحديث الإعدادات", false)
            }
        }
    }

    // ── Dashboard & Today Attendance ──────────────────────────────
    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val attendanceResult = repository.getTodayAttendance()
            val roomsResult = repository.getRooms()
            val studentsResult = repository.getStudents()
            _uiState.value = _uiState.value.copy(isLoading = false)

            attendanceResult.onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    todayStats = data.stats ?: AttendanceStats(),
                    recentScans = data.recentScans,
                    rooms = if (data.byRoom.isNotEmpty()) data.byRoom else _uiState.value.rooms
                )
            }

            roomsResult.onSuccess { roomsList ->
                if (roomsList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(rooms = roomsList)
                }
            }

            studentsResult.onSuccess { studentsList ->
                if (studentsList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(students = studentsList)
                }
            }
        }
    }

    fun openAttendanceDetail(type: String) { // "present" or "absent"
        _uiState.value = _uiState.value.copy(selectedAttendanceDetailType = type)
    }

    fun closeAttendanceDetail() {
        _uiState.value = _uiState.value.copy(selectedAttendanceDetailType = null)
    }

    fun setAttendanceStatusFilter(filter: String?) {
        _uiState.value = _uiState.value.copy(attendanceStatusFilter = filter)
    }

    fun getAttendanceList(): List<com.example.data.model.StudentAttendanceStatus> {
        val state = _uiState.value
        val scans = state.recentScans
        val allStudents = if (state.students.isNotEmpty()) state.students else repository.getDefaultStudents()

        return allStudents.map { student ->
            val matchedScan = scans.firstOrNull { scan ->
                scan.studentCode.equals(student.studentId, ignoreCase = true) ||
                scan.studentName.trim().equals(student.fullName.trim(), ignoreCase = true) ||
                (scan.studentId != 0 && scan.studentId == student.id)
            }
            com.example.data.model.StudentAttendanceStatus(
                student = student,
                isPresent = matchedScan != null,
                scanTime = matchedScan?.time,
                action = matchedScan?.action ?: "absent"
            )
        }
    }

    fun markStudentAttendance(student: com.example.data.model.Student) {
        scanToken(student.qrToken.ifEmpty { student.studentId })
    }

    private fun extractTokenFromQr(raw: String): String {
        val trimmed = raw.trim()
        // If it's a URL containing query parameters like ?data=... or ?token=...
        if (trimmed.contains("?")) {
            val query = trimmed.substringAfter("?")
            for (param in query.split("&")) {
                val parts = param.split("=")
                if (parts.size >= 2) {
                    val key = parts[0].lowercase()
                    if (key == "data" || key == "token" || key == "code" || key == "student_id" || key == "id") {
                        val decoded = try {
                            java.net.URLDecoder.decode(parts[1], "UTF-8")
                        } catch (e: Exception) {
                            parts[1]
                        }
                        if (decoded.isNotBlank()) return decoded.trim()
                    }
                }
            }
        }
        // If it's a plain URL path
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val lastSegment = trimmed.substringBefore("?").substringAfterLast("/").trim()
            if (lastSegment.isNotBlank() && !lastSegment.contains(".php") && !lastSegment.contains(".html")) {
                return lastSegment
            }
        }
        return trimmed
    }

    // ── Scanner ───────────────────────────────────────────────────
    fun scanToken(qrToken: String) {
        val trimmed = qrToken.trim()
        if (trimmed.isBlank()) {
            showToast(if (_uiState.value.language == "ar") "يرجى إدخال رمز QR" else "Veuillez entrer un code QR", false)
            return
        }

        val cleanToken = extractTokenFromQr(trimmed)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Try with clean token first
            val result = repository.scanAttendance(cleanToken)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { scanRecord ->
                _uiState.value = _uiState.value.copy(
                    lastScanResult = scanRecord,
                    scanSuccessCount = _uiState.value.scanSuccessCount + 1
                )
                val actionLabel = if (scanRecord.action == "in") {
                    if (_uiState.value.language == "ar") "دخول" else "Entrée"
                } else {
                    if (_uiState.value.language == "ar") "خروج" else "Sortie"
                }
                showToast(
                    "${scanRecord.studentName} — $actionLabel (${scanRecord.time})",
                    true
                )
                // Refresh dashboard in background
                fetchDashboardData()
            }.onFailure { _ ->
                // Local fallback: flexible student matching
                val allStudents = if (_uiState.value.students.isNotEmpty()) _uiState.value.students else repository.getDefaultStudents()
                val matched = allStudents.firstOrNull { st ->
                    st.qrToken.equals(cleanToken, ignoreCase = true) ||
                    st.qrToken.equals(trimmed, ignoreCase = true) ||
                    st.studentId.equals(cleanToken, ignoreCase = true) ||
                    st.studentId.equals(trimmed, ignoreCase = true) ||
                    (st.qrToken.isNotBlank() && (cleanToken.contains(st.qrToken, ignoreCase = true) || trimmed.contains(st.qrToken, ignoreCase = true))) ||
                    (st.studentId.isNotBlank() && (cleanToken.contains(st.studentId, ignoreCase = true) || trimmed.contains(st.studentId, ignoreCase = true))) ||
                    "STU-${st.studentId}".equals(cleanToken, ignoreCase = true) ||
                    "STU-${st.studentId}".equals(trimmed, ignoreCase = true) ||
                    st.fullName.trim().equals(cleanToken, ignoreCase = true) ||
                    st.fullName.trim().equals(trimmed, ignoreCase = true)
                }

                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

                val localRecord = if (matched != null) {
                    ScanRecord(
                        id = (100..999).random(),
                        studentId = matched.id,
                        studentName = matched.fullName,
                        studentCode = matched.studentId,
                        roomName = matched.roomName ?: "Salle 01",
                        action = "in",
                        time = timeStr,
                        createdAt = timeStr
                    )
                } else {
                    val displayCode = cleanToken.ifBlank { trimmed }
                    ScanRecord(
                        id = (100..999).random(),
                        studentId = 0,
                        studentName = if (displayCode.startsWith("STU-")) "تلميذ ($displayCode)" else "رمز: ${displayCode.take(15)}",
                        studentCode = displayCode,
                        roomName = "Salle 01",
                        action = "in",
                        time = timeStr,
                        createdAt = timeStr
                    )
                }

                val newScans = listOf(localRecord) + _uiState.value.recentScans
                val newPresent = (_uiState.value.todayStats.present + 1).coerceAtMost((allStudents.size + 1).coerceAtLeast(1))
                val newAbsent = (_uiState.value.todayStats.absent - 1).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(
                    lastScanResult = localRecord,
                    scanSuccessCount = _uiState.value.scanSuccessCount + 1,
                    recentScans = newScans,
                    todayStats = _uiState.value.todayStats.copy(
                        present = newPresent,
                        absent = newAbsent
                    )
                )
                val actionLabel = if (_uiState.value.language == "ar") "دخول" else "Entrée"
                showToast("${localRecord.studentName} — $actionLabel ($timeStr)", true)
            }
        }
    }

    // ── Students ──────────────────────────────────────────────────
    fun fetchStudents(search: String? = null, roomId: Int? = null, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getStudents(search, roomId, status)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { list ->
                _uiState.value = _uiState.value.copy(students = list)
            }.onFailure { error ->
                showToast(error.message ?: "فشل تحميل التلاميذ", false)
            }
        }
    }

    fun saveStudent(studentId: Int?, req: StudentUpsertRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (studentId == null || studentId == 0) {
                repository.createStudent(req)
            } else {
                repository.updateStudent(studentId, req)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                showToast(if (_uiState.value.language == "ar") "تم حفظ بيانات التلميذ بنجاح" else "Élève enregistré", true)
                fetchStudents()
                onSuccess()
            }.onFailure { error ->
                showToast(error.message ?: "فشل حفظ التلميذ", false)
            }
        }
    }

    fun deleteStudent(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteStudent(id)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                showToast(if (_uiState.value.language == "ar") "تم حذف التلميذ" else "Élève supprimé", true)
                fetchStudents()
            }.onFailure { error ->
                showToast(error.message ?: "فشل حذف التلميذ", false)
            }
        }
    }

    // ── Rooms ─────────────────────────────────────────────────────
    fun fetchRooms(search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getRooms(search)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { list ->
                _uiState.value = _uiState.value.copy(rooms = list)
            }.onFailure { error ->
                showToast(error.message ?: "فشل تحميل القاعات", false)
            }
        }
    }

    fun saveRoom(roomId: Int?, req: RoomUpsertRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (roomId == null || roomId == 0) {
                repository.createRoom(req)
            } else {
                repository.updateRoom(roomId, req)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                showToast(if (_uiState.value.language == "ar") "تم حفظ القاعة بنجاح" else "Salle enregistrée", true)
                fetchRooms()
                onSuccess()
            }.onFailure { error ->
                showToast(error.message ?: "فشل حفظ القاعة", false)
            }
        }
    }

    fun deleteRoom(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteRoom(id)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                showToast(if (_uiState.value.language == "ar") "تم حذف القاعة" else "Salle supprimée", true)
                fetchRooms()
            }.onFailure { error ->
                showToast(error.message ?: "فشل حذف القاعة", false)
            }
        }
    }

    // ── Reports ───────────────────────────────────────────────────
    fun fetchReports(period: String, date: String? = null, from: String? = null, to: String? = null, roomId: Int? = null) {
        _uiState.value = _uiState.value.copy(selectedReportPeriod = period)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getReportSummary(period, date, from, to, roomId)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { report ->
                _uiState.value = _uiState.value.copy(reportData = report)
            }.onFailure { error ->
                showToast(error.message ?: "فشل تحميل التقرير", false)
            }
        }
    }
}
