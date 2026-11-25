package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GridCell
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltBorder
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
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
fun GeohashQrInviteDialog(
    cell: GridCell,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qrPayload = QrCodePayloadHelper.buildPayload(
        code = cell.code,
        name = cell.name,
        lat = cell.centerLat,
        lng = cell.centerLng
    )
    val shareMessage = QrCodePayloadHelper.buildShareMessage(
        code = cell.code,
        name = cell.name,
        lat = cell.centerLat,
        lng = cell.centerLng
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("geohash_qr_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = DarkGlassCard,
            border = BorderStroke(1.dp, HairlineGlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CobaltSoft)
                                .border(1.dp, ElectricCobalt.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GRID PASS QR",
                                style = AuraKickerStyle,
                                color = CobaltGlow
                            )
                            Text(
                                text = "Grid Cell QR Code",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PureWhite,
                                modifier = Modifier.testTag("qr_dialog_title")
                            )
                        }
                    }

                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.testTag("qr_close_button")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = CircleShape,
                    color = ElevatedGlassSurface,
                    border = BorderStroke(1.dp, HairlineGlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cell.name,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = ObsidianInk
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CobaltSoft)
                                .border(1.dp, ElectricCobalt.copy(alpha = 0.6f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cell.code,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Crisp pure-white QR plate for reliable optical scanning on dark canvas
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(PureWhite)
                        .border(1.5.dp, CobaltBorder.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                        .padding(14.dp)
                ) {
                    QrCodeCanvas(
                        content = qrPayload,
                        modifier = Modifier
                            .size(200.dp)
                            .testTag("qr_canvas_view"),
                        darkColor = ObsidianBlack,
                        lightColor = PureWhite
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Neighbors can scan this offline pass with NearGrid to join ${cell.name} directly. No internet or external web domain required.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 12.sp,
                    color = MutedSlate,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillPrimaryButton(
                        text = "Share Invite",
                        icon = Icons.Default.Share,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join ${cell.name} on NearGrid")
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Grid Cell Invite"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_share_button")
                    )

                    GlassSecondaryPillButton(
                        text = "Copy Code",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("NearGrid Cell Code", cell.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Cell code #${cell.code} copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_copy_button")
                    )
                }
            }
        }
    }
}
