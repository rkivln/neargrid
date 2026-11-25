package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.ui.components.AuraMeshBackground
import com.example.ui.components.AuraVariant
import com.example.ui.components.CircularOutlineIconButton
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.GlassSecondaryPillButton
import com.example.ui.components.LiquidGlassBackButton
import com.example.ui.components.WhiteHeroPillButton
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltHeroGradient
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.util.QrCodeCanvas
import com.example.util.QrCodePayloadHelper

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
    val cellLat = currentCell?.centerLat ?: 0.0
    val cellLng = currentCell?.centerLng ?: 0.0

    val qrPayload = QrCodePayloadHelper.buildPayload(
        code = cellCode,
        name = cellName,
        lat = cellLat,
        lng = cellLng
    )
    val shareMessage = QrCodePayloadHelper.buildShareMessage(
        code = cellCode,
        name = cellName,
        lat = cellLat,
        lng = cellLng
    )

    val activeNeighbors = currentCell?.activeNeighborsCount ?: 12
    val openNeeds = currentCell?.openNeedsCount ?: 3
    val openOffers = currentCell?.openOffersCount ?: 5

    AuraMeshBackground(
        variant = AuraVariant.COBALT,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Minimalist Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LiquidGlassBackButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("grid_qr_back_button")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INVITE TO GEOHASH CELL",
                            style = AuraKickerStyle,
                            color = CobaltGlow
                        )
                        Text(
                            text = "Neighborhood Grid Pass",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = PureWhite
                        )
                    }
                }

                CircularOutlineIconButton(
                    icon = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    onClick = onOpenScanner,
                    size = 44.dp,
                    modifier = Modifier.testTag("grid_qr_scan_button")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Dark Glass Resident Credential Pass Card
                DarkGlassCard(
                    modifier = Modifier.testTag("grid_qr_badge_card"),
                    cornerRadius = 28.dp,
                    containerColor = DarkGlassCard.copy(alpha = 0.94f),
                    borderColor = HairlineGlassBorder,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CobaltHeroGradient)
                                    .border(1.dp, CobaltGlow.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RESIDENT CREDENTIAL",
                                    style = AuraKickerStyle,
                                    color = CobaltGlow
                                )
                                Text(
                                    text = userNickname.ifBlank { "Local Resident" },
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ObsidianInk
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = EmeraldSoft,
                            border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VERIFIED MESH",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = cellName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CobaltGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GEOHASH CELL ${cellCode.uppercase()}",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Crisp high-contrast QR backing plate so any camera scans reliably
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.76f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(PureWhite)
                            .padding(14.dp)
                            .testTag("grid_qr_code_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeCanvas(
                            content = qrPayload,
                            darkColor = ObsidianBlack,
                            lightColor = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan with NearGrid in-app scanner to instantly connect to this neighborhood grid (No internet or domain required).",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadge(
                            label = "NEIGHBORS",
                            value = "$activeNeighbors",
                            bgColor = EmeraldSoft,
                            valueColor = EmeraldLight,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            label = "OPEN NEEDS",
                            value = "$openNeeds",
                            bgColor = FieryCoralSoft,
                            valueColor = FieryCoralGlow,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            label = "OFFERS",
                            value = "$openOffers",
                            bgColor = AmberSoft,
                            valueColor = AmberLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                WhiteHeroPillButton(
                    text = "Share Grid Pass",
                    icon = Icons.Default.Share,
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Join $cellName on NearGrid!")
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Grid Invitation"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grid_qr_share_button")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassSecondaryPillButton(
                        text = "Copy Pass",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(qrPayload))
                            Toast.makeText(context, "Offline grid pass copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("grid_qr_copy_button")
                    )

                    GlassSecondaryPillButton(
                        text = "Scan Code",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("grid_qr_scan_button_action")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DarkGlassCard(
                    cornerRadius = 24.dp,
                    containerColor = DarkGlassCard.copy(alpha = 0.92f),
                    borderColor = HairlineGlassBorder
                ) {
                    Text(
                        text = "DISPATCH PROTOCOL",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "How Neighborhood Grid Invites Work",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ObsidianInk
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    InstructionRow(
                        step = "01",
                        title = "Share Pass or Print Badge",
                        desc = "Show this QR code to a neighbor or paste the invite link in your building chat."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionRow(
                        step = "02",
                        title = "Instant Spatial Access",
                        desc = "Scanning instantly connects them to $cellName's local feed without sharing exact addresses."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionRow(
                        step = "03",
                        title = "Zero-Knowledge Privacy",
                        desc = "All member interactions stay localized to your geohash cell boundary."
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    bgColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, HairlineGlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = valueColor
            )
            Text(
                text = label,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = ObsidianInk
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
                .size(28.dp)
                .clip(CircleShape)
                .background(CobaltSoft)
                .border(1.dp, CobaltGlow.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CobaltGlow
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ObsidianInk
            )
            Text(
                text = desc,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MutedSlate
            )
        }
    }
}
