package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GridCell
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CapsuleChevronRow
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.CobaltHeroCard
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.PillPrimaryButton
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
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    onRadiusChange: (Int) -> Unit,
    onGpsToggle: () -> Unit,
    onReplayOnboarding: () -> Unit,
    userNickname: String = "Alex Rivera",
    userHandle: String = "@alex_oakwood",
    userBio: String = "",
    avatarUri: String? = null,
    avatarPresetIndex: Int = 0,
    incognitoGridPresence: Boolean = false,
    allowProximityMessaging: Boolean = true,
    hideExactDistance: Boolean = false,
    locationPrecision: String = "EXACT",
    blockedUserIds: List<String> = emptyList(),
    onToggleIncognito: () -> Unit = {},
    onToggleAllowDMs: () -> Unit = {},
    onToggleHideExactDistance: () -> Unit = {},
    onSetLocationPrecision: (String) -> Unit = {},
    onSaveProfile: (
        nickname: String,
        handle: String,
        bio: String,
        avatarUri: String?,
        avatarPresetIndex: Int,
        privacyRadiusMeters: Int
    ) -> Unit = { _, _, _, _, _, _ -> },
    onUnblockUser: (String) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenGridQrScreen: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onResetToFreshGrid: () -> Unit = {},
    userEmail: String? = null,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showResetSuccessBanner by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    AuraMeshBackground(
        variant = AuraVariant.MIXED,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Minimal Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resident Pass",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Profile",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = PureWhite
                    )
                }

                CircularOutlineIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    onClick = { showEditSheet = true },
                    modifier = Modifier.testTag("profile_edit_top_button")
                )
            }

            // Streamlined Cobalt Hero Identity Card (1-tap Edit Sheet & 1-tap QR Pass)
            CobaltHeroCard(
                eyebrowText = "$userHandle · ${currentCell?.name ?: "Local Sector"}",
                titleText = userNickname,
                subtitleText = userBio.ifBlank { "Verified Neighbor · ${discoveryRadiusMeters}m perimeter" },
                avatarInitials = userNickname.take(2).uppercase().ifBlank { "NG" },
                onAvatarClick = { showEditSheet = true },
                trailingIcon = Icons.Default.QrCodeScanner,
                trailingIconDescription = "Open Grid QR Pass",
                onTrailingIconClick = onOpenGridQrScreen,
                trailingIconTestTag = "profile_qr_quick_button",
                primaryCtaText = "Edit Profile",
                primaryCtaIcon = Icons.Default.Edit,
                onPrimaryCtaClick = { showEditSheet = true },
                primaryCtaTestTag = "profile_edit_full_button",
                extraContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.5.dp, PureWhite.copy(alpha = 0.45f), CircleShape)
                                .clickable { showEditSheet = true }
                                .testTag("profile_avatar_preview"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val currentPreset = PRESET_AVATARS.getOrNull(avatarPresetIndex) ?: PRESET_AVATARS[0]
                                Icon(
                                    imageVector = currentPreset.icon,
                                    contentDescription = currentPreset.name,
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Neighbor",
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Pass (${currentCell?.code ?: "#GPS"})",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            )

            // 1-Tap QR Pass Shortcut
            GlassSecondaryPillButton(
                text = "Grid QR Pass",
                icon = Icons.Default.QrCodeScanner,
                onClick = onOpenGridQrScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_share_grid_qr_card")
            )

            // 1-Tap Privacy & Radio Switches Card
            DarkGlassCard(cornerRadius = 24.dp) {
                Text(
                    text = "Quick Controls",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = CobaltGlow
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Privacy & Signal",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Map & Feed Location Precision",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(8.dp))

                val precisions = listOf(
                    "EXACT" to "Exact",
                    "APPROXIMATE" to "Fuzzy",
                    "NEIGHBORHOOD" to "Sector"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    precisions.forEach { (key, label) ->
                        val isSelected = locationPrecision == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCobalt else ElevatedGlassSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) CobaltGlow else HairlineGlassBorder,
                                    CircleShape
                                )
                                .clickable { onSetLocationPrecision(key) }
                                .padding(vertical = 9.dp)
                                .testTag("precision_pill_${key.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) PureWhite else MutedSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                Spacer(modifier = Modifier.height(10.dp))

                QuickSwitchRow(
                    title = "Live GPS",
                    subtitle = if (gpsEnabled) "Updating local distance bands" else "Using static sector cell",
                    checked = gpsEnabled,
                    activeColor = EmeraldMint,
                    onCheckedChange = { onGpsToggle() },
                    testTag = "profile_switch_gps"
                )

                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider, modifier = Modifier.padding(vertical = 8.dp))

                QuickSwitchRow(
                    title = "Hide Exact Distance",
                    subtitle = "Show coarse range bands instead of meters",
                    checked = hideExactDistance,
                    activeColor = ElectricCobalt,
                    onCheckedChange = { onToggleHideExactDistance() },
                    testTag = "profile_switch_hide_distance"
                )

                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider, modifier = Modifier.padding(vertical = 8.dp))

                QuickSwitchRow(
                    title = "Incognito Presence",
                    subtitle = "Hide your marker from the neighborhood map",
                    checked = incognitoGridPresence,
                    activeColor = FieryCoral,
                    onCheckedChange = { onToggleIncognito() },
                    testTag = "profile_switch_incognito"
                )

                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider, modifier = Modifier.padding(vertical = 8.dp))

                QuickSwitchRow(
                    title = "Allow Neighbor Messages",
                    subtitle = "Let nearby verified residents start chats",
                    checked = allowProximityMessaging,
                    activeColor = ElectricCobalt,
                    onCheckedChange = { onToggleAllowDMs() },
                    testTag = "profile_switch_allow_dms"
                )
            }

            // Clean Capsule Shortcuts
            CapsuleChevronRow(
                title = "App Settings & Storage",
                subtitle = "Notifications, cache & support",
                icon = Icons.Default.Tune,
                iconBackgroundColor = CobaltSoft,
                iconTint = CobaltGlow,
                onClick = onOpenSettings,
                modifier = Modifier.testTag("profile_open_settings_row")
            )

            CapsuleChevronRow(
                title = "Quick App Tour",
                subtitle = "Replay onboarding walkthrough",
                icon = Icons.Default.Refresh,
                iconBackgroundColor = ElevatedGlassSurface,
                iconTint = ObsidianInk,
                onClick = onReplayOnboarding
            )

            // Compact Account & Reset Card
            DarkGlassCard(cornerRadius = 24.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldSoft)
                            .border(1.dp, EmeraldMint.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Resident Session",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                        Text(
                            text = userEmail ?: "Signed in with Google",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = MutedSlate
                        )
                    }
                }

                if (showResetSuccessBanner) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldSoft,
                        border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Grid reset! Local cache cleared cleanly.",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = EmeraldLight,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassSecondaryPillButton(
                        text = "Reset Grid",
                        icon = Icons.Default.Refresh,
                        onClick = { showResetConfirmDialog = true },
                        height = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_fresh_grid_button")
                    )

                    GlassSecondaryPillButton(
                        text = "Sign Out",
                        icon = Icons.Default.Lock,
                        onClick = { showSignOutConfirmDialog = true },
                        surfaceTint = FieryCoralSoft,
                        contentColor = FieryCoralGlow,
                        borderColor = FieryCoral.copy(alpha = 0.45f),
                        height = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sign_out_button")
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .height(118.dp)
                    .navigationBarsPadding()
            )
        }
    }

    // Single Profile Edit Bottom Sheet
    if (showEditSheet) {
        ProfileEditSheet(
            currentNickname = userNickname,
            currentHandle = userHandle,
            currentBio = userBio,
            currentAvatarUri = avatarUri,
            currentPresetIndex = avatarPresetIndex,
            currentRadiusMeters = discoveryRadiusMeters,
            blockedUserIds = blockedUserIds,
            onUnblockUser = onUnblockUser,
            onSave = { nick, handle, bio, uri, preset, radius ->
                onSaveProfile(nick, handle, bio, uri, preset, radius)
                onRadiusChange(radius)
                showEditSheet = false
            },
            onDismiss = { showEditSheet = false }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Grid to Fresh State?", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = PureWhite)
            },
            text = {
                Text(
                    "This clears locally cached posts, issues, and chats, restoring NearGrid to a fresh state.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate
                )
            },
            confirmButton = {
                PillPrimaryButton(
                    text = "Reset Now",
                    useCoral = true,
                    onClick = {
                        showResetConfirmDialog = false
                        onResetToFreshGrid()
                        showResetSuccessBanner = true
                    },
                    modifier = Modifier.height(44.dp)
                )
            },
            dismissButton = {
                GlassSecondaryPillButton(
                    text = "Cancel",
                    onClick = { showResetConfirmDialog = false },
                    height = 44.dp
                )
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = {
                Text("Sign out of NearGrid?", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, color = PureWhite)
            },
            text = {
                Text(
                    "You will need to sign in with Google to access your neighborhood board and messages.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate
                )
            },
            confirmButton = {
                PillPrimaryButton(
                    text = "Sign Out",
                    useCoral = true,
                    onClick = {
                        showSignOutConfirmDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.height(44.dp)
                )
            },
            dismissButton = {
                GlassSecondaryPillButton(
                    text = "Cancel",
                    onClick = { showSignOutConfirmDialog = false },
                    height = 44.dp
                )
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun QuickSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    activeColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PureWhite
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = MutedSlate
            )
        }
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeColor = activeColor,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditSheet(
    currentNickname: String,
    currentHandle: String,
    currentBio: String,
    currentAvatarUri: String?,
    currentPresetIndex: Int,
    currentRadiusMeters: Int,
    blockedUserIds: List<String>,
    onUnblockUser: (String) -> Unit,
    onSave: (
        nickname: String,
        handle: String,
        bio: String,
        avatarUri: String?,
        avatarPresetIndex: Int,
        radiusMeters: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var nickname by remember(currentNickname) { mutableStateOf(currentNickname) }
    var handle by remember(currentHandle) { mutableStateOf(currentHandle) }
    var bio by remember(currentBio) { mutableStateOf(currentBio) }
    var avatarUri by remember(currentAvatarUri) { mutableStateOf(currentAvatarUri) }
    var presetIndex by remember(currentPresetIndex) { mutableIntStateOf(currentPresetIndex) }
    var radiusMeters by remember(currentRadiusMeters) { mutableIntStateOf(currentRadiusMeters) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (ignored: Exception) {
            }
            avatarUri = uri.toString()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianCanvas,
        contentColor = PureWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "IDENTITY & RANGE",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Edit Resident Profile",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PureWhite
                    )
                }
                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close Edit Sheet",
                    onClick = onDismiss,
                    size = 40.dp
                )
            }

            // Avatar Preview + Photo Picker + 6 Preset Avatars
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CobaltSoft)
                        .border(2.dp, ElectricCobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Selected avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val preset = PRESET_AVATARS.getOrNull(presetIndex) ?: PRESET_AVATARS[0]
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = preset.name,
                            tint = preset.accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassSecondaryPillButton(
                        text = "Choose Photo",
                        icon = Icons.Default.PhotoLibrary,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (avatarUri != null) {
                        Text(
                            text = "Use icon badge instead",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = CobaltGlow,
                            modifier = Modifier
                                .clickable { avatarUri = null }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(PRESET_AVATARS) { preset ->
                    val isSelected = avatarUri == null && presetIndex == preset.id
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(preset.backgroundColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ElectricCobalt else HairlineGlassBorder,
                                shape = CircleShape
                            )
                            .clickable {
                                avatarUri = null
                                presetIndex = preset.id
                            }
                            .testTag("edit_profile_preset_${preset.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = preset.name,
                            tint = preset.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCobalt,
                unfocusedBorderColor = HairlineGlassBorder,
                focusedContainerColor = ElevatedGlassSurface,
                unfocusedContainerColor = ElevatedGlassSurface,
                focusedTextColor = PureWhite,
                unfocusedTextColor = ObsidianInk,
                focusedLabelColor = CobaltGlow,
                unfocusedLabelColor = MutedSlate
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Display Name", fontFamily = PlusJakartaFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_nickname_input")
            )

            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Handle", fontFamily = PlusJakartaFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_handle_input")
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Short Bio", fontFamily = PlusJakartaFontFamily) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(18.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_bio_input")
            )

            // Discovery & Privacy Radius Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Neighborhood Discovery Range",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = PureWhite
                )
                val radii = listOf(300 to "300 m", 500 to "500 m", 800 to "800 m", 2000 to "2 km")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radii.forEach { (r, label) ->
                        val isSelected = radiusMeters == r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCobalt else ElevatedGlassSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) CobaltGlow else HairlineGlassBorder,
                                    CircleShape
                                )
                                .clickable { radiusMeters = r }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) PureWhite else MutedSlate
                            )
                        }
                    }
                }
            }

            if (blockedUserIds.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Blocked Neighbors (${blockedUserIds.size})",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FieryCoralGlow
                    )
                    blockedUserIds.forEach { uid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedGlassSurface)
                                .border(1.dp, HairlineGlassBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Neighbor ${uid.take(8)}",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = ObsidianInk
                            )
                            Text(
                                text = "Unblock",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CobaltGlow,
                                modifier = Modifier.clickable { onUnblockUser(uid) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            PillPrimaryButton(
                text = "Save Profile",
                icon = Icons.Default.Check,
                onClick = {
                    onSave(nickname, handle, bio, avatarUri, presetIndex, radiusMeters)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_save_button")
            )
        }
    }
}
