package com.example.data.remote

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AlAbrarApiService {

    // ── Authentication ───────────────────────────────────────────
    @POST("api/v1/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    // ── Settings ──────────────────────────────────────────────────
    @GET("api/v1/settings/index.php")
    suspend fun getSettings(): Response<ApiResponse<SiteSettings>>

    @PUT("api/v1/settings/index.php")
    suspend fun updateSettings(@Body settings: Map<String, Any?>): Response<ApiResponse<SiteSettings>>

    // ── Rooms ─────────────────────────────────────────────────────
    @GET("api/v1/rooms/index.php")
    suspend fun getRooms(
        @Query("search") search: String? = null,
        @Query("year_id") yearId: Int? = null
    ): Response<ApiResponse<List<Room>>>

    @GET("api/v1/rooms/index.php")
    suspend fun getRoom(@Query("id") id: Int): Response<ApiResponse<Room>>

    @POST("api/v1/rooms/index.php")
    suspend fun createRoom(@Body request: RoomUpsertRequest): Response<ApiResponse<Room>>

    @PUT("api/v1/rooms/index.php")
    suspend fun updateRoom(
        @Query("id") id: Int,
        @Body request: RoomUpsertRequest
    ): Response<ApiResponse<Room>>

    @DELETE("api/v1/rooms/index.php")
    suspend fun deleteRoom(@Query("id") id: Int): Response<ApiResponse<Unit>>

    // ── Students ──────────────────────────────────────────────────
    @GET("api/v1/students/index.php")
    suspend fun getStudents(
        @Query("search") search: String? = null,
        @Query("room_id") roomId: Int? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiResponse<List<Student>>>

    @GET("api/v1/students/index.php")
    suspend fun getStudent(@Query("id") id: Int): Response<ApiResponse<Student>>

    @POST("api/v1/students/index.php")
    suspend fun createStudent(@Body request: StudentUpsertRequest): Response<ApiResponse<Student>>

    @PUT("api/v1/students/index.php")
    suspend fun updateStudent(
        @Query("id") id: Int,
        @Body request: StudentUpsertRequest
    ): Response<ApiResponse<Student>>

    @DELETE("api/v1/students/index.php")
    suspend fun deleteStudent(@Query("id") id: Int): Response<ApiResponse<Unit>>

    // ── Attendance ────────────────────────────────────────────────
    @POST("api/v1/attendance/scan.php")
    suspend fun scanAttendance(@Body request: ScanRequest): Response<ApiResponse<ScanRecord>>

    @GET("api/v1/attendance/today.php")
    suspend fun getTodayAttendance(@Query("room_id") roomId: Int? = null): Response<ApiResponse<TodayAttendanceData>>

    // ── Reports ───────────────────────────────────────────────────
    @GET("api/v1/reports/summary.php")
    suspend fun getReportSummary(
        @Query("period") period: String = "daily",
        @Query("date") date: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("room_id") roomId: Int? = null
    ): Response<ApiResponse<ReportData>>
}
