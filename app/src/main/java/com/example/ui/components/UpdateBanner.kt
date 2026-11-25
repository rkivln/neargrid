package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.update.AppUpdateInfo
import com.example.data.update.AppUpdateManager
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite

/**
 * Dismissible Obsidian Cobalt Aura update card when a newer app release is available.
 */
@Composable
fun UpdateBanner(
    modifier: Modifier = Modifier,
    updateManager: AppUpdateManager = AppUpdateManager.instance,
    updateInfoOverride: AppUpdateInfo? = null,
    isDismissedOverride: Boolean? = null,
    onDismissOverride: (() -> Unit)? = null
) {
    val observedUpdateInfo by updateManager.updateState.collectAsStateWithLifecycle()
    val observedIsDismissed by updateManager.isBannerDismissed.collectAsStateWithLifecycle()

    val updateInfo = updateInfoOverride ?: observedUpdateInfo
    val isDismissed = isDismissedOverride ?: observedIsDismissed
    val onDismiss = onDismissOverride ?: { updateManager.dismissBanner() }

    val shouldShow = updateInfo?.isUpdateAvailable == true && !isDismissed
    val context = LocalContext.current

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        if (updateInfo != null) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("update_banner"),
                shape = RoundedCornerShape(22.dp),
                color = DarkGlassCard,
                border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CobaltSoft)
                                    .border(1.dp, ElectricCobalt.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = "New App Version",
                                    tint = CobaltGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "RELEASE UPDATE · V${updateInfo.latestVersion}",
                                    style = AuraKickerStyle,
                                    color = CobaltGlow
                                )
                                Text(
                                    text = "New Version Available: v${updateInfo.latestVersion}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Update now to get the latest features & improvements.",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    color = MutedSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        CircularOutlineIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Dismiss Banner",
                            onClick = onDismiss,
                            size = 36.dp,
                            modifier = Modifier.testTag("dismiss_update_banner_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PillPrimaryButton(
                            text = "Update Now (v${updateInfo.latestVersion})",
                            icon = Icons.Default.Download,
                            onClick = {
                                val targetUrl = updateInfo.apkDownloadUrl.ifBlank { updateInfo.releaseHtmlUrl }
                                if (targetUrl.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("update_now_button")
                        )

                        if (updateInfo.releaseHtmlUrl.isNotBlank()) {
                            Text(
                                text = "Release Notes",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CobaltGlow,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releaseHtmlUrl))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .testTag("update_release_notes_link")
                            )
                        }
                    }
                }
            }
        }
    }
}
