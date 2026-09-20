package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SchoolHeaderLogo
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.UiState

@Composable
fun SettingsScreen(
    state: UiState,
    onSaveSettings: (Map<String, Any?>) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val settings = state.siteSettings

    var siteNameAr by remember(settings) { mutableStateOf(settings.siteNameAr) }
    var siteNameFr by remember(settings) { mutableStateOf(settings.siteNameFr) }
    var brandTag by remember(settings) { mutableStateOf(settings.brandTag) }
    var primaryColor by remember(settings) { mutableStateOf(settings.primaryColor) }
    var secondaryColor by remember(settings) { mutableStateOf(settings.secondaryColor) }
    var footerTextAr by remember(settings) { mutableStateOf(settings.footerTextAr) }
    var footerTextFr by remember(settings) { mutableStateOf(settings.footerTextFr) }
    var serverUrl by remember(state.baseUrl) { mutableStateOf(state.baseUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo preview & branding card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SchoolHeaderLogo(
                    logoUrl = settings.logoUrl,
                    brandTag = brandTag,
                    siteName = if (lang == "ar") siteNameAr else siteNameFr,
                    size = 80
                )
            }
        }

        // General Info Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = AppStrings.t("settings", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )

                OutlinedTextField(
                    value = siteNameAr,
                    onValueChange = { siteNameAr = it },
                    label = { Text(AppStrings.t("site_name_ar", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = siteNameFr,
                    onValueChange = { siteNameFr = it },
                    label = { Text(AppStrings.t("site_name_fr", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = brandTag,
                    onValueChange = { brandTag = it },
                    label = { Text(AppStrings.t("brand_tag", lang)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = primaryColor,
                        onValueChange = { primaryColor = it },
                        label = { Text(AppStrings.t("primary_color", lang)) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                        colors = appTextFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = secondaryColor,
                        onValueChange = { secondaryColor = it },
                        label = { Text(AppStrings.t("secondary_color", lang)) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                        colors = appTextFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = footerTextAr,
                    onValueChange = { footerTextAr = it },
                    label = { Text("${AppStrings.t("footer_text", lang)} (Ar)") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = footerTextFr,
                    onValueChange = { footerTextFr = it },
                    label = { Text("${AppStrings.t("footer_text", lang)} (Fr)") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val payload = mapOf(
                            "site_name_ar" to siteNameAr,
                            "site_name_fr" to siteNameFr,
                            "brand_tag" to brandTag,
                            "primary_color" to primaryColor,
                            "secondary_color" to secondaryColor,
                            "footer_text_ar" to footerTextAr,
                            "footer_text_fr" to footerTextFr
                        )
                        onSaveSettings(payload)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(AppStrings.t("save", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Server URL Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = AppStrings.t("server_url", lang),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.SemiBold),
                    colors = appTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onUpdateServerUrl(serverUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = PetrolBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(AppStrings.t("save", lang))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
