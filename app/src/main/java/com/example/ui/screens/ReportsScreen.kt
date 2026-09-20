package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentReportItem
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.util.PdfReportGenerator
import com.example.ui.viewmodel.UiState

@Composable
fun ReportsScreen(
    state: UiState,
    onSelectPeriod: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = state.language
    val report = state.reportData
    val totals = report.totals
    var isGeneratingPdf by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector chips
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
                            text = AppStrings.t("reports", lang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PetrolBlue
                            )
                        )
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = PetrolBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("daily", "weekly", "monthly").forEach { p ->
                            FilterChip(
                                selected = state.selectedReportPeriod == p,
                                onClick = { onSelectPeriod(p) },
                                label = { Text(AppStrings.t(p, lang)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PetrolBlue,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("report_period_$p")
                            )
                        }
                    }

                    report.period?.label?.let { label ->
                        if (label.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "الفترة: $label",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Download PDF button
                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            try {
                                val pdfFile = PdfReportGenerator.generateAndShareReport(
                                    context = context,
                                    report = report,
                                    period = state.selectedReportPeriod,
                                    siteSettings = state.siteSettings,
                                    studentsList = state.students
                                )
                                isGeneratingPdf = false
                                if (pdfFile != null) {
                                    PdfReportGenerator.openOrSharePdf(context, pdfFile)
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (lang == "ar") "فشل إنشاء ملف PDF" else "Échec de génération du PDF",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                isGeneratingPdf = false
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isGeneratingPdf,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_pdf_report_button")
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "جاري إنشاء التقرير..." else "Génération...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "تنزيل ومشاركة التقرير PDF" else "Télécharger & Partager le PDF",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Key Metrics Summary
        if (totals != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = AppStrings.t("attendance_rate", lang),
                        value = "${totals.avgAttendanceRate.toInt()}%",
                        icon = Icons.Default.TrendingUp,
                        accentColor = if (totals.avgAttendanceRate >= 80) StatusSuccess else BrandOrange,
                        bgColor = if (totals.avgAttendanceRate >= 80) StatusSuccessBg else StatusWarningBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = AppStrings.t("total_students", lang),
                        value = totals.totalStudents.toString(),
                        icon = Icons.Default.Groups,
                        accentColor = PetrolBlue,
                        bgColor = Color(0xFFE0F2F6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Room breakdown
        if (report.byRoom.isNotEmpty()) {
            item {
                Text(
                    text = AppStrings.t("by_room", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )
            }

            items(report.byRoom) { r ->
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
                        Column {
                            Text(
                                text = r.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                            )
                            Text(
                                text = "${r.level} • ${r.section}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (r.rate >= 80) StatusSuccessBg else StatusWarningBg
                        ) {
                            Text(
                                text = "${r.rate.toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.rate >= 80) StatusSuccess else BrandOrangeDark
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Students ranking (most absent first)
        item {
            Text(
                text = AppStrings.t("most_absent", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PetrolBlue
                )
            )
        }

        if (report.students.isEmpty()) {
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
                            text = AppStrings.t("empty_data", lang),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(report.students) { s ->
                StudentReportCard(item = s, lang = lang)
            }
        }
    }
}

@Composable
fun StudentReportCard(item: StudentReportItem, lang: String) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                )
                Text(
                    text = "${item.studentId} • ${item.roomName} (${item.section})",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.daysAbsent} ${AppStrings.t("absent", lang)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = StatusDanger,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${item.daysPresent} ${AppStrings.t("present", lang)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.rate >= 80) StatusSuccessBg else StatusDangerBg
                ) {
                    Text(
                        text = "${item.rate.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.rate >= 80) StatusSuccess else StatusDanger
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
