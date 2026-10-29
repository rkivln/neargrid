package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PastelCoral
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarmBorder

@Composable
fun PostMediaCarousel(
    mediaUris: List<String>,
    mediaTypes: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (mediaUris.isEmpty()) return

    var selectedViewerIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(mediaUris) { index, uriString ->
                val isVideo = mediaTypes.getOrNull(index) == "VIDEO" ||
                        uriString.contains("video", ignoreCase = true) ||
                        uriString.endsWith(".mp4", ignoreCase = true)

                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .width(if (mediaUris.size == 1) 280.dp else 220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkNavy)
                        .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedViewerIndex = index }
                        .testTag("post_media_item_$index")
                ) {
                    AsyncImage(
                        model = uriString,
                        contentDescription = "Media attachment ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isVideo) {
                        // Play Icon Overlay Badge
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.9f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = PrimaryText,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Video Tag Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Video",
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Media Viewer Dialog
    if (selectedViewerIndex >= 0 && selectedViewerIndex < mediaUris.size) {
        val viewerUri = mediaUris[selectedViewerIndex]
        val isViewerVideo = mediaTypes.getOrNull(selectedViewerIndex) == "VIDEO" ||
                viewerUri.contains("video", ignoreCase = true) ||
                viewerUri.endsWith(".mp4", ignoreCase = true)

        Dialog(
            onDismissRequest = { selectedViewerIndex = -1 },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Bar with Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Media ${selectedViewerIndex + 1} of ${mediaUris.size}",
                            fontFamily = DMSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )

                        IconButton(
                            onClick = { selectedViewerIndex = -1 },
                            modifier = Modifier.testTag("media_viewer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Main Image/Video Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = viewerUri,
                            contentDescription = "Full-screen media view",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isViewerVideo) {
                            Surface(
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.9f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Playing Video",
                                        tint = DeepNavy,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Navigation if multiple
                    if (mediaUris.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            mediaUris.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (idx == selectedViewerIndex) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == selectedViewerIndex) PureWhite else Color.White.copy(alpha = 0.4f))
                                        .clickable { selectedViewerIndex = idx }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
