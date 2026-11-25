package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PureWhite

@Composable
fun PostMediaCarousel(
    mediaUris: List<String>,
    mediaTypes: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (mediaUris.isEmpty()) return

    var selectedViewerIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (mediaUris.size == 1) {
            val uriString = mediaUris.first()
            val isVideo = mediaTypes.getOrNull(0) == "VIDEO" ||
                    uriString.contains("video", ignoreCase = true) ||
                    uriString.endsWith(".mp4", ignoreCase = true)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
                    .background(ElevatedGlassSurface)
                    .clickable { selectedViewerIndex = 0 }
                    .testTag("post_media_item_0")
            ) {
                AsyncImage(
                    model = uriString,
                    error = painterResource(id = R.drawable.img_neighborhood_connection),
                    contentDescription = "Media attachment 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = CircleShape,
                    color = ObsidianCanvas.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "FULL VIEW",
                        style = AuraKickerStyle,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PureWhite,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = ObsidianCanvas.copy(alpha = 0.86f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VIDEO",
                                    style = AuraKickerStyle,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(mediaUris) { index, uriString ->
                    val isVideo = mediaTypes.getOrNull(index) == "VIDEO" ||
                            uriString.contains("video", ignoreCase = true) ||
                            uriString.endsWith(".mp4", ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .width(260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ElevatedGlassSurface)
                            .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
                            .clickable { selectedViewerIndex = index }
                            .testTag("post_media_item_$index")
                    ) {
                        AsyncImage(
                            model = uriString,
                            error = painterResource(id = R.drawable.img_neighborhood_connection),
                            contentDescription = "Media attachment ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isVideo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PureWhite,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Video",
                                            tint = ObsidianBlack,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = ObsidianCanvas.copy(alpha = 0.86f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
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
                                            text = "VIDEO",
                                            style = AuraKickerStyle,
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
    }

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
                    .background(ObsidianCanvas.copy(alpha = 0.97f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElevatedGlassSurface,
                            border = BorderStroke(1.dp, HairlineGlassBorder)
                        ) {
                            Text(
                                text = "MEDIA ${selectedViewerIndex + 1} / ${mediaUris.size}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        CircularOutlineIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { selectedViewerIndex = -1 },
                            modifier = Modifier.testTag("media_viewer_close_button")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = viewerUri,
                            error = painterResource(id = R.drawable.img_neighborhood_connection),
                            contentDescription = "Full-screen media view",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isViewerVideo) {
                            Surface(
                                shape = CircleShape,
                                color = PureWhite,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Playing Video",
                                        tint = ObsidianBlack,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }

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
                                        .height(5.dp)
                                        .width(if (idx == selectedViewerIndex) 24.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == selectedViewerIndex) ElectricCobalt else Color.White.copy(alpha = 0.35f))
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
