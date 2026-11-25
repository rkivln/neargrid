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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassBackButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.PillPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate
import com.example.util.StorageUsage

data class ColorThemeOption(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val accentColor: Color
)

val ACCENT_THEME_OPTIONS = listOf(
    ColorThemeOption("WARM_AMBER", "Aura Amber", AmberGlow, AmberSoft),
    ColorThemeOption("ELECTRIC_TEAL", "Electric Cobalt", ElectricCobalt, CobaltSoft),
    ColorThemeOption("BOTANICAL_SAGE", "Emerald Mesh", EmeraldMint, EmeraldSoft),
    ColorThemeOption("SOLAR_CORAL", "Fiery Coral", FieryCoral, FieryCoralSoft),
    ColorThemeOption("MIDNIGHT_INDIGO", "Obsidian Glow", CobaltGlow, CobaltSoft)
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

    AuraMeshBackground(
        variant = AuraVariant.MIXED,
        modifier = modifier
            .fillMaxSize()
            .testTag("app_settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassBackButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM PREFERENCES",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Settings & Customization",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        color = ObsidianInk
                    )
                    Text(
                        text = "Appearance, storage meter, support & diagnostics",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Appearance & Accent Colors
                SettingsCardSection(
                    title = "VISUAL APPEARANCE & ACCENTS",
                    icon = Icons.Default.Palette
                ) {
                    Text(
                        text = "Primary Accent Theme",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ACCENT_THEME_OPTIONS.forEach { theme ->
                            val isSelected = appSettings.themeAccent == theme.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) theme.accentColor else ElevatedGlassSurface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) theme.primaryColor else HairlineGlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(16.dp))
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
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = theme.name.split(" ").last(),
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = ObsidianInk
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Layout Density",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("COMFORT" to "Comfortable", "COMPACT" to "Compact Grid").forEach { (key, label) ->
                            val isSelected = appSettings.layoutDensity == key
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CobaltSoft else ElevatedGlassSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricCobalt else HairlineGlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable {
                                        onUpdateSettings(appSettings.copy(layoutDensity = key))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, if (isSelected) CobaltGlow else MutedSlate, CircleShape)
                                            .background(if (isSelected) ElectricCobalt else Color.Transparent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = ObsidianInk
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Background Mesh Refresh Frequency",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15 to "15M", 30 to "30M", 60 to "1H", 0 to "MANUAL").forEach { (min, label) ->
                            val isSelected = appSettings.backgroundSyncMinutes == min
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ElectricCobalt else ElevatedGlassSurface,
                                border = BorderStroke(1.dp, if (isSelected) CobaltGlow else HairlineGlassBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable { onUpdateSettings(appSettings.copy(backgroundSyncMinutes = min)) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 9.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else ObsidianInk
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
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianInk
                        )
                        Surface(
                            shape = CircleShape,
                            color = EmeraldSoft,
                            border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "HEALTHY",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = EmeraldLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { storageUsage.imagePercentage.coerceAtLeast(0.08f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = ElectricCobalt,
                        trackColor = ElevatedGlassSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StorageMeterBadge("Images & Tiles", storageUsage.formattedImageCache, CobaltSoft, CobaltGlow, Modifier.weight(1f))
                        StorageMeterBadge("Local DB", storageUsage.formattedDb, EmeraldSoft, EmeraldLight, Modifier.weight(1f))
                        StorageMeterBadge("Temp Files", storageUsage.formattedTemp, FieryCoralSoft, FieryCoralGlow, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsSwitchRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Automatic Cache Trimming",
                        subtitle = "Auto-purges stale map tiles and cached media on launch",
                        checked = appSettings.autoTrimCacheEnabled,
                        onCheckedChange = { onUpdateSettings(appSettings.copy(autoTrimCacheEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassSecondaryPillButton(
                        text = "Optimize Storage & Trim Cache Now",
                        icon = Icons.Default.Refresh,
                        onClick = onTrimCacheNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trim_cache_now_button")
                    )
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
                    Spacer(modifier = Modifier.height(8.dp))
                    FaqExpandableItem(
                        question = "What data is stored on my device vs cloud?",
                        answer = "Community posts and messages are synchronized via encrypted Firestore collections. Local preferences, blocked accounts, and cached map tiles stay private in your device's protected app sandbox."
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PillPrimaryButton(
                        text = "Submit Support Ticket",
                        icon = Icons.Default.SupportAgent,
                        onClick = { showSupportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_support_ticket_button")
                    )
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
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = ObsidianInk
                            )
                            Text(
                                text = "VERSION ${appSettings.appVersion} · BUILD ${appSettings.buildId}",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = AmberSoft,
                            border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "OFFICIAL",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = AmberLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassSecondaryPillButton(
                            text = "Diagnostics",
                            icon = Icons.Default.BugReport,
                            onClick = { showDiagnosticsModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_diagnostics_button")
                        )

                        GlassSecondaryPillButton(
                            text = "Open Licenses",
                            icon = Icons.Default.PrivacyTip,
                            onClick = { showLicenseModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_licenses_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

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

        if (showDiagnosticsModal) {
            DiagnosticsDialog(
                appVersion = appSettings.appVersion,
                buildId = appSettings.buildId,
                storageUsage = storageUsage,
                onDismiss = { showDiagnosticsModal = false }
            )
        }

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
    DarkGlassCard(
        cornerRadius = 24.dp,
        containerColor = DarkGlassCard,
        borderColor = HairlineGlassBorder,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(ElevatedGlassSurface)
                    .border(1.dp, HairlineGlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CobaltGlow,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = AuraKickerStyle,
                color = MutedSlate
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
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
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CobaltGlow,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ObsidianInk
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 11.5.sp,
                color = MutedSlate
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeColor = ElectricCobalt
        )
    }
}

@Composable
fun StorageMeterBadge(
    label: String,
    formattedSize: String,
    color: Color,
    valueColor: Color = PureWhite,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = PlusJakartaFontFamily,
                fontSize = 9.sp,
                color = MutedSlate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedSize,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun FaqExpandableItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ElevatedGlassSurface,
        border = BorderStroke(1.dp, HairlineGlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ObsidianInk,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CobaltGlow,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MutedSlate
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
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("support_ticket_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submit Support Ticket",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = ObsidianInk
                )
                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    size = 38.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CATEGORY",
                style = AuraKickerStyle,
                color = MutedSlate
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CobaltSoft else ElevatedGlassSurface,
                        border = BorderStroke(1.dp, if (isSelected) ElectricCobalt else HairlineGlassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { category = cat }
                    ) {
                        Text(
                            text = cat,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = ObsidianInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 400) message = it },
                label = { Text("Describe issue or request", fontFamily = PlusJakartaFontFamily, color = MutedSlate) },
                placeholder = { Text("What happened or what feature would you like?", fontFamily = PlusJakartaFontFamily, color = SubtleSlate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_message_input"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCobalt,
                    unfocusedBorderColor = HairlineGlassBorder,
                    focusedContainerColor = ElevatedGlassSurface,
                    unfocusedContainerColor = ElevatedGlassSurface,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = ObsidianInk
                ),
                maxLines = 4
            )

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = statusMessage,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PillPrimaryButton(
                text = "Submit Ticket",
                icon = Icons.Default.Send,
                enabled = message.isNotBlank(),
                onClick = { onSubmit(category, message) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_support_ticket_button")
            )
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
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "DIAGNOSTICS REPORT",
                style = AuraKickerStyle,
                color = CobaltGlow
            )
            Text(
                text = "System Diagnostics",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = ObsidianInk
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(10.dp))
            DiagnosticRow("App Version", appVersion)
            DiagnosticRow("Build ID", buildId)
            DiagnosticRow("OS Environment", "Android 14 (API 34)")
            DiagnosticRow("Memory Footprint", storageUsage.formattedTotal)
            DiagnosticRow("Telemetry Status", "Active & Silent")
            DiagnosticRow("Mesh State", "Connected")
            Spacer(modifier = Modifier.height(16.dp))
            PillPrimaryButton(
                text = "Close Report",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = PlusJakartaFontFamily, fontSize = 12.sp, color = MutedSlate)
        Text(text = value, fontFamily = PlusJakartaFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ObsidianInk)
    }
}

@Composable
fun OpenSourceLicenseDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        DarkGlassCard(
            cornerRadius = 28.dp,
            containerColor = DarkGlassCard,
            borderColor = HairlineGlassBorder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "OPEN SOURCE CREDITS",
                style = AuraKickerStyle,
                color = EmeraldLight
            )
            Text(
                text = "Open Source Software",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = ObsidianInk
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "NearGrid is built with open source Android libraries:\n\n• Jetpack Compose & Material 3 (Apache 2.0)\n• Kyant Backdrop Liquid Glass (Apache 2.0)\n• Firebase Android SDK & Firestore (Apache 2.0)\n• osmdroid OpenStreetMap Engine (Apache 2.0)\n• Coil Image Loading Library (Apache 2.0)\n• Kotlin Coroutines & Flow (Apache 2.0)",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = ObsidianInk
            )
            Spacer(modifier = Modifier.height(16.dp))
            PillPrimaryButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
