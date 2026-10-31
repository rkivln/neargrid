package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory
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
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("geohash_qr_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Button
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
                                .background(PastelPeach),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Grid Cell QR Code",
                                fontFamily = DMSerifFontFamily,
                                fontSize = 20.sp,
                                color = PrimaryText,
                                modifier = Modifier.testTag("qr_dialog_title")
                            )
                            Text(
                                text = "Invite neighbors to join",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("qr_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PrimaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Geohash Code Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftCream,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cell.name,
                            fontFamily = DMSerifFontFamily,
                            fontSize = 14.sp,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DeepNavy)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = cell.code,
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // QR Code Render Canvas
                QrCodeCanvas(
                    content = qrPayload,
                    modifier = Modifier
                        .size(220.dp)
                        .testTag("qr_canvas_view"),
                    darkColor = DeepNavy,
                    lightColor = WarmIvory
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Neighbors can scan this offline pass with NearGrid to join ${cell.name} directly. No internet or external web domain required.",
                    fontFamily = DMSansFontFamily,
                    fontSize = 12.sp,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons: Share & Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join ${cell.name} on NearGrid")
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Grid Cell Invite"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryText,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("qr_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Invite",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("NearGrid Cell Code", cell.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Cell code #${cell.code} copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("qr_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = PrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Code",
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PrimaryText
                        )
                    }
                }
            }
        }
    }
}
