package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanRecord
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceStatus
import com.example.ui.components.ActionBadge
import com.example.ui.components.AttendanceListBottomSheet
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    state: UiState,
    onNavigate: (AppScreen) -> Unit,
    onRefresh: () -> Unit,
    onOpenAttendanceDetail: (String) -> Unit = {},
    onCloseAttendanceDetail: () -> Unit = {},
    onMarkAttendance: (Student) -> Unit = {},
    attendanceList: List<StudentAttendanceStatus> = emptyList(),
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val stats = state.todayStats
    val total = stats.total.coerceAtLeast(1)
    val rate = if (stats.total > 0) ((stats.present.toFloat() / stats.total) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "ar") state.siteSettings.siteNameAr else state.siteSettings.siteNameFr,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${AppStrings.t("dashboard", lang)} • ${stats.date.ifEmpty { "اليوم" }}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }

                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Scan QR CTA Button
                    Button(
                        onClick = { onNavigate(AppScreen.SCANNER) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("dashboard_scan_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.t("scan_qr", lang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 4 Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = AppStrings.t("total_students", lang),
                    value = stats.total.toString(),
                    icon = Icons.Default.Groups,
                    accentColor = PetrolBlue,
                    bgColor = Color(0xFFE0F2F6),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.STUDENTS) }
                )
                StatCard(
                    title = AppStrings.t("present", lang),
                    value = stats.present.toString(),
                    icon = Icons.Default.CheckCircle,
                    accentColor = StatusSuccess,
                    bgColor = StatusSuccessBg,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenAttendanceDetail("present") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = AppStrings.t("absent", lang),
                    value = stats.absent.toString(),
                    icon = Icons.Default.Cancel,
                    accentColor = StatusDanger,
                    bgColor = StatusDangerBg,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenAttendanceDetail("absent") }
                )
                StatCard(
                    title = AppStrings.t("late", lang),
                    value = stats.late.toString(),
                    icon = Icons.Default.Schedule,
                    accentColor = StatusWarning,
                    bgColor = StatusWarningBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Attendance Progress Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("attendance_rate", lang),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Text(
                            text = "$rate%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (rate >= 80) StatusSuccess else BrandOrange
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { stats.present.toFloat() / total.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (rate >= 80) StatusSuccess else BrandOrange,
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // By Room Section Header
        if (state.rooms.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.t("by_room", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetrolBlue
                        )
                    )
                    TextButton(onClick = { onNavigate(AppScreen.ROOMS) }) {
                        Text(
                            text = AppStrings.t("rooms", lang),
                            color = BrandOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Room breakdown items
            items(state.rooms.take(4)) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PetrolBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = PetrolBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain
                                    )
                                )
                                Text(
                                    text = "${room.level} • ${room.section}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${room.present} / ${if (room.totalStudents > 0) room.totalStudents else room.studentCount}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolBlue
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Recent Scans Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.t("recent_scans", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )
            }
        }

        // Recent Scans List
        if (state.recentScans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.t("no_scans", lang),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(state.recentScans) { scan ->
                RecentScanCard(scan = scan)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Attendance Details Bottom Sheet (Present or Absent students)
    if (state.selectedAttendanceDetailType != null) {
        AttendanceListBottomSheet(
            selectedType = state.selectedAttendanceDetailType,
            items = attendanceList,
            lang = lang,
            onDismiss = onCloseAttendanceDetail,
            onSwitchType = onOpenAttendanceDetail,
            onMarkAttendance = onMarkAttendance,
            onNavigateToStudents = { onNavigate(AppScreen.STUDENTS) }
        )
    }
}

@Composable
fun RecentScanCard(scan: ScanRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PetrolBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scan.studentName.take(1).ifEmpty { "T" },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetrolBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = scan.studentName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    )
                    Text(
                        text = "${scan.studentCode} • ${scan.roomName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                ActionBadge(action = scan.action)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scan.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
