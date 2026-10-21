package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
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
import com.example.data.model.GridCell
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividPink

@Composable
fun ProfileScreen(
    currentCell: GridCell?,
    discoveryRadiusMeters: Int,
    gpsEnabled: Boolean,
    onRadiusChange: (Int) -> Unit,
    onGpsToggle: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var privacyMode by remember { mutableStateOf("APPROXIMATE_BAND") }
    var allowDirectMessages by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize().background(PureBlack)) {
        // Oversized background typographic word
        Text(
            text = "TRUST",
            fontFamily = AntonFontFamily,
            fontSize = 140.sp,
            color = CharcoalBlack.copy(alpha = 0.5f),
            letterSpacing = 6.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp)
                .offset(x = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalBlack,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROFILE & TRUST",
                        fontFamily = AntonFontFamily,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        color = AcidYellow
                    )
                    Text(
                        text = "Community reputation & cryptographic coordinate privacy",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Neighbor Identity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AcidYellow)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AcidYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "YOU",
                                fontFamily = AntonFontFamily,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp,
                                color = PureBlack
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "VERIFIED RESIDENT",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp,
                                    color = EditorialWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Neighbor",
                                    tint = AcidYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "CELL: ${currentCell?.name?.uppercase() ?: "OAKWOOD GROVE"} (${currentCell?.code ?: "#H3-8826"})",
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                color = AcidYellow
                            )
                            Text(
                                text = "Member since October 2026 • Tier 1 Trust",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcidYellow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = AcidYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resident Pass", fontFamily = AntonFontFamily, fontSize = 12.sp, letterSpacing = 0.5.sp, color = AcidYellow)
                        }

                        Button(
                            onClick = onReplayOnboarding,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AcidYellow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tour Guide", fontFamily = AntonFontFamily, fontSize = 12.sp, letterSpacing = 0.5.sp, color = PureBlack)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Community Contribution Badges
            Text(
                text = "COMMUNITY CONTRIBUTION BADGES",
                fontFamily = AntonFontFamily,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = AcidYellow,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BadgeItem(
                        icon = Icons.Default.VolunteerActivism,
                        title = "HELPFUL NEIGHBOR (LEVEL 2)",
                        desc = "Completed 4 tool lendings & local assists",
                        color = ElectricLime
                    )
                    BadgeItem(
                        icon = Icons.Default.Shield,
                        title = "PROBLEM SOLVER",
                        desc = "Upvoted & verified 3 resolved neighborhood issues",
                        color = VividPink
                    )
                    BadgeItem(
                        icon = Icons.Default.People,
                        title = "COMMUNITY BUILDER",
                        desc = "Participated in local cricket & Pinecrest pavilion sessions",
                        color = BrightCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Location & Privacy Guarantees
            Text(
                text = "COORDINATE PRIVACY PROTOCOLS",
                fontFamily = AntonFontFamily,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = AcidYellow,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            colors = RadioButtonDefaults.colors(selectedColor = AcidYellow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DISTANCE BANDS (RECOMMENDED)",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = EditorialWhite
                            )
                            Text(
                                text = "Neighbors only see fuzzy proximity like \"within 350 m\". Your exact home coordinates are never stored or exposed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                            colors = RadioButtonDefaults.colors(selectedColor = AcidYellow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CELL ONLY (MAXIMUM PRIVACY)",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = EditorialWhite
                            )
                            Text(
                                text = "Only displays cell name (${currentCell?.name?.uppercase() ?: "OAKWOOD GROVE"}) with zero distance calculations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle direct messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ALLOW INTENT-MATCHED MESSAGES",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = EditorialWhite
                            )
                            Text(
                                text = "Neighbors can message you only after an exchange is accepted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }
                        Switch(
                            checked = allowDirectMessages,
                            onCheckedChange = { allowDirectMessages = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureBlack,
                                checkedTrackColor = AcidYellow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle GPS coordinate sensor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIVE GPS SENSOR ACCESS",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = EditorialWhite
                            )
                            Text(
                                text = if (gpsEnabled) "GPS active for live distance band calculations." else "Using discrete spatial mesh without device GPS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }
                        Switch(
                            checked = gpsEnabled,
                            onCheckedChange = { onGpsToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureBlack,
                                checkedTrackColor = AcidYellow
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
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
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PureBlack,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontFamily = AntonFontFamily,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = EditorialWhite
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray
            )
        }
    }
}
