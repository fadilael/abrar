package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Room
import com.example.data.model.RoomUpsertRequest
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.UiState

@Composable
fun RoomsScreen(
    state: UiState,
    onSaveRoom: (Int?, RoomUpsertRequest, () -> Unit) -> Unit,
    onDeleteRoom: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<Room?>(null) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }

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
            // Header with Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppStrings.t("rooms", lang)} (${state.rooms.size})",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )

                Button(
                    onClick = {
                        editingRoom = null
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    modifier = Modifier.testTag("add_room_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("add_room", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Rooms List
            if (state.rooms.isEmpty()) {
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
                        Text(
                            text = AppStrings.t("empty_data", lang),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.rooms, key = { it.id }) { room ->
                        RoomCard(
                            room = room,
                            lang = lang,
                            onEdit = {
                                editingRoom = room
                                showAddDialog = true
                            },
                            onDelete = { roomToDelete = room }
                        )
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (showAddDialog) {
            RoomUpsertDialog(
                initial = editingRoom,
                lang = lang,
                onDismiss = { showAddDialog = false },
                onSave = { req ->
                    onSaveRoom(editingRoom?.id, req) {
                        showAddDialog = false
                    }
                }
            )
        }

        // Delete Dialog
        roomToDelete?.let { room ->
            ConfirmDialog(
                title = AppStrings.t("delete_room", lang),
                message = "${AppStrings.t("confirm_delete", lang)}: ${room.name} (${room.section})",
                confirmText = AppStrings.t("delete", lang),
                cancelText = AppStrings.t("cancel", lang),
                onConfirm = {
                    onDeleteRoom(room.id)
                    roomToDelete = null
                },
                onDismiss = { roomToDelete = null }
            )
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    lang: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PetrolBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = PetrolBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Text(
                            text = "${room.level} • Section: ${room.section}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${room.studentCount} / ${room.capacity} ${AppStrings.t("students", lang)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetrolBlue
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
fun RoomUpsertDialog(
    initial: Room?,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (RoomUpsertRequest) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var level by remember { mutableStateOf(initial?.level ?: "1AC") }
    var section by remember { mutableStateOf(initial?.section ?: "1AC-1") }
    var capacity by remember { mutableStateOf((initial?.capacity ?: 35).toString()) }

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
                    text = if (initial == null) AppStrings.t("add_room", lang) else AppStrings.t("edit_room", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.t("room_name", lang)) },
                    placeholder = { Text("مثال: Salle 01", color = Color(0xFF94A3B8)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text(AppStrings.t("level", lang)) },
                    placeholder = { Text("مثال: 1AC", color = Color(0xFF94A3B8)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text(AppStrings.t("section", lang)) },
                    placeholder = { Text("مثال: 1AC-1", color = Color(0xFF94A3B8)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text(AppStrings.t("capacity", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (name.isNotBlank()) {
                                onSave(
                                    RoomUpsertRequest(
                                        name = name.trim(),
                                        level = level.trim(),
                                        section = section.trim(),
                                        capacity = capacity.toIntOrNull() ?: 35
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
