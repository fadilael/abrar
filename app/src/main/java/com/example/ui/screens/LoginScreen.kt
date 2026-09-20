package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SchoolHeaderLogo
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.UiState

@Composable
fun LoginScreen(
    state: UiState,
    onLogin: (String, String) -> Unit,
    onToggleLang: () -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("Admin@1234") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerConfig by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(state.baseUrl) }

    val focusManager = LocalFocusManager.current
    val lang = state.language

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language switch button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onToggleLang,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = PetrolBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == "ar") "Français" else "العربية",
                        color = PetrolBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // School Logo and Title
            SchoolHeaderLogo(
                logoUrl = state.siteSettings.logoUrl,
                brandTag = state.siteSettings.brandTag,
                siteName = if (lang == "ar") state.siteSettings.siteNameAr else state.siteSettings.siteNameFr,
                size = 84
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = AppStrings.t("login_desc", lang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = AppStrings.t("login", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetrolBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(AppStrings.t("username", lang)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PetrolBlue)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(AppStrings.t("password", lang)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PetrolBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide" else "Show",
                                    tint = TextMuted
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLogin(username, password)
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick credential helper chip
                    Surface(
                        onClick = {
                            username = "admin"
                            password = "Admin@1234"
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = PetrolBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PetrolBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("quick_login_hint", lang),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PetrolBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLogin(username, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = AppStrings.t("login", lang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Server configuration toggle
            TextButton(
                onClick = { showServerConfig = !showServerConfig }
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.t("server_url", lang),
                    color = TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            AnimatedVisibility(visible = showServerConfig) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("API Base URL") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                            colors = appTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUpdateServerUrl(serverUrlInput)
                                showServerConfig = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PetrolBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(AppStrings.t("save", lang))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer text
            Text(
                text = if (lang == "ar") state.siteSettings.footerTextAr else state.siteSettings.footerTextFr,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                textAlign = TextAlign.Center
            )
        }
    }
}
