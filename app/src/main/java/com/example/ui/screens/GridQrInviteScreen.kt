package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DeepNavy
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
import com.example.util.QrCodeCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridQrInviteScreen(
    currentCell: GridCell?,
    currentGeohash: String,
    userNickname: String,
    onBackClick: () -> Unit,
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val cellName = currentCell?.name ?: "Local Neighborhood Grid"
    val cellCode = currentCell?.code.takeIf { !it.isNullOrBlank() }
        ?: if (currentGeohash.isNotBlank()) "#$currentGeohash" else "#GPS-LOCAL"
    val inviteUrl = "https://neargrid.app/join?cell=${cellCode.replace("#", "")}"

    val activeNeighbors = currentCell?.activeNeighborsCount ?: 12
    val openNeeds = currentCell?.openNeedsCount ?: 3
    val openOffers = currentCell?.openOffersCount ?: 5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Neighborhood Grid Pass",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 20.sp,
                        color = PrimaryText
                    )
                    Text(
                        text = "Invite neighbors to your local geohash grid",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("grid_qr_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.testTag("grid_qr_scan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR Code",
                        tint = PrimaryText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureWhite,
                titleContentColor = PrimaryText
            ),
            modifier = Modifier.border(0.dp, WarmBorder)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Printable Resident Badge Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grid_qr_badge_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.5.dp, WarmBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge Header Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PastelCoral.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = null,
                                    tint = PastelCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NearGrid Resident Pass",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelCoral
                                )
                                Text(
                                    text = userNickname.ifBlank { "Local Resident" },
                                    fontFamily = DMSerifFontFamily,
                                    fontSize = 16.sp,
                                    color = PrimaryText
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelMint.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, PastelMint.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = PrimaryText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Mesh",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cell Location Info
                    Text(
                        text = cellName,
                        fontFamily = DMSerifFontFamily,
                        fontSize = 24.sp,
                        color = PrimaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Geohash Grid Cell: $cellCode",
                            fontFamily = DMSansFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // High-Res QR Code Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .testTag("grid_qr_code_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeCanvas(
                            content = inviteUrl,
                            darkColor = DeepNavy,
                            lightColor = SoftCream
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan with NearGrid or camera app to instantly connect to this neighborhood grid",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(WarmDivider)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cell Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatBadge(
                            label = "Neighbors",
                            value = "$activeNeighbors",
                            bgColor = PastelSoftCyan.copy(alpha = 0.3f)
                        )
                        StatBadge(
                            label = "Open Needs",
                            value = "$openNeeds",
                            bgColor = PastelPeach.copy(alpha = 0.3f)
                        )
                        StatBadge(
                            label = "Offers",
                            value = "$openOffers",
                            bgColor = PastelButterYellow.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action Buttons
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Join $cellName on NearGrid!"
                        )
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Hey neighbor! Join our local neighborhood grid ($cellName, code: $cellCode) on NearGrid to share needs, offers, and local updates: $inviteUrl"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Grid Invitation"))
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepNavy,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("grid_qr_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Invite Link",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Share Invitation Link",
                    fontFamily = DMSansFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(inviteUrl))
                        Toast.makeText(context, "Grid invite code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("grid_qr_copy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = PrimaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Code",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }

                OutlinedButton(
                    onClick = onOpenScanner,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("grid_qr_scan_button_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = PrimaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan Code",
                        fontFamily = DMSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Explanation Guide
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftCream),
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "How Neighborhood Grid Invites Work",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 16.sp,
                        color = PrimaryText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionRow(
                        step = "1",
                        title = "Share Pass or Print Badge",
                        desc = "Show this QR code to a neighbor or paste the invite link in your building chat."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionRow(
                        step = "2",
                        title = "Instant Spatial Access",
                        desc = "Scanning instantly connects them to $cellName's local feed without sharing exact addresses."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionRow(
                        step = "3",
                        title = "Zero-Knowledge Privacy",
                        desc = "All member interactions stay localized to your geohash cell boundary."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    bgColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText
            )
            Text(
                text = label,
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun InstructionRow(
    step: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(PastelCoral),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontFamily = DMSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DMSansFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
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
