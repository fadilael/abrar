package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceStatus
import com.example.ui.theme.*
import com.example.ui.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceListBottomSheet(
    selectedType: String, // "present" or "absent"
    items: List<StudentAttendanceStatus>,
    lang: String,
    onDismiss: () -> Unit,
    onSwitchType: (String) -> Unit,
    onMarkAttendance: (Student) -> Unit,
    onNavigateToStudents: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val presentItems = remember(items) { items.filter { it.isPresent } }
    val absentItems = remember(items) { items.filter { !it.isPresent } }

    val currentList = if (selectedType == "present") presentItems else absentItems
    val filteredList = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) currentList
        else currentList.filter {
            it.student.fullName.contains(searchQuery, ignoreCase = true) ||
            it.student.studentId.contains(searchQuery, ignoreCase = true) ||
            (it.student.roomName?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BgMain,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (selectedType == "present") StatusSuccess else StatusDanger)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedType == "present") {
                            if (lang == "ar") "قائمة الحاضرين اليوم (${presentItems.size})"
                            else "Liste des Présents (${presentItems.size})"
                        } else {
                            if (lang == "ar") "قائمة الغائبين اليوم (${absentItems.size})"
                            else "Liste des Absents (${absentItems.size})"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetrolBlue
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                // Present tab
                Surface(
                    onClick = { onSwitchType("present") },
                    shape = RoundedCornerShape(9.dp),
                    color = if (selectedType == "present") StatusSuccess else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("tab_present_students")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedType == "present") Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${AppStrings.t("present", lang)} (${presentItems.size})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == "present") Color.White else TextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Absent tab
                Surface(
                    onClick = { onSwitchType("absent") },
                    shape = RoundedCornerShape(9.dp),
                    color = if (selectedType == "absent") StatusDanger else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("tab_absent_students")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (selectedType == "absent") Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${AppStrings.t("absent", lang)} (${absentItems.size})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == "absent") Color.White else TextMuted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search in bottom sheet
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attendance_sheet_search"),
                placeholder = {
                    Text(
                        if (lang == "ar") "بحث عن تلميذ..." else "Rechercher un élève...",
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PetrolBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // List of items
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedType == "present") Icons.Default.CheckCircle else Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedType == "present") {
                                if (lang == "ar") "لا يوجد تلاميذ حاضرين حتى الآن" else "Aucun élève présent"
                            } else {
                                if (lang == "ar") "لا يوجد تلاميذ غائبين، ممتاز!" else "Aucun absent, parfait !"
                            },
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.student.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.student.fullName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextMain
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${item.student.studentId} • ${item.student.roomName ?: "Salle 01"} (${item.student.section ?: "1AC"})",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                        )
                                    }

                                    // Status Badge
                                    if (item.isPresent) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StatusSuccessBg
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = StatusSuccess,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.scanTime ?: AppStrings.t("present", lang),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = StatusSuccess,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StatusDangerBg
                                        ) {
                                            Text(
                                                text = AppStrings.t("absent", lang),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = StatusDanger,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // If absent: show quick actions (Call parent & Mark Present)
                                if (!item.isPresent) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val parentNumber = item.student.parentPhone ?: item.student.phone
                                        if (!parentNumber.isNullOrBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                                        data = Uri.parse("tel:$parentNumber")
                                                    }
                                                    context.startActivity(callIntent)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = "Call",
                                                    tint = PetrolBlue,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (lang == "ar") "اتصال بالولي" else "Appeler",
                                                    style = MaterialTheme.typography.labelMedium.copy(color = PetrolBlue)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { onMarkAttendance(item.student) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Present",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "ar") "تسجيل حضور" else "Pointer",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View all in Students page CTA
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onNavigateToStudents()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = PetrolBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == "ar") "فتح صفحة التلاميذ الكاملة" else "Voir la liste complète des élèves",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PetrolBlue
                    )
                )
            }
        }
    }
}
