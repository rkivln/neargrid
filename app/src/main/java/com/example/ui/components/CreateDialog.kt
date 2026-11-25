package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import coil.compose.AsyncImage
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.FieryCoralSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIntentSheet(
    initialType: String,
    onDismiss: () -> Unit,
    onCreateNeed: (title: String, desc: String, category: String, urgency: String, expiryHours: Int, mediaUris: List<String>, mediaTypes: List<String>) -> Unit,
    onCreateOffer: (title: String, desc: String, category: String, expiryHours: Int, mediaUris: List<String>, mediaTypes: List<String>) -> Unit,
    onCreateIssue: (title: String, desc: String, location: String, category: String, mediaUris: List<String>, mediaTypes: List<String>) -> Unit,
    onCreateMeetup: (title: String, desc: String, category: String, hours: Int, mediaUris: List<String>, mediaTypes: List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember {
        mutableIntStateOf(
            when (initialType) {
                "OFFER" -> 1
                "ISSUE" -> 2
                "MEETUP" -> 3
                else -> 0
            }
        )
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationOrSpot by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TOOLS") }
    var selectedUrgency by remember { mutableStateOf("NORMAL") }
    var selectedExpiryHours by remember { mutableIntStateOf(24) }

    val mediaUris = remember { mutableStateListOf<String>() }
    val mediaTypes = remember { mutableStateListOf<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        uris.forEach { uri ->
            val uStr = uri.toString()
            if (!mediaUris.contains(uStr)) {
                mediaUris.add(uStr)
                val isVideo = uStr.contains("video", ignoreCase = true) || uStr.endsWith(".mp4", ignoreCase = true)
                mediaTypes.add(if (isVideo) "VIDEO" else "IMAGE")
            }
        }
    }

    val categories = listOf("TOOLS", "HOUSEHOLD", "HELP", "STUDY", "SPORTS")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW DISPATCH",
                        style = AuraKickerStyle,
                        color = CobaltGlow
                    )
                    Text(
                        text = "New neighborhood post",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Coordinate directly with nearby residents",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                }
                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_create_sheet")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
            Spacer(modifier = Modifier.height(14.dp))

            // Capsule Segmented Action Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = ElevatedGlassSurface,
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Need", "Offer", "Civic", "Meetup")
                    tabs.forEachIndexed { idx, label ->
                        val isSelected = selectedTab == idx
                        val activeBg = when (idx) {
                            0 -> FieryCoral
                            1 -> EmeraldMint
                            2 -> AmberGlow
                            else -> ElectricCobalt
                        }
                        val activeTextColor = if (idx == 2) ObsidianBlack else PureWhite
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) activeBg else Color.Transparent)
                                .clickable { selectedTab = idx }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) activeTextColor else MutedSlate
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy Guarantee Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = EmeraldSoft,
                border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Zero address leak: Only your neighborhood cell and fuzzy distance band (< 400m) will be shown.",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = ObsidianInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val darkFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCobalt,
                unfocusedBorderColor = HairlineGlassBorder,
                focusedTextColor = PureWhite,
                unfocusedTextColor = ObsidianInk,
                focusedContainerColor = ElevatedGlassSurface,
                unfocusedContainerColor = ElevatedGlassSurface,
                focusedLabelColor = CobaltGlow,
                unfocusedLabelColor = MutedSlate
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        when (selectedTab) {
                            0 -> "What do you need? (e.g. 16ft ladder, Calculus tutor)"
                            1 -> "What are you offering? (e.g. Power drill, Stroller)"
                            2 -> "What is the civic issue? (e.g. Streetlamp out)"
                            else -> "Meetup title (e.g. Saturday Cricket, Book swap)"
                        },
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 12.sp
                    )
                },
                colors = darkFieldColors,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details, availability or context", fontFamily = PlusJakartaFontFamily, fontSize = 12.sp) },
                colors = darkFieldColors,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_desc_input"),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 2) {
                OutlinedTextField(
                    value = locationOrSpot,
                    onValueChange = { locationOrSpot = it },
                    label = { Text("Approximate location (e.g. Elm St & 4th Cross)", fontFamily = PlusJakartaFontFamily, fontSize = 12.sp) },
                    colors = darkFieldColors,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_location_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "CATEGORY",
                style = AuraKickerStyle,
                color = MutedSlate
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(categories) { _, cat ->
                    val isSelected = selectedCategory == cat
                    val icon = getCategoryIcon(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) CobaltGlow else MutedSlate
                            )
                        },
                        label = {
                            Text(
                                cat.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltSoft,
                            selectedLabelColor = PureWhite,
                            containerColor = ElevatedGlassSurface,
                            labelColor = MutedSlate
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) ElectricCobalt else HairlineGlassBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = CircleShape,
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                Text(
                    text = "URGENCY LEVEL",
                    style = AuraKickerStyle,
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL" to "Normal", "TIME_SENSITIVE" to "Time-sensitive", "URGENT" to "Urgent").forEach { (urg, label) ->
                        val isSelected = selectedUrgency == urg
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedUrgency = urg },
                            label = {
                                Text(
                                    label,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FieryCoralSoft,
                                selectedLabelColor = FieryCoralGlow,
                                containerColor = ElevatedGlassSurface,
                                labelColor = MutedSlate
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) FieryCoral else HairlineGlassBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = CircleShape
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (selectedTab != 2) {
                Text(
                    text = "AUTO-EXPIRES AFTER",
                    style = AuraKickerStyle,
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6 to "6 hours", 24 to "24 hours", 72 to "3 days").forEach { (hours, label) ->
                        val isSelected = selectedExpiryHours == hours
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedExpiryHours = hours },
                            label = {
                                Text(
                                    label,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSoft,
                                selectedLabelColor = EmeraldLight,
                                containerColor = ElevatedGlassSurface,
                                labelColor = MutedSlate
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) EmeraldMint else HairlineGlassBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = CircleShape
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "PHOTOS & VIDEOS",
                style = AuraKickerStyle,
                color = MutedSlate
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassSecondaryPillButton(
                text = if (mediaUris.isEmpty()) "Attach Photos or Videos" else "Add More Media (${mediaUris.size}/5)",
                icon = Icons.Default.AddPhotoAlternate,
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_photos_videos_button")
            )

            if (mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(mediaUris) { index, uriStr ->
                        val isVideo = mediaTypes.getOrNull(index) == "VIDEO"
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedGlassSurface)
                                .border(1.dp, HairlineGlassBorder, RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = "Media preview $index",
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
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Video",
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianCanvas.copy(alpha = 0.85f))
                                    .clickable {
                                        mediaUris.removeAt(index)
                                        if (index < mediaTypes.size) {
                                            mediaTypes.removeAt(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove media",
                                    tint = PureWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkGlassCard)
                    .border(1.dp, HairlineGlassBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FieryCoral)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Live GPS coordinates will be attached for exact distance calculation to nearby neighbors.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = MutedSlate,
                    lineHeight = 16.sp
                )
            }

            WhiteHeroPillButton(
                text = when (selectedTab) {
                    0 -> "Post Need to Neighbors"
                    1 -> "Publish Resource Offer"
                    2 -> "Submit Civic Issue"
                    else -> "Publish Neighborhood Meetup"
                },
                enabled = title.isNotBlank(),
                onClick = {
                    if (title.isNotBlank()) {
                        val urisList = mediaUris.toList()
                        val typesList = mediaTypes.toList()
                        when (selectedTab) {
                            0 -> onCreateNeed(title, description, selectedCategory, selectedUrgency, selectedExpiryHours, urisList, typesList)
                            1 -> onCreateOffer(title, description, selectedCategory, selectedExpiryHours, urisList, typesList)
                            2 -> onCreateIssue(title, description, locationOrSpot.ifBlank { "Nearby street block" }, selectedCategory, urisList, typesList)
                            3 -> onCreateMeetup(title, description, selectedCategory, selectedExpiryHours, urisList, typesList)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_action_button")
            )
        }
    }
}
