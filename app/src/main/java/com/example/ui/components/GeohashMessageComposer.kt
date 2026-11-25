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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeohashMessage
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate
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
            "Announcement: ",
            "Coffee meetup: ",
            "Giving away: ",
            "Neighborhood note: ",
            "Sighting/Alert: "
        )
    }

    val hasGpsLock = latitude != 0.0 && longitude != 0.0
    val displayGeohash = currentGeohash.removePrefix("#").ifBlank {
        if (hasGpsLock) {
            LocationHelper.computeGeohash(latitude, longitude, 7)
        } else {
            "..."
        }
    }

    val coordinatesLabel = if (hasGpsLock) {
        LocationHelper.formatCoordinates(latitude, longitude)
    } else {
        "Acquiring GPS fix..."
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geohash_message_composer"),
        shape = RoundedCornerShape(24.dp),
        color = DarkGlassCard,
        border = BorderStroke(1.dp, HairlineGlassBorder)
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CobaltSoft,
                            border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.55f)),
                            modifier = Modifier.testTag("geohash_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tag,
                                    contentDescription = "Geohash Area Tag",
                                    tint = CobaltGlow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "#${displayGeohash.uppercase()}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (isLocating) AmberSoft else EmeraldSoft,
                            border = BorderStroke(
                                1.dp,
                                if (isLocating) AmberGlow.copy(alpha = 0.5f) else EmeraldMint.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isLocating) AmberGlow else EmeraldMint)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isLocating) "LOCATING" else "LIVE GPS",
                                    style = AuraKickerStyle,
                                    color = if (isLocating) AmberLight else EmeraldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (localityName.isNotBlank()) "$localityName · $coordinatesLabel" else coordinatesLabel,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElevatedGlassSurface)
                        .border(1.dp, HairlineGlassBorder, CircleShape)
                        .testTag("geohash_refresh_gps")
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = CobaltGlow
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh GPS Location",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Compose Geohash Broadcast",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = PureWhite
            )

            Text(
                text = "Post an instant dispatch to neighbors in your active geohash cell with verified coordinates.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = MutedSlate,
                modifier = Modifier.padding(top = 3.dp, bottom = 10.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickTopics) { topic ->
                    Surface(
                        shape = CircleShape,
                        color = ElevatedGlassSurface,
                        border = BorderStroke(1.dp, HairlineGlassBorder),
                        modifier = Modifier
                            .clip(CircleShape)
                            .testTag("geohash_quick_tag")
                            .clickable {
                                textMessage = if (textMessage.isBlank()) topic else "$textMessage $topic"
                            }
                    ) {
                        Text(
                            text = topic.trimEnd(' ').trimEnd(':'),
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ObsidianInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        text = "Share an update or notice in geohash #$displayGeohash...",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCobalt,
                    unfocusedBorderColor = HairlineGlassBorder,
                    focusedContainerColor = ElevatedGlassSurface,
                    unfocusedContainerColor = ElevatedGlassSurface,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = ObsidianInk
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Pinned to coordinates",
                        tint = EmeraldLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TAGGED LAT · LON · TIME",
                        style = AuraKickerStyle,
                        color = SubtleSlate
                    )
                }

                Text(
                    text = "${textMessage.length}/$maxChars",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (textMessage.length >= maxChars) FieryCoralGlow else MutedSlate
                )
            }

            AnimatedVisibility(
                visible = postStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (postStatus != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldSoft,
                        border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = postStatus,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ObsidianInk,
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
                                    tint = ObsidianInk,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (hasGpsLock && textMessage.isNotBlank() && !isPosting) {
                        val messageToPost = textMessage
                        textMessage = ""
                        onPostMessage(messageToPost)
                    }
                },
                enabled = hasGpsLock && textMessage.isNotBlank() && !isPosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_geohash_message_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCobalt,
                    contentColor = PureWhite,
                    disabledContainerColor = ElevatedGlassSurface,
                    disabledContentColor = MutedSlate
                )
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Posting to Geohash Area...",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!hasGpsLock) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "GPS Required",
                        modifier = Modifier.size(16.dp),
                        tint = MutedSlate
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acquiring GPS fix to broadcast...",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Geohash Message",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Post to Geohash #$displayGeohash",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (messages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SubtleGlassDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Geohash Messages (${messages.size})",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = PureWhite
                    )
                    Surface(
                        shape = CircleShape,
                        color = CobaltSoft,
                        border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "#${displayGeohash.uppercase()}",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = CobaltGlow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messages.take(4).forEach { msg ->
                        GeohashMessageItem(
                            message = msg,
                            userLatitude = latitude,
                            userLongitude = longitude
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeohashMessageItem(
    message: GeohashMessage,
    userLatitude: Double = 0.0,
    userLongitude: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val distanceLabel = remember(message.latitude, message.longitude, userLatitude, userLongitude) {
        if (userLatitude != 0.0 && userLongitude != 0.0 && message.latitude != 0.0 && message.longitude != 0.0) {
            val dist = LocationHelper.calculateDistanceMeters(userLatitude, userLongitude, message.latitude, message.longitude)
            LocationHelper.formatDistance(dist)
        } else {
            null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geohash_message_item"),
        shape = RoundedCornerShape(18.dp),
        color = if (message.isFromCurrentUser) CobaltSoft.copy(alpha = 0.55f) else ElevatedGlassSurface,
        border = BorderStroke(
            1.dp,
            if (message.isFromCurrentUser) ElectricCobalt.copy(alpha = 0.45f) else HairlineGlassBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (message.isFromCurrentUser) ElectricCobalt else EmeraldMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.authorInitials.take(2),
                            color = PureWhite,
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (message.isFromCurrentUser) "${message.authorName} (You)" else message.authorName,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (distanceLabel != null) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldSoft,
                            border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = distanceLabel,
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = timeFormatted,
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        color = SubtleSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = ObsidianInk
            )

            if (message.latitude != 0.0 && message.longitude != 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "GPS Coordinates",
                        tint = EmeraldLight,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "GPS %.4f, %.4f", message.latitude, message.longitude),
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        color = SubtleSlate
                    )
                }
            }
        }
    }
}
