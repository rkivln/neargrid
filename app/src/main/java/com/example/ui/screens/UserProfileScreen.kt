package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GridCell
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DMSerifFontFamily
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
import com.example.ui.theme.WarmDivider
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.launch

data class AvatarPreset(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val accentColor: Color
)

val PRESET_AVATARS = listOf(
    AvatarPreset(0, "Herb & Garden", Icons.Default.Spa, PastelMint, Color(0xFF2E7D32)),
    AvatarPreset(1, "Workshop", Icons.Default.Build, PastelPeach, Color(0xFFD84315)),
    AvatarPreset(2, "Artisan", Icons.Default.Palette, PastelCoral, Color(0xFFC2185B)),
    AvatarPreset(3, "Bicycle Ranger", Icons.Default.DirectionsBike, PastelSoftCyan, Color(0xFF00838F)),
    AvatarPreset(4, "Book Nook", Icons.Default.MenuBook, PastelButterYellow, Color(0xFFE65100)),
    AvatarPreset(5, "Cafe Neighbor", Icons.Default.LocalCafe, SoftCream, Color(0xFF5D4037))
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
        allowDMs: Boolean
    ) -> Unit,
    onBack: () -> Unit,
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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Android Photo Picker Launcher (zero-permission compliant)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SoftCream)
                                .border(1.dp, WarmBorder, CircleShape)
                                .testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = PrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "User Profile & Privacy",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 20.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Nickname, picture, and spatial perimeter",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Button(
                        onClick = {
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
                                allowDMs
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile & privacy settings saved!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryText,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("profile_top_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PureWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: DISPLAY PICTURE / AVATAR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Display Picture",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 18.sp,
                        color = PrimaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Choose an avatar or upload a picture for your neighbor card",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Avatar Preview with camera action badge
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.dp, PastelPeach, CircleShape)
                                .background(SoftCream),
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
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }
                        }

                        // Floating edit photo badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryText)
                                .border(2.dp, PureWhite, CircleShape)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Photo action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_pick_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Choose Photo",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = PrimaryText
                            )
                        }

                        if (avatarUri != null) {
                            OutlinedButton(
                                onClick = { avatarUri = null },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PastelCoral.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                                modifier = Modifier.testTag("profile_remove_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Photo",
                                    tint = PastelCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    color = PastelCoral
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Avatars Selector
                    Text(
                        text = "Or choose a neighborhood avatar style:",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = PrimaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_AVATARS) { preset ->
                            val isSelected = avatarUri == null && presetIndex == preset.id
                            val animatedBorder by animateColorAsState(
                                targetValue = if (isSelected) PrimaryText else WarmBorder,
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
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(preset.backgroundColor)
                                        .border(if (isSelected) 2.dp else 1.dp, animatedBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = preset.name,
                                        tint = preset.accentColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryText),
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
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) PrimaryText else SecondaryText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: NICKNAME & IDENTITY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resident Identity",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 18.sp,
                        color = PrimaryText
                    )
                    Text(
                        text = "Set your nickname and how neighbors recognize you in Cell ${currentCell?.name ?: "Oakwood Grove"}",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nickname Input
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            if (it.length <= 32) nickname = it
                        },
                        label = { Text("Resident Nickname / Preferred Name", fontFamily = DMSansFontFamily) },
                        placeholder = { Text("e.g. Alex Rivera", fontFamily = DMSansFontFamily) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PastelCoral
                            )
                        },
                        trailingIcon = {
                            if (nickname.isNotEmpty()) {
                                IconButton(onClick = { nickname = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear nickname",
                                        tint = SecondaryText,
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
                                    color = if (nickname.isBlank()) PastelCoral else SecondaryText,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${nickname.length}/32",
                                    color = SecondaryText,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        isError = nickname.isBlank(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryText,
                            unfocusedBorderColor = WarmBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = SoftCream
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_nickname_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Nickname Suggestions
                    Text(
                        text = "Suggested aliases:",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText
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
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 11.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SoftCream,
                                    labelColor = PrimaryText,
                                    selectedContainerColor = PastelPeach,
                                    selectedLabelColor = PrimaryText
                                ),
                                border = BorderStroke(1.dp, WarmBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Community Handle
                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
                        label = { Text("Grid Tag / Handle", fontFamily = DMSansFontFamily) },
                        prefix = { Text("@", fontFamily = DMSansFontFamily, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryText,
                            unfocusedBorderColor = WarmBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = SoftCream
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_handle_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Community Bio
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 140) bio = it },
                        label = { Text("Neighbor Bio & Skills", fontFamily = DMSansFontFamily) },
                        placeholder = { Text("e.g. Garden enthusiast, have power tools to share...", fontFamily = DMSansFontFamily) },
                        supportingText = {
                            Text(
                                text = "${bio.length}/140 • Helps neighbors find common interests",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryText,
                            unfocusedBorderColor = WarmBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = SoftCream
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_bio_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: NEIGHBORHOOD PRIVACY RADIUS SETTINGS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PastelMint.copy(alpha = 0.5f))
                                .border(1.dp, PastelMint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Neighborhood Privacy Radius",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 18.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Manage your broadcast zone & discoverability perimeter",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Radar Visualization Graphic
                    RadiusRadarGraphic(
                        radiusMeters = selectedRadius,
                        cellName = currentCell?.name ?: "Oakwood Grove",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftCream)
                            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select active neighborhood boundary:",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Radius Option Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRIVACY_RADIUS_OPTIONS.forEach { (meters, labels) ->
                            val (title, description) = labels
                            val isSelected = selectedRadius == meters

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedRadius = meters }
                                    .testTag("privacy_radius_option_$meters"),
                                color = if (isSelected) PastelPeach.copy(alpha = 0.35f) else SoftCream,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) PastelCoral else WarmBorder
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                if (isSelected) PastelCoral else SecondaryText,
                                                CircleShape
                                            )
                                            .background(if (isSelected) PastelCoral else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
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
                                                fontFamily = DMSansFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PrimaryText
                                            )
                                            if (meters == 500) {
                                                Surface(
                                                    color = PastelMint,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "RECOMMENDED",
                                                        fontFamily = DMSansFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = PrimaryText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = description,
                                            fontFamily = DMSansFontFamily,
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(WarmDivider)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Privacy Controls & Toggles
                    Text(
                        text = "Spatial coordinate safeguards:",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: Hex-Cell Centroid Fuzzing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hex-Cell Centroid Fuzzing",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Displaces coordinates by up to 180m toward the nearest cell vertex. Your exact home door is mathematically untraceable.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = fuzzyCoords,
                            onCheckedChange = { fuzzyCoords = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            ),
                            modifier = Modifier.testTag("toggle_fuzzy_coords")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle 2: Incognito Grid Presence
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Incognito Grid Presence",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (incognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (incognito) PastelCoral else SecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (incognito) "You are hidden from the active Living Grid presence map." else "Your avatar appears as an active neighbor node inside your cell.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = incognito,
                            onCheckedChange = { incognito = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            ),
                            modifier = Modifier.testTag("toggle_incognito")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle 3: Direct Proximity Messaging
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Direct Intent Messaging",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Allow verified neighbors located inside your $selectedRadius m boundary to chat about offers & needs.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = allowDMs,
                            onCheckedChange = { allowDMs = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            ),
                            modifier = Modifier.testTag("toggle_allow_dms")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
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
                            allowDMs
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profile & privacy radius successfully updated!")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_save_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Profile & Privacy Settings",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_cancel_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
                ) {
                    Text(
                        text = "Cancel & Return",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = PrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
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

            // Draw outer grid lines
            drawCircle(
                color = Color(0xFFD8D9DB),
                radius = maxDrawRadius,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Draw intermediate concentric rings
            drawCircle(
                color = Color(0xFFE2E3E5),
                radius = maxDrawRadius * 0.6f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw pulsing wave within target radius
            drawCircle(
                color = PastelCoral.copy(alpha = (1f - pulseProgress) * 0.3f),
                radius = targetPixelRadius * pulseProgress,
                center = center
            )

            // Draw active perimeter fill
            drawCircle(
                color = PastelPeach.copy(alpha = 0.35f),
                radius = targetPixelRadius,
                center = center
            )

            // Draw active perimeter boundary stroke
            drawCircle(
                color = PastelCoral,
                radius = targetPixelRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Ambient neighborhood beacons (dots in or out of perimeter)
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
                    color = if (isInside) PastelMint else Color(0xFFBDC3C7),
                    radius = if (isInside) 4.5.dp.toPx() else 3.dp.toPx(),
                    center = node
                )
            }

            // Center User Node
            drawCircle(
                color = PrimaryText,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = PureWhite,
                radius = 3.dp.toPx(),
                center = center
            )
        }

        // Overlay status label
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            color = PureWhite.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PastelCoral)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$radiusMeters m perimeter active in $cellName",
                    fontFamily = DMSansFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            }
        }
    }
}
