package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GridCell
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    onRadiusChange: (Int) -> Unit,
    onGpsToggle: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    userNickname: String = "Alex Rivera",
    userHandle: String = "@alex_oakwood",
    userBio: String = "",
    avatarUri: String? = null,
    avatarPresetIndex: Int = 0,
    onEditProfile: () -> Unit = {},
    onResetToFreshGrid: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var privacyMode by remember { mutableStateOf("APPROXIMATE_BAND") }
    var allowDirectMessages by remember { mutableStateOf(true) }
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showResetSuccessBanner by remember { mutableStateOf(false) }

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
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profile & trust",
                            fontFamily = DMSerifFontFamily,
                            fontSize = 26.sp,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Community reputation and privacy-preserving coordinate settings",
                            fontFamily = DMSansFontFamily,
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }

                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                        modifier = Modifier.testTag("profile_edit_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PrimaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Edit",
                            fontFamily = DMSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neighbor Identity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(PastelPeach.copy(alpha = 0.5f))
                                .border(1.5.dp, PastelCoral.copy(alpha = 0.6f), CircleShape)
                                .clickable { onEditProfile() }
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
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userNickname,
                                    fontFamily = DMSerifFontFamily,
                                    fontSize = 20.sp,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Neighbor",
                                    tint = PastelMint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$userHandle • ${currentCell?.name ?: "Oakwood Grove"} (${currentCell?.code ?: "#H3-8826"})",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = PastelCoral
                            )
                            Text(
                                text = "Privacy Radius: $discoveryRadiusMeters m active • Tier 1 Neighborhood Trust",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    if (userBio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftCream,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“$userBio”",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = PrimaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action: Edit Profile & Privacy
                    Button(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryText,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_edit_full_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Nickname, Picture & Privacy Radius",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Resident Pass",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = PrimaryText
                            )
                        }

                        Button(
                            onClick = onReplayOnboarding,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftCream,
                                contentColor = PrimaryText
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = PrimaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Tour Guide",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = PrimaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Community Contribution Badges
            Text(
                text = "Community contribution badges",
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BadgeItem(
                        icon = Icons.Default.VolunteerActivism,
                        title = "Neighbor Member (Active)",
                        desc = "Verified resident in the neighborhood grid mesh",
                        color = PastelMint
                    )
                    BadgeItem(
                        icon = Icons.Default.Shield,
                        title = "Civic Guardian",
                        desc = "Ready to report and resolve local hazards",
                        color = PastelCoral
                    )
                    BadgeItem(
                        icon = Icons.Default.People,
                        title = "Mutual Aid Circle",
                        desc = "Lend tools, help neighbors, or share skills",
                        color = PastelSoftCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Coordinate Privacy Protocols
            Text(
                text = "Coordinate privacy protocols",
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Privacy Mode 1: Distance Bands
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacyMode == "APPROXIMATE_BAND",
                            onClick = { privacyMode = "APPROXIMATE_BAND" },
                            colors = RadioButtonDefaults.colors(selectedColor = PastelCoral)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Distance bands (Recommended)",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Neighbors only see fuzzy proximity like \"within 350 m\". Your exact home coordinates are never stored or exposed.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Privacy Mode 2: Cell Only
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacyMode == "CELL_ONLY",
                            onClick = { privacyMode = "CELL_ONLY" },
                            colors = RadioButtonDefaults.colors(selectedColor = PastelCoral)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cell only (Maximum privacy)",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Only displays cell name (${currentCell?.name ?: "Oakwood Grove"}) with zero distance calculations.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle direct messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow intent-matched messages",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = "Neighbors can message you only after an exchange is accepted.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                        Switch(
                            checked = allowDirectMessages,
                            onCheckedChange = { allowDirectMessages = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle GPS coordinate sensor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live GPS sensor access",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = if (gpsEnabled) "GPS active for live distance band calculations." else "Using discrete spatial mesh without device GPS.",
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                        Switch(
                            checked = gpsEnabled,
                            onCheckedChange = { onGpsToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = PastelCoral
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 9: FAQ, Help, and Contact Screen ("Magic Canvas" reference)
            Text(
                text = "Help & Community FAQ",
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Hand-drawn artwork with pastel brush strokes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_community_gathering),
                            contentDescription = "Community help illustration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Questions & neighborhood answers",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 20.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Everything you need to know about NearGrid coordinates, privacy, and community safety.",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expandable FAQ items
                    val faqs = listOf(
                        "How does NearGrid protect my exact address?" to "NearGrid divides geography into discrete H3 cells and shows only fuzzy distance bands like 'within 350m'. Your actual home address is never pinned or visible on any map.",
                        "What are verified safe public exchange spots?" to "These are pre-screened municipal public locations such as the Oakwood Pavilion with 24/7 lighting and security where neighbors can comfortably meet to hand off items.",
                        "How does the autonomous intent matching engine work?" to "The engine correlates needs (e.g. borrowing a ladder) with neighbor offers within your selected radius and alerts both parties without broadcast spam."
                    )

                    faqs.forEachIndexed { idx, (question, answer) ->
                        val isExpanded = expandedFaqIndex == idx
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isExpanded) SoftCream else Color.Transparent)
                                .clickable { expandedFaqIndex = if (isExpanded) -1 else idx }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = question,
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = PrimaryText,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = answer,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                        if (idx < faqs.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(WarmDivider)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Simple high-contrast action button
                    Button(
                        onClick = { /* Contact community council support */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryText,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contact Community Council",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PureWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data & Fresh State Reset Card
            Text(
                text = "Neighborhood mesh storage",
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Clean state & fresh grid",
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reset all locally stored needs, offers, issues, and private chats to start completely fresh without any lingering test entries.",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (showResetSuccessBanner) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PastelMint.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "Grid reset! All data is fresh and clean.",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = PrimaryText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCoral.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                        modifier = Modifier.fillMaxWidth().testTag("reset_fresh_grid_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PastelCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Grid to Fresh State",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PastelCoral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showResetConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = {
                    Text("Reset Grid to Fresh State?", fontFamily = DMSerifFontFamily, color = PrimaryText)
                },
                text = {
                    Text(
                        "This will clear all locally created posts, issues, and private chats, restoring NearGrid to its fresh default state.",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            onResetToFreshGrid()
                            showResetSuccessBanner = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelCoral)
                    ) {
                        Text("Reset Now", fontFamily = DMSansFontFamily, color = PureWhite)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel", fontFamily = DMSansFontFamily, color = PrimaryText)
                    }
                },
                containerColor = PureWhite,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun BadgeItem(
    icon: ImageVector,
    title: String,
    desc: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.5f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryText,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PrimaryText
            )
            Text(
                text = desc,
                fontFamily = DMSansFontFamily,
                fontSize = 12.sp,
                color = SecondaryText
            )
        }
    }
}
