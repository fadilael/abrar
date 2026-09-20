package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Room
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceStatus
import com.example.data.model.StudentUpsertRequest
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    state: UiState,
    onSearch: (String) -> Unit,
    onSaveStudent: (Int?, StudentUpsertRequest, () -> Unit) -> Unit,
    onDeleteStudent: (Int) -> Unit,
    attendanceList: List<StudentAttendanceStatus> = emptyList(),
    onMarkAttendance: (Student) -> Unit = {},
    onSetStatusFilter: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = state.language
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember(state.attendanceStatusFilter) {
        mutableStateOf(state.attendanceStatusFilter)
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingQrStudent by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    val attendanceMap = remember(attendanceList) {
        attendanceList.associateBy { it.student.id }
    }
    val presentCount = remember(attendanceList) { attendanceList.count { it.isPresent } }
    val absentCount = remember(attendanceList) { attendanceList.count { !it.isPresent } }

    val filteredStudents = remember(state.students, searchQuery, selectedFilter, attendanceMap) {
        val base = if (searchQuery.isBlank()) state.students
        else state.students.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.studentId.contains(searchQuery, ignoreCase = true) ||
            (it.roomName?.contains(searchQuery, ignoreCase = true) == true)
        }

        when (selectedFilter) {
            "present" -> base.filter { attendanceMap[it.id]?.isPresent == true }
            "absent" -> base.filter { attendanceMap[it.id]?.isPresent != true }
            else -> base
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search and Add Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearch(it)
                    },
                    placeholder = { Text(AppStrings.t("search", lang), color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PetrolBlue)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearch("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("students_search_input"),
                    colors = appTextFieldColors()
                )

                Button(
                    onClick = {
                        editingStudent = null
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("add_student_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("add", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Attendance Filter Chips: All, Present Today, Absent Today
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = {
                        selectedFilter = null
                        onSetStatusFilter(null)
                    },
                    label = { Text("${AppStrings.t("all", lang)} (${state.students.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PetrolBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_all_students")
                )

                FilterChip(
                    selected = selectedFilter == "present",
                    onClick = {
                        val next = if (selectedFilter == "present") null else "present"
                        selectedFilter = next
                        onSetStatusFilter(next)
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${AppStrings.t("present", lang)} ($presentCount)")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusSuccess,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_present_students")
                )

                FilterChip(
                    selected = selectedFilter == "absent",
                    onClick = {
                        val next = if (selectedFilter == "absent") null else "absent"
                        selectedFilter = next
                        onSetStatusFilter(next)
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusDanger)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${AppStrings.t("absent", lang)} ($absentCount)")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusDanger,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_absent_students")
                )
            }

            // Student Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppStrings.t("students", lang)} (${filteredStudents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )
            }

            // List of Students
            if (filteredStudents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PeopleOutline,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = AppStrings.t("empty_data", lang),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val attendanceStatus = attendanceMap[student.id]
                        StudentItemCard(
                            student = student,
                            attendanceStatus = attendanceStatus,
                            lang = lang,
                            onViewQr = { viewingQrStudent = student },
                            onEdit = {
                                editingStudent = student
                                showAddDialog = true
                            },
                            onDelete = { studentToDelete = student },
                            onMarkAttendance = { onMarkAttendance(student) }
                        )
                    }
                }
            }
        }

        // Add / Edit Student Dialog
        if (showAddDialog) {
            StudentUpsertDialog(
                initial = editingStudent,
                rooms = state.rooms,
                lang = lang,
                onDismiss = { showAddDialog = false },
                onSave = { req ->
                    onSaveStudent(editingStudent?.id, req) {
                        showAddDialog = false
                    }
                }
            )
        }

        // View QR Dialog
        viewingQrStudent?.let { student ->
            StudentQrDialog(
                student = student,
                lang = lang,
                onDismiss = { viewingQrStudent = null }
            )
        }

        // Delete Confirm Dialog
        studentToDelete?.let { student ->
            ConfirmDialog(
                title = AppStrings.t("delete_student", lang),
                message = "${AppStrings.t("confirm_delete", lang)}: ${student.fullName} (${student.studentId})",
                confirmText = AppStrings.t("delete", lang),
                cancelText = AppStrings.t("cancel", lang),
                onConfirm = {
                    onDeleteStudent(student.id)
                    studentToDelete = null
                },
                onDismiss = { studentToDelete = null }
            )
        }
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    attendanceStatus: StudentAttendanceStatus?,
    lang: String,
    onViewQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAttendance: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PetrolBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.firstName.take(1).ifEmpty { "S" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PetrolBlue
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.fullName.ifEmpty { "تلميذ" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Text(
                            text = "Matricule: ${student.studentId}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                StatusBadge(
                    text = if (student.status == "active") AppStrings.t("active", lang) else AppStrings.t("inactive", lang),
                    isActive = student.status == "active"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Today Attendance Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (attendanceStatus?.isPresent == true) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusSuccessBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${AppStrings.t("present", lang)} (${attendanceStatus.scanTime ?: "اليوم"})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusDangerBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = StatusDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${AppStrings.t("absent", lang)} اليوم",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDanger
                                )
                            )
                        }
                    }
                }

                // If absent: quick action to mark attendance
                if (attendanceStatus?.isPresent != true) {
                    Surface(
                        onClick = onMarkAttendance,
                        shape = RoundedCornerShape(8.dp),
                        color = StatusSuccess.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (lang == "ar") "تسجيل حضور" else "Pointer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room & Token row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "🏛 ${student.roomName ?: "Salle"} ${student.section ?: ""}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PetrolBlue
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (student.qrToken.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandOrange.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "QR: ${student.qrToken}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandOrangeDark
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BorderColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onViewQr) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = PetrolBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.t("view_qr", lang), color = PetrolBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.t("edit", lang), color = TextMuted, fontSize = 12.sp)
                }

                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.t("delete", lang), color = StatusDanger, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StudentUpsertDialog(
    initial: Student?,
    rooms: List<Room>,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (StudentUpsertRequest) -> Unit
) {
    var firstName by remember { mutableStateOf(initial?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initial?.lastName ?: "") }
    var studentId by remember { mutableStateOf(initial?.studentId ?: "") }
    var selectedRoomId by remember {
        mutableStateOf(initial?.roomId ?: rooms.firstOrNull()?.id ?: 1)
    }
    var status by remember { mutableStateOf(initial?.status ?: "active") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var parentPhone by remember { mutableStateOf(initial?.parentPhone ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initial == null) AppStrings.t("add_student", lang) else AppStrings.t("edit_student", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(AppStrings.t("first_name", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(AppStrings.t("last_name", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text(AppStrings.t("student_id", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Room Selector Row
                Text(
                    text = AppStrings.t("room", lang),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rooms.forEach { room ->
                        FilterChip(
                            selected = selectedRoomId == room.id,
                            onClick = { selectedRoomId = room.id },
                            label = { Text(room.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PetrolBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.t("cancel", lang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (firstName.isNotBlank() && studentId.isNotBlank()) {
                                onSave(
                                    StudentUpsertRequest(
                                        studentId = studentId.trim(),
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        roomId = selectedRoomId,
                                        status = status,
                                        phone = phone.ifBlank { null },
                                        parentPhone = parentPhone.ifBlank { null }
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text(AppStrings.t("save", lang))
                    }
                }
            }
        }
    }
}

@Composable
fun StudentQrDialog(
    student: Student,
    lang: String,
    onDismiss: () -> Unit
) {
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${student.qrToken.ifEmpty { "STU-${student.studentId}" }}"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )
                Text(
                    text = "${student.studentId} • ${student.roomName ?: ""}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = student.qrToken.ifEmpty { "STU-${student.studentId}" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandOrangeDark
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PetrolBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق / Fermer")
                }
            }
        }
    }
}
