package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeohashMessage
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BlueGray
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightLime
import com.example.ui.theme.PrimarySurface
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SoftLimeSurface
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.SubtleDivider
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GeohashMessageComposer(
    currentGeohash: String,
    latitude: Double,
    longitude: Double,
    localityName: String,
    isLocating: Boolean = false,
    isPosting: Boolean = false,
    postStatus: String? = null,
    messages: List<GeohashMessage> = emptyList(),
    onRefreshLocation: () -> Unit = {},
    onPostMessage: (String) -> Unit,
    onDismissStatus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textMessage by remember { mutableStateOf("") }
    val maxChars = 280

    val quickTopics = remember {
        listOf(
            "📢 Announcement: ",
            "☕ Coffee meetup: ",
            "📦 Giving away: ",
            "ℹ️ Neighborhood note: ",
            "🐾 Sighting/Alert: "
        )
    }

    val displayGeohash = currentGeohash.ifBlank {
        if (latitude != 0.0 && longitude != 0.0) {
            LocationHelper.computeGeohash(latitude, longitude, 7)
        } else {
            "9q8yyk8"
        }
    }

    val coordinatesLabel = if (latitude != 0.0 && longitude != 0.0) {
        LocationHelper.formatCoordinates(latitude, longitude)
    } else {
        "Acquiring GPS fix..."
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geohash_message_composer"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimarySurface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with Geohash Badge and Live GPS Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftLimeSurface,
                            border = BorderStroke(1.dp, AccentLime),
                            modifier = Modifier.testTag("geohash_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tag,
                                    contentDescription = "Geohash Area Tag",
                                    tint = DeepNavy,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "#$displayGeohash",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Live GPS indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftSurface,
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isLocating) SlateBlue else AccentLime)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLocating) "Locating..." else "Live GPS",
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (localityName.isNotBlank()) "$localityName • $coordinatesLabel" else coordinatesLabel,
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Refresh Location button
                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("geohash_refresh_gps")
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = SlateBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh GPS Location",
                            tint = SlateBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Compose Geohash Broadcast",
                fontFamily = DMSerifFontFamily,
                fontSize = 18.sp,
                color = PrimaryText
            )

            Text(
                text = "Post an instant text message to neighbors in your active geohash cell with verified coordinates.",
                fontFamily = DMSansFontFamily,
                fontSize = 11.sp,
                color = SecondaryText,
                modifier = Modifier.padding(top = 1.dp, bottom = 8.dp)
            )

            // Quick Topic Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickTopics) { topic ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftSurface,
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier
                            .testTag("geohash_quick_tag")
                            .clickable {
                                textMessage = if (textMessage.isBlank()) topic else "$textMessage $topic"
                            }
                    ) {
                        Text(
                            text = topic.trimEnd(':').trimEnd(' '),
                            fontFamily = DMSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Text Message Composer Input
            OutlinedTextField(
                value = textMessage,
                onValueChange = {
                    if (it.length <= maxChars) {
                        textMessage = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("geohash_message_input"),
                placeholder = {
                    Text(
                        text = "Share an update or notice with neighbors in geohash #$displayGeohash...",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepNavy,
                    unfocusedBorderColor = CardBorderColor,
                    focusedContainerColor = SoftSurface,
                    unfocusedContainerColor = SoftSurface,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText
                )
            )

            // Character counter & metadata footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Pinned to coordinates",
                        tint = SlateBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tagged with lat, lon & timestamp",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }

                Text(
                    text = "${textMessage.length}/$maxChars",
                    fontFamily = DMSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (textMessage.length >= maxChars) Color(0xFFD32F2F) else SecondaryText
                )
            }

            // Status message banner (if any)
            AnimatedVisibility(
                visible = postStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (postStatus != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftLimeSurface,
                        border = BorderStroke(1.dp, AccentLime),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = DeepNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = postStatus,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DeepNavy,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = onDismissStatus,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = DeepNavy,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Post Button
            Button(
                onClick = {
                    if (textMessage.isNotBlank() && !isPosting) {
                        val messageToPost = textMessage
                        textMessage = ""
                        onPostMessage(messageToPost)
                    }
                },
                enabled = textMessage.isNotBlank() && !isPosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_geohash_message_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepNavy,
                    contentColor = PureWhite,
                    disabledContainerColor = DeepNavy.copy(alpha = 0.35f),
                    disabledContentColor = PureWhite.copy(alpha = 0.6f)
                )
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Posting to Geohash Area...",
                        fontFamily = DMSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Geohash Message",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Post to Geohash #$displayGeohash",
                        fontFamily = DMSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stream of messages posted to this geohash
            if (messages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Geohash Messages (${messages.size})",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 16.sp,
                        color = PrimaryText
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = "#$displayGeohash",
                            fontFamily = DMSansFontFamily,
                            fontSize = 10.sp,
                            color = SlateBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messages.take(4).forEach { msg ->
                        GeohashMessageItem(message = msg)
                    }
                }
            }
        }
    }
}

@Composable
fun GeohashMessageItem(
    message: GeohashMessage,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geohash_message_item"),
        shape = RoundedCornerShape(14.dp),
        color = if (message.isFromCurrentUser) SoftLimeSurface.copy(alpha = 0.5f) else SoftSurface,
        border = BorderStroke(1.dp, if (message.isFromCurrentUser) AccentLime else CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (message.isFromCurrentUser) DeepNavy else SlateBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.authorInitials.take(2),
                            color = PureWhite,
                            fontFamily = DMSansFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (message.isFromCurrentUser) "${message.authorName} (You)" else message.authorName,
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontFamily = DMSansFontFamily,
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                fontFamily = DMSansFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = PrimaryText
            )

            if (message.latitude != 0.0 && message.longitude != 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "GPS Coordinates",
                        tint = SlateBlue,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "GPS: %.4f, %.4f", message.latitude, message.longitude),
                        fontFamily = DMSansFontFamily,
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}
