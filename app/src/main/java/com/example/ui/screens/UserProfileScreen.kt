package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GridCell
import com.example.data.update.AppUpdateInfo
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassBackButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.PillPrimaryButton
import com.example.ui.components.WhiteHeroPillButton
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltDeep
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
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate
import kotlinx.coroutines.launch

data class AvatarPreset(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val accentColor: Color
)

val PRESET_AVATARS = listOf(
    AvatarPreset(0, "Herb & Garden", Icons.Default.Spa, EmeraldSoft, EmeraldLight),
    AvatarPreset(1, "Workshop", Icons.Default.Build, AmberSoft, AmberLight),
    AvatarPreset(2, "Artisan", Icons.Default.Palette, FieryCoralSoft, FieryCoralGlow),
    AvatarPreset(3, "Bicycle Ranger", Icons.Default.DirectionsBike, CobaltSoft, CobaltGlow),
    AvatarPreset(4, "Book Nook", Icons.Default.MenuBook, ElevatedGlassSurface, ObsidianInk),
    AvatarPreset(5, "Cafe Neighbor", Icons.Default.LocalCafe, CobaltSoft, PureWhite)
)

val PRIVACY_RADIUS_OPTIONS = listOf(
    250 to ("250 m" to "Immediate Cell (Quiet block, ~1 street)"),
    500 to ("500 m" to "Walking District (~5 min walk, recommended)"),
    1000 to ("1.0 km" to "Neighborhood Sector (Local park & school)"),
    2500 to ("2.5 km" to "Wide Civil District (Multi-cell boundary)")
)

val NICKNAME_SUGGESTIONS = listOf(
    "Alex Rivera",
    "Alex (Oakwood)",
    "Gardener Alex",
    "Rivera #8826",
    "Oakwood_Maker",
    "Neighbor Alex"
)

