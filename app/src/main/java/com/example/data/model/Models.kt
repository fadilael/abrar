package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Generic API Envelope ──────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "details") val details: String? = null,
    @Json(name = "meta") val meta: PaginationMeta? = null
)

@JsonClass(generateAdapter = true)
data class PaginationMeta(
    @Json(name = "total") val total: Int = 0,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "per_page") val perPage: Int = 25,
    @Json(name = "pages") val pages: Int = 0
)

// ── Authentication ───────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "username") val username: String = "",
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "role") val role: String = "admin" // admin, teacher, viewer
)

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "token") val token: String = "",
    @Json(name = "user") val user: User? = null,
    @Json(name = "expires_in") val expiresIn: Long = 604800
)

// ── Site Settings ─────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class SiteSettings(
    @Json(name = "site_name_ar") val siteNameAr: String = "مجموعة مدارس الأبرار",
    @Json(name = "site_name_fr") val siteNameFr: String = "Groupe Scolaire Al Abrar",
    @Json(name = "brand_tag") val brandTag: String = "AL ABRAR GROUPE SCOLAIRE",
    @Json(name = "logo_url") val logoUrl: String = "",
    @Json(name = "logo_emoji") val logoEmoji: String = "🎓",
    @Json(name = "primary_color") val primaryColor: String = "#075A6E",
    @Json(name = "secondary_color") val secondaryColor: String = "#F15A24",
    @Json(name = "footer_text_ar") val footerTextAr: String = "مجموعة مدارس الأبرار © 2026/2027",
    @Json(name = "footer_text_fr") val footerTextFr: String = "Groupe Scolaire Al Abrar © 2026/2027"
)

// ── Rooms ─────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class Room(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "level") val level: String = "",
    @Json(name = "section") val section: String = "",
    @Json(name = "year_id") val yearId: Int = 1,
    @Json(name = "capacity") val capacity: Int = 30,
    @Json(name = "student_count") val studentCount: Int = 0,
    @Json(name = "year_label") val yearLabel: String? = null,
    @Json(name = "total_students") val totalStudents: Int = 0,
    @Json(name = "present") val present: Int = 0
)

@JsonClass(generateAdapter = true)
data class RoomUpsertRequest(
    @Json(name = "name") val name: String,
    @Json(name = "level") val level: String,
    @Json(name = "section") val section: String,
    @Json(name = "capacity") val capacity: Int = 30,
    @Json(name = "year_id") val yearId: Int = 1
)

// ── Students ──────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class Student(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "student_id") val studentId: String = "",
    @Json(name = "first_name") val firstName: String = "",
    @Json(name = "last_name") val lastName: String = "",
    @Json(name = "room_id") val roomId: Int = 0,
    @Json(name = "room_name") val roomName: String? = null,
    @Json(name = "section") val section: String? = null,
    @Json(name = "level") val level: String? = null,
    @Json(name = "status") val status: String = "active", // active, inactive
    @Json(name = "qr_token") val qrToken: String = "",
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "parent_phone") val parentPhone: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

@JsonClass(generateAdapter = true)
data class StudentUpsertRequest(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "room_id") val roomId: Int,
    @Json(name = "status") val status: String = "active",
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "parent_phone") val parentPhone: String? = null
)

// ── Attendance ────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ScanRequest(
    @Json(name = "qr_token") val qrToken: String
)

@JsonClass(generateAdapter = true)
data class ScanRecord(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "student_id") val studentId: Int = 0,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_code") val studentCode: String = "",
    @Json(name = "room_name") val roomName: String = "",
    @Json(name = "action") val action: String = "in", // "in" or "out"
    @Json(name = "time") val time: String = "",
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class AttendanceStats(
    @Json(name = "total") val total: Int = 0,
    @Json(name = "present") val present: Int = 0,
    @Json(name = "absent") val absent: Int = 0,
    @Json(name = "late") val late: Int = 0,
    @Json(name = "date") val date: String = ""
)

data class StudentAttendanceStatus(
    val student: Student,
    val isPresent: Boolean,
    val scanTime: String? = null,
    val action: String = if (isPresent) "in" else "absent"
)

@JsonClass(generateAdapter = true)
data class TodayAttendanceData(
    @Json(name = "stats") val stats: AttendanceStats? = null,
    @Json(name = "recent_scans") val recentScans: List<ScanRecord> = emptyList(),
    @Json(name = "by_room") val byRoom: List<Room> = emptyList()
)

// ── Reports ───────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class ReportPeriod(
    @Json(name = "type") val type: String = "daily",
    @Json(name = "from") val from: String = "",
    @Json(name = "to") val to: String = "",
    @Json(name = "label") val label: String = ""
)

@JsonClass(generateAdapter = true)
data class ReportTotals(
    @Json(name = "total_students") val totalStudents: Int = 0,
    @Json(name = "total_days") val totalDays: Int = 1,
    @Json(name = "avg_attendance_rate") val avgAttendanceRate: Double = 0.0,
    @Json(name = "avg_present_per_day") val avgPresentPerDay: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class DailyBreakdownItem(
    @Json(name = "date") val date: String = "",
    @Json(name = "present") val present: Int = 0,
    @Json(name = "absent") val absent: Int = 0,
    @Json(name = "late") val late: Int = 0,
    @Json(name = "total") val total: Int = 0,
    @Json(name = "rate") val rate: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class RoomBreakdownItem(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "section") val section: String = "",
    @Json(name = "level") val level: String = "",
    @Json(name = "total_students") val totalStudents: Int = 0,
    @Json(name = "rate") val rate: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class StudentReportItem(
    @Json(name = "student_id") val studentId: String = "",
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "room_name") val roomName: String = "",
    @Json(name = "section") val section: String = "",
    @Json(name = "days_present") val daysPresent: Int = 0,
    @Json(name = "days_absent") val daysAbsent: Int = 0,
    @Json(name = "rate") val rate: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ReportData(
    @Json(name = "period") val period: ReportPeriod? = null,
    @Json(name = "totals") val totals: ReportTotals? = null,
    @Json(name = "daily_breakdown") val dailyBreakdown: List<DailyBreakdownItem> = emptyList(),
    @Json(name = "by_room") val byRoom: List<RoomBreakdownItem> = emptyList(),
    @Json(name = "students") val students: List<StudentReportItem> = emptyList()
)
