package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppSettings
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PastelButterYellow
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelSoftCyan
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.util.StorageUsage

data class ColorThemeOption(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val accentColor: Color
)

val ACCENT_THEME_OPTIONS = listOf(
    ColorThemeOption("WARM_AMBER", "Warm Amber", Color(0xFFD84315), PastelPeach),
    ColorThemeOption("ELECTRIC_TEAL", "Electric Teal", Color(0xFF00838F), PastelSoftCyan),
    ColorThemeOption("BOTANICAL_SAGE", "Botanical Sage", Color(0xFF2E7D32), PastelMint),
    ColorThemeOption("SOLAR_CORAL", "Solar Coral", Color(0xFFC2185B), PastelCoral),
    ColorThemeOption("MIDNIGHT_INDIGO", "Midnight Indigo", DarkNavy, PastelButterYellow)
)

@Composable
fun AppSettingsScreen(
    appSettings: AppSettings,
    storageUsage: StorageUsage,
    supportTicketStatus: String?,
    onUpdateSettings: (AppSettings) -> Unit,
    onTrimCacheNow: () -> Unit,
    onSubmitSupportTicket: (category: String, message: String) -> Unit,
    onClearSupportStatus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showSupportDialog by remember { mutableStateOf(false) }
    var showLicenseModal by remember { mutableStateOf(false) }
    var showDiagnosticsModal by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftCream)
            .testTag("app_settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Bar
            Surface(
                color = PureWhite,
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoftCream)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings & Customization",
                            fontFamily = DMSerifFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = "App preferences, storage meter, support & version info",
                            fontFamily = DMSansFontFamily,
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Appearance & Accent Colors
                SettingsCardSection(
                    title = "VISUAL APPEARANCE & ACCENTS",
                    icon = Icons.Default.Palette
                ) {
                    Text(
                        text = "Primary Accent Theme",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ACCENT_THEME_OPTIONS.forEach { theme ->
                            val isSelected = appSettings.themeAccent == theme.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) theme.accentColor else PureWhite,
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) theme.primaryColor else WarmBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clickable {
                                        onUpdateSettings(appSettings.copy(themeAccent = theme.id))
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(theme.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.name.split(" ").first(),
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = PrimaryText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Layout Density",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("COMFORT" to "Comfortable Spacing", "COMPACT" to "Compact Grid Density").forEach { (key, label) ->
                            val isSelected = appSettings.layoutDensity == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PastelPeach.copy(alpha = 0.5f) else PureWhite,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryText else WarmBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onUpdateSettings(appSettings.copy(layoutDensity = key))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, if (isSelected) PrimaryText else SecondaryText, CircleShape)
                                            .background(if (isSelected) PrimaryText else Color.Transparent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = PrimaryText
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Mesh Sync & Haptic Preferences
                SettingsCardSection(
                    title = "PERFORMANCE & MESH NETWORK",
                    icon = Icons.Default.Speed
                ) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Tactile Feedback",
                        subtitle = "Vibrate on post creation, upvotes, and map clicks",
                        checked = appSettings.hapticEnabled,
                        onCheckedChange = { onUpdateSettings(appSettings.copy(hapticEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Background Mesh Refresh Frequency",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15 to "15m", 30 to "30m", 60 to "1h", 0 to "Manual").forEach { (min, label) ->
                            val isSelected = appSettings.backgroundSyncMinutes == min
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryText else PureWhite,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryText else WarmBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(appSettings.copy(backgroundSyncMinutes = min)) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) PureWhite else PrimaryText
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Memory Management & Storage Meter
                SettingsCardSection(
                    title = "MEMORY & STORAGE USAGE METER",
                    icon = Icons.Default.Memory
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "App Footprint: ${storageUsage.formattedTotal}",
                            fontFamily = DMSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelMint,
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = "Healthy",
                                fontFamily = DMSansFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Storage Bar Meter
                    LinearProgressIndicator(
                        progress = { storageUsage.imagePercentage.coerceAtLeast(0.05f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PrimaryText,
                        trackColor = WarmBorder
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breakdown items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StorageMeterBadge("Images & Tiles", storageUsage.formattedImageCache, PastelPeach)
                        StorageMeterBadge("Local Database", storageUsage.formattedDb, PastelSoftCyan)
                        StorageMeterBadge("Temp Files", storageUsage.formattedTemp, PastelButterYellow)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsSwitchRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Automatic Cache Trimming",
                        subtitle = "Auto-purges stale map tiles and cached media on launch",
                        checked = appSettings.autoTrimCacheEnabled,
                        onCheckedChange = { onUpdateSettings(appSettings.copy(autoTrimCacheEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onTrimCacheNow,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("trim_cache_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optimize Storage & Trim Cache Now",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = PrimaryText
                        )
                    }
                }

                // Section 4: Support & Privacy Center
                SettingsCardSection(
                    title = "SUPPORT, HELP & PRIVACY CENTER",
                    icon = Icons.Default.Shield
                ) {
                    FaqExpandableItem(
                        question = "How does local spatial privacy work?",
                        answer = "NearGrid uses localized geohashing. Your exact coordinates are never shared with third-party tracking servers. When you select 'Approximate' or 'Neighborhood Only', coordinates are fuzzed or anchored to the neighborhood cell centroid."
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FaqExpandableItem(
                        question = "What data is stored on my device vs cloud?",
                        answer = "Community posts and messages are synchronized via encrypted Firestore collections. Local preferences, blocked accounts, and cached map tiles stay private in your device's protected app sandbox."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSupportDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryText,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("open_support_ticket_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Submit Support Ticket",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Section 5: About App & Diagnostics
                SettingsCardSection(
                    title = "ABOUT NEARGRID",
                    icon = Icons.Default.Info
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NearGrid Mobile App",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = "Version ${appSettings.appVersion} • Build ${appSettings.buildId}",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelCoral.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = "Official Release",
                                fontFamily = DMSansFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDiagnosticsModal = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("view_diagnostics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System Diagnostics",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = PrimaryText
                            )
                        }

                        OutlinedButton(
                            onClick = { showLicenseModal = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("open_licenses_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Licenses",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = PrimaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Support Ticket Modal Dialog
        if (showSupportDialog) {
            SupportTicketDialog(
                statusMessage = supportTicketStatus,
                onDismiss = {
                    showSupportDialog = false
                    onClearSupportStatus()
                },
                onSubmit = { category, msg ->
                    onSubmitSupportTicket(category, msg)
                }
            )
        }

        // Diagnostics Modal
        if (showDiagnosticsModal) {
            DiagnosticsDialog(
                appVersion = appSettings.appVersion,
                buildId = appSettings.buildId,
                storageUsage = storageUsage,
                onDismiss = { showDiagnosticsModal = false }
            )
        }

        // License Modal
        if (showLicenseModal) {
            OpenSourceLicenseDialog(
                onDismiss = { showLicenseModal = false }
            )
        }
    }
}

@Composable
fun SettingsCardSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, WarmBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SoftCream,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DMSansFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = subtitle,
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                color = SecondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = PrimaryText
            )
        )
    }
}

@Composable
fun StorageMeterBadge(label: String, formattedSize: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontFamily = DMSansFontFamily,
                fontSize = 10.sp,
                color = SecondaryText
            )
            Text(
                text = formattedSize,
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
    }
}

@Composable
fun FaqExpandableItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftCream,
        border = BorderStroke(1.dp, WarmBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontFamily = DMSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
fun SupportTicketDialog(
    statusMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (category: String, message: String) -> Unit
) {
    var category by remember { mutableStateOf("Bug Report") }
    var message by remember { mutableStateOf("") }
    val categories = listOf("Bug Report", "Feature Suggestion", "Account & Privacy", "General Help")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, WarmBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("support_ticket_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Submit Support Ticket",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = SecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CATEGORY",
                    fontFamily = DMSansFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SoftCream else PureWhite,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryText else WarmBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = PrimaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { if (it.length <= 400) message = it },
                    label = { Text("Describe issue or request") },
                    placeholder = { Text("What happened or what feature would you like?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_message_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryText,
                        unfocusedBorderColor = WarmBorder,
                        focusedContainerColor = SoftCream,
                        unfocusedContainerColor = SoftCream
                    ),
                    maxLines = 4
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage,
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSubmit(category, message) },
                    enabled = message.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("send_support_ticket_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Ticket", fontFamily = DMSansFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DiagnosticsDialog(
    appVersion: String,
    buildId: String,
    storageUsage: StorageUsage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "System Diagnostics Report", fontFamily = DMSerifFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                Spacer(modifier = Modifier.height(12.dp))
                DiagnosticRow("App Version", appVersion)
                DiagnosticRow("Build ID", buildId)
                DiagnosticRow("OS Environment", "Android 14 (API 34)")
                DiagnosticRow("Memory Footprint", storageUsage.formattedTotal)
                DiagnosticRow("Telemetry Status", "Active & Silent")
                DiagnosticRow("Mesh State", "Connected")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Close Report")
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontFamily = DMSansFontFamily, fontSize = 12.sp, color = SecondaryText)
        Text(text = value, fontFamily = DMSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
    }
}

@Composable
fun OpenSourceLicenseDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, WarmBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Open Source Software Credits", fontFamily = DMSerifFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "NearGrid is built with standard open source Android Jetpack libraries:\n\n• Jetpack Compose & Material 3 (Apache 2.0)\n• Firebase Android SDK & Firestore (Apache 2.0)\n• osmdroid OpenStreetMap Engine (Apache 2.0)\n• Coil Image Loading Library (Apache 2.0)\n• Kotlin Coroutines & Flow (Apache 2.0)",
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Close")
                }
            }
        }
    }
}