@Composable
fun UserProfileScreen(
    currentNickname: String,
    currentHandle: String,
    currentBio: String,
    currentAvatarUri: String?,
    currentPresetIndex: Int,
    currentPrivacyRadius: Int,
    currentFuzzyCoords: Boolean,
    currentIncognito: Boolean,
    currentAllowDMs: Boolean,
    currentLocationPrecision: String = "EXACT",
    currentHideOnlineStatus: Boolean = false,
    currentHideExactDistance: Boolean = false,
    blockedUserIds: List<String> = emptyList(),
    onUnblockUser: (String) -> Unit = {},
    currentCell: GridCell?,
    onSaveProfile: (
        nickname: String,
        handle: String,
        bio: String,
        avatarUri: String?,
        avatarPresetIndex: Int,
        privacyRadiusMeters: Int,
        fuzzyCoords: Boolean,
        incognito: Boolean,
        allowDMs: Boolean,
        locationPrecision: String,
        hideOnlineStatus: Boolean,
        hideExactDistance: Boolean
    ) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    updateInfo: AppUpdateInfo? = null,
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var nickname by remember { mutableStateOf(currentNickname) }
    var handle by remember { mutableStateOf(currentHandle.removePrefix("@")) }
    var bio by remember { mutableStateOf(currentBio) }
    var avatarUri by remember { mutableStateOf(currentAvatarUri) }
    var presetIndex by remember { mutableIntStateOf(currentPresetIndex.coerceIn(0, PRESET_AVATARS.lastIndex)) }
    var selectedRadius by remember { mutableIntStateOf(currentPrivacyRadius) }
    var fuzzyCoords by remember { mutableStateOf(currentFuzzyCoords) }
    var incognito by remember { mutableStateOf(currentIncognito) }
    var allowDMs by remember { mutableStateOf(currentAllowDMs) }
    var locationPrecision by remember { mutableStateOf(currentLocationPrecision) }
    var hideOnlineStatus by remember { mutableStateOf(currentHideOnlineStatus) }
    var hideExactDistance by remember { mutableStateOf(currentHideExactDistance) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri.toString()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Display picture updated!")
            }
        }
    }

    fun performSave() {
        val cleanNick = nickname.trim().ifBlank { "Neighbor" }
        val cleanHandle = handle.trim().ifBlank { "resident" }
        onSaveProfile(
            cleanNick,
            cleanHandle,
            bio,
            avatarUri,
            presetIndex,
            selectedRadius,
            fuzzyCoords,
            incognito,
            allowDMs,
            locationPrecision,
            hideOnlineStatus,
            hideExactDistance
        )
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Profile & privacy settings saved!")
        }
    }

    AuraMeshBackground(
        variant = AuraVariant.MIXED,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LiquidGlassBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "RESIDENT DOSSIER",
                            style = AuraKickerStyle,
                            color = CobaltGlow
                        )
                        Text(
                            text = "Profile & Privacy",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = ObsidianInk
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularOutlineIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "App Settings",
                        onClick = onOpenSettings,
                        size = 44.dp,
                        modifier = Modifier.testTag("open_settings_button")
                    )

                    WhiteHeroPillButton(
                        text = "Save",
                        icon = Icons.Default.Check,
                        onClick = { performSave() },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("profile_top_save_button")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // SECTION 1: DISPLAY PICTURE / AVATAR
                DarkGlassCard(
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard,
                    borderColor = HairlineGlassBorder
                ) {
                    Text(
                        text = "01 // DISPLAY PORTRAIT",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Display Picture",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ObsidianInk
                    )
                    Text(
                        text = "Choose an avatar or upload a picture for your neighbor dossier",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.dp, CobaltGlow, CircleShape)
                                .background(ElevatedGlassSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "User display picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val currentPreset = PRESET_AVATARS[presetIndex]
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(currentPreset.backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = currentPreset.icon,
                                        contentDescription = currentPreset.name,
                                        tint = currentPreset.accentColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElectricCobalt)
                                .border(2.dp, DarkGlassCard, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("profile_change_photo_badge"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pick picture",
                                tint = PureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassSecondaryPillButton(
                            text = "Choose Photo",
                            icon = Icons.Default.PhotoLibrary,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_pick_photo_button")
                        )

                        if (avatarUri != null) {
                            OutlinedButton(
                                onClick = { avatarUri = null },
                                shape = CircleShape,
                                border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.55f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = FieryCoralSoft),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("profile_remove_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Photo",
                                    tint = FieryCoralGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FieryCoralGlow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Or choose a neighborhood avatar style:",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_AVATARS) { preset ->
                            val isSelected = avatarUri == null && presetIndex == preset.id
                            val animatedBorder by animateColorAsState(
                                targetValue = if (isSelected) ElectricCobalt else HairlineGlassBorder,
                                label = "presetBorder"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        avatarUri = null
                                        presetIndex = preset.id
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(preset.backgroundColor)
                                        .border(if (isSelected) 2.dp else 1.dp, animatedBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = preset.name,
                                        tint = preset.accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(ElectricCobalt),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.name.split(" ").first(),
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) CobaltGlow else MutedSlate,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // SECTION 2: NICKNAME & IDENTITY
                DarkGlassCard(
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard,
                    borderColor = HairlineGlassBorder
                ) {
                    Text(
                        text = "02 // RESIDENT IDENTITY",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Text(
                        text = "Resident Identity",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ObsidianInk
                    )
                    Text(
                        text = "Set your nickname and how neighbors recognize you in Cell ${currentCell?.name ?: "Oakwood Grove"}",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = HairlineGlassBorder,
                        focusedContainerColor = ElevatedGlassSurface,
                        unfocusedContainerColor = ElevatedGlassSurface,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = ObsidianInk,
                        errorBorderColor = FieryCoral,
                        errorContainerColor = ElevatedGlassSurface
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            if (it.length <= 32) nickname = it
                        },
                        label = { Text("Resident Nickname / Preferred Name", fontFamily = PlusJakartaFontFamily, color = MutedSlate) },
                        placeholder = { Text("e.g. Alex Rivera", fontFamily = PlusJakartaFontFamily, color = SubtleSlate) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CobaltGlow
                            )
                        },
                        trailingIcon = {
                            if (nickname.isNotEmpty()) {
                                IconButton(onClick = { nickname = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear nickname",
                                        tint = MutedSlate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (nickname.isBlank()) "Nickname cannot be blank" else "Visible on all posts & asks",
                                    color = if (nickname.isBlank()) FieryCoralGlow else MutedSlate,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${nickname.length}/32",
                                    color = MutedSlate,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                        },
                        isError = nickname.isBlank(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_nickname_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SUGGESTED ALIASES",
                        style = AuraKickerStyle,
                        color = SubtleSlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(NICKNAME_SUGGESTIONS) { suggestion ->
                            FilterChip(
                                selected = nickname == suggestion,
                                onClick = { nickname = suggestion },
                                label = {
                                    Text(
                                        text = suggestion,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 11.sp
                                    )
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = ElevatedGlassSurface,
                                    labelColor = ObsidianInk,
                                    selectedContainerColor = CobaltSoft,
                                    selectedLabelColor = CobaltGlow
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (nickname == suggestion) ElectricCobalt else HairlineGlassBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
                        label = { Text("Grid Tag / Handle", fontFamily = PlusJakartaFontFamily, color = MutedSlate) },
                        prefix = { Text("@", fontFamily = PlusJakartaFontFamily, fontWeight = FontWeight.Bold, color = CobaltGlow) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_handle_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 140) bio = it },
                        label = { Text("Neighbor Bio & Skills", fontFamily = PlusJakartaFontFamily, color = MutedSlate) },
                        placeholder = { Text("e.g. Garden enthusiast, have power tools to share...", fontFamily = PlusJakartaFontFamily, color = SubtleSlate) },
                        supportingText = {
                            Text(
                                text = "${bio.length}/140 · Helps neighbors find common interests",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(18.dp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_bio_input")
                    )
                }

                // SECTION 3: NEIGHBORHOOD PRIVACY RADIUS SETTINGS
                DarkGlassCard(
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard,
                    borderColor = HairlineGlassBorder
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldSoft)
                                .border(1.dp, EmeraldMint.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "03 // SPATIAL PERIMETER",
                                style = AuraKickerStyle,
                                color = EmeraldLight
                            )
                            Text(
                                text = "Neighborhood Privacy Radius",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ObsidianInk
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    RadiusRadarGraphic(
                        radiusMeters = selectedRadius,
                        cellName = currentCell?.name ?: "Oakwood Grove",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ObsidianCanvas)
                            .border(1.dp, HairlineGlassBorder, RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select active neighborhood boundary:",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRIVACY_RADIUS_OPTIONS.forEach { (meters, labels) ->
                            val (title, description) = labels
                            val isSelected = selectedRadius == meters

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedRadius = meters }
                                    .testTag("privacy_radius_option_$meters"),
                                color = if (isSelected) CobaltSoft else ElevatedGlassSurface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) ElectricCobalt else HairlineGlassBorder
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                if (isSelected) CobaltGlow else MutedSlate,
                                                CircleShape
                                            )
                                            .background(if (isSelected) ElectricCobalt else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(PureWhite)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = title,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = ObsidianInk
                                            )
                                            if (meters == 500) {
                                                Surface(
                                                    color = EmeraldSoft,
                                                    shape = CircleShape,
                                                    border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.45f))
                                                ) {
                                                    Text(
                                                        text = "RECOMMENDED",
                                                        fontFamily = PlusJakartaFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = EmeraldLight,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = description,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontSize = 11.5.sp,
                                            color = MutedSlate
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Map location precision:",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ObsidianInk
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val precisionOptions = listOf(
                        Triple("EXACT", "Exact Pin", "Live coordinate pinpoint on map"),
                        Triple("APPROXIMATE", "Approximate (~200m)", "Fuzzes coordinates to protect home doorstep"),
                        Triple("NEIGHBORHOOD", "Neighborhood Only", "Displays at area center without exact location")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        precisionOptions.forEach { (key, title, desc) ->
                            val isSelected = locationPrecision == key
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) CobaltSoft else ElevatedGlassSurface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) ElectricCobalt else HairlineGlassBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { locationPrecision = key }
                                    .testTag("precision_option_${key.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = ObsidianInk
                                        )
                                        Text(
                                            text = desc,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontSize = 11.sp,
                                            color = MutedSlate,
                                            lineHeight = 15.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ElectricCobalt,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "ACTIVITY & PRESENCE INDICATORS",
                        style = AuraKickerStyle,
                        color = SubtleSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ProfilePrivacySwitchRow(
                        title = "Hide Online Status Badge",
                        subtitle = "Conceals the live indicator. Neighbors see you as 'Active in neighborhood' without revealing real-time usage.",
                        checked = hideOnlineStatus,
                        onCheckedChange = { hideOnlineStatus = it },
                        testTag = "toggle_hide_online_status"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ProfilePrivacySwitchRow(
                        title = "Hide Exact Distance Meters",
                        subtitle = "Displays a broad proximity band (e.g. '< 500 m') instead of exact meters like '85 m away'.",
                        checked = hideExactDistance,
                        onCheckedChange = { hideExactDistance = it },
                        testTag = "toggle_hide_exact_distance"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ghost / Incognito Presence",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = ObsidianInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (incognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (incognito) FieryCoralGlow else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (incognito) "Completely hidden from map radar and neighbor lists." else "Your avatar appears as an active neighbor node inside your cell.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                color = MutedSlate,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        LiquidGlassSwitch(
                            checked = incognito,
                            onCheckedChange = { incognito = it },
                            activeColor = FieryCoral,
                            modifier = Modifier.testTag("toggle_incognito")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ProfilePrivacySwitchRow(
                        title = "Direct Intent Messaging",
                        subtitle = "Allow verified neighbors located inside your $selectedRadius m boundary to chat about offers & needs.",
                        checked = allowDMs,
                        onCheckedChange = { allowDMs = it },
                        testTag = "toggle_allow_dms"
                    )
                }

                // SECTION 4: BLOCKED NEIGHBORS & SAFETY
                DarkGlassCard(
                    modifier = Modifier.testTag("blocked_neighbors_card"),
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard,
                    borderColor = HairlineGlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FieryCoralSoft)
                                .border(1.dp, FieryCoral.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = FieryCoralGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "04 // SAFETY ROSTER",
                                style = AuraKickerStyle,
                                color = FieryCoralGlow
                            )
                            Text(
                                text = "Blocked Neighbors",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ObsidianInk
                            )
                            Text(
                                text = "${blockedUserIds.size} accounts blocked",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Blocked neighbors cannot view your location on the map, see your posts, or send you private messages.",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (blockedUserIds.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ElevatedGlassSurface,
                            border = BorderStroke(1.dp, HairlineGlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No neighbors currently blocked. You can block or report any neighbor directly from their profile on the map.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            blockedUserIds.forEach { blockedId ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ElevatedGlassSurface,
                                    border = BorderStroke(1.dp, HairlineGlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MutedSlate,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Neighbor #${blockedId.take(8)}",
                                                fontFamily = PlusJakartaFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = ObsidianInk
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { onUnblockUser(blockedId) },
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, HairlineGlassBorder),
                                            modifier = Modifier.testTag("unblock_button_$blockedId")
                                        ) {
                                            Text(
                                                text = "Unblock",
                                                fontFamily = PlusJakartaFontFamily,
                                                fontSize = 11.sp,
                                                color = ObsidianInk
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 5: APP RELEASES & UPDATES
                DarkGlassCard(
                    modifier = Modifier.testTag("app_version_card"),
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard,
                    borderColor = HairlineGlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CobaltSoft)
                                    .border(1.dp, CobaltBorder.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = CobaltGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Version & Updates",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ObsidianInk
                                )
                                Text(
                                    text = "BUILD V${updateInfo?.currentVersion ?: com.example.BuildConfig.VERSION_NAME}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }
                        }

                        if (updateInfo?.isUpdateAvailable == true) {
                            Surface(
                                shape = CircleShape,
                                color = FieryCoralSoft,
                                border = BorderStroke(1.dp, FieryCoral.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "V${updateInfo.latestVersion} AVAILABLE",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FieryCoralGlow,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldSoft,
                                border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "UP TO DATE",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (updateInfo?.isUpdateAvailable == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = ElevatedGlassSurface,
                            border = BorderStroke(1.dp, HairlineGlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = updateInfo.releaseName.ifBlank { "Release v${updateInfo.latestVersion}" },
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ObsidianInk
                                )
                                if (updateInfo.releaseNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = updateInfo.releaseNotes,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 11.5.sp,
                                        color = MutedSlate,
                                        lineHeight = 15.sp,
                                        maxLines = 3
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PillPrimaryButton(
                                        text = "Download APK",
                                        icon = Icons.Default.Download,
                                        onClick = {
                                            val url = updateInfo.apkDownloadUrl.ifBlank { updateInfo.releaseHtmlUrl }
                                            if (url.isNotBlank()) {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_download_update_button")
                                    )

                                    if (updateInfo.releaseHtmlUrl.isNotBlank()) {
                                        GlassSecondaryPillButton(
                                            text = "GitHub",
                                            icon = Icons.Default.OpenInNew,
                                            onClick = {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releaseHtmlUrl)))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (updateInfo?.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateInfo.errorMessage,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onCheckForUpdates,
                        enabled = updateInfo?.isChecking != true,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, HairlineGlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = ElevatedGlassSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_check_updates_button")
                    ) {
                        if (updateInfo?.isChecking == true) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CobaltGlow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checking for Releases...",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = ObsidianInk
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Check for Updates Now",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ObsidianInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Save & Cancel Capsule Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WhiteHeroPillButton(
                    text = "Save Profile & Privacy Settings",
                    icon = Icons.Default.Check,
                    onClick = { performSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_save_button")
                )

                GlassSecondaryPillButton(
                    text = "Cancel & Return",
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_cancel_button")
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun ProfilePrivacySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ObsidianInk
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 11.sp,
                color = MutedSlate,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeColor = ElectricCobalt,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun RadiusRadarGraphic(
    radiusMeters: Int,
    cellName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxDrawRadius = (size.height / 2f) * 0.85f

            val normalizedRadius = when (radiusMeters) {
                250 -> 0.35f
                500 -> 0.55f
                1000 -> 0.75f
                else -> 0.95f
            }

            val targetPixelRadius = maxDrawRadius * normalizedRadius

            drawCircle(
                color = HairlineGlassBorder,
                radius = maxDrawRadius,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            drawCircle(
                color = HairlineGlassBorder.copy(alpha = 0.5f),
                radius = maxDrawRadius * 0.6f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            drawCircle(
                color = ElectricCobalt.copy(alpha = (1f - pulseProgress) * 0.35f),
                radius = targetPixelRadius * pulseProgress,
                center = center
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCobalt.copy(alpha = 0.28f),
                        CobaltDeep.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = targetPixelRadius
                ),
                radius = targetPixelRadius,
                center = center
            )

            drawCircle(
                color = CobaltGlow,
                radius = targetPixelRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            val ambientBeaconOffsets = listOf(
                Offset(center.x - 35f, center.y - 25f),
                Offset(center.x + 40f, center.y + 30f),
                Offset(center.x - 65f, center.y + 45f),
                Offset(center.x + 85f, center.y - 50f),
                Offset(center.x - 110f, center.y - 15f),
                Offset(center.x + 120f, center.y + 40f)
            )

            ambientBeaconOffsets.forEach { node ->
                val distance = (node - center).getDistance()
                val isInside = distance <= targetPixelRadius
                drawCircle(
                    color = if (isInside) EmeraldMint else HairlineGlassBorder,
                    radius = if (isInside) 4.5.dp.toPx() else 3.dp.toPx(),
                    center = node
                )
            }

            drawCircle(
                color = ElectricCobalt,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = PureWhite,
                radius = 3.dp.toPx(),
                center = center
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            color = DarkGlassCard.copy(alpha = 0.92f),
            shape = CircleShape,
            border = BorderStroke(1.dp, HairlineGlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CobaltGlow)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$radiusMeters M PERIMETER · ${cellName.uppercase()}",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianInk
                )
            }
        }
    }
}
