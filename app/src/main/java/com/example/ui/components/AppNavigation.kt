package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.AppScreen

data class NavItem(
    val screen: AppScreen,
    val titleKey: String,
    val icon: ImageVector
)

val MainNavItems = listOf(
    NavItem(AppScreen.DASHBOARD, "dashboard", Icons.Default.Dashboard),
    NavItem(AppScreen.SCANNER, "scanner", Icons.Default.QrCodeScanner),
    NavItem(AppScreen.STUDENTS, "students", Icons.Default.People),
    NavItem(AppScreen.ROOMS, "rooms", Icons.Default.MeetingRoom),
    NavItem(AppScreen.REPORTS, "reports", Icons.Default.BarChart),
    NavItem(AppScreen.SETTINGS, "settings", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    currentLang: String,
    userName: String?,
    userRole: String?,
    onToggleLang: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = AppStrings.t(MainNavItems.find { it.screen == currentScreen }?.titleKey ?: "app_title", currentLang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (userName != null) {
                        Text(
                            text = "$userName (${userRole ?: "admin"})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        },
        actions = {
            // Language switch pill button
            Surface(
                onClick = onToggleLang,
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = if (currentLang == "ar") "Français" else "العربية",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Logout icon button
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PetrolBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    currentLang: String,
    userRole: String?,
    onSelectScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = if (userRole == "admin") {
        MainNavItems
    } else {
        MainNavItems.filter { it.screen != AppScreen.SETTINGS }
    }

    NavigationBar(
        containerColor = Color.White,
        contentColor = PetrolBlue,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = AppStrings.t(item.titleKey, currentLang),
                        tint = if (isSelected) BrandOrange else TextMuted
                    )
                },
                label = {
                    Text(
                        text = AppStrings.t(item.titleKey, currentLang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandOrange else TextMuted,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BrandOrange.copy(alpha = 0.12f)
                )
            )
        }
    }
}
