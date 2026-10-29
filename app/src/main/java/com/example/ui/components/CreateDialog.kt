package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
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
import com.example.ui.theme.WarmIvory

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

    val typeAccent = when (selectedTab) {
        0 -> PastelCoral
        1 -> PastelMint
        2 -> PastelButterYellow
        else -> PastelSoftCyan
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "New neighborhood post",
                        fontFamily = DMSerifFontFamily,
                        fontSize = 24.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Coordinate directly with nearby residents",
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_sheet")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action type tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SoftCream,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PureWhite else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) WarmBorder else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = DMSansFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = PrimaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy guarantee card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PastelCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Zero address leak: Only your neighborhood cell and fuzzy distance band (< 400m) will be shown.",
                        fontFamily = DMSansFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title input
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
                        fontFamily = DMSansFontFamily,
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryText,
                    unfocusedBorderColor = WarmBorder,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details, availability or context", fontFamily = DMSansFontFamily, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryText,
                    unfocusedBorderColor = WarmBorder,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_desc_input"),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Issue specific: Location snippet
            if (selectedTab == 2) {
                OutlinedTextField(
                    value = locationOrSpot,
                    onValueChange = { locationOrSpot = it },
                    label = { Text("Approximate location (e.g. Elm St & 4th Cross)", fontFamily = DMSansFontFamily, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryText,
                        unfocusedBorderColor = WarmBorder,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_location_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Selector
            Text(
                text = "Category",
                fontFamily = DMSerifFontFamily,
                fontSize = 15.sp,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val icon = getCategoryIcon(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) PrimaryText else SecondaryText
                            )
                        },
                        label = {
                            Text(
                                cat.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = DMSansFontFamily,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelPeach,
                            selectedLabelColor = PrimaryText,
                            containerColor = PureWhite,
                            labelColor = SecondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) PastelCoral.copy(alpha = 0.5f) else WarmBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Need specific: Urgency
            if (selectedTab == 0) {
                Text(
                    text = "Urgency level",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 15.sp,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL" to "Normal", "TIME_SENSITIVE" to "Time-sensitive", "URGENT" to "Urgent").forEach { (urg, label) ->
                        val isSelected = selectedUrgency == urg
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedUrgency = urg },
                            label = {
                                Text(
                                    label,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelCoral.copy(alpha = 0.25f),
                                selectedLabelColor = PastelCoral,
                                containerColor = PureWhite,
                                labelColor = SecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) PastelCoral else WarmBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expiry hours
            if (selectedTab != 2) {
                Text(
                    text = "Auto-expires after",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 15.sp,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6 to "6 hours", 24 to "24 hours", 72 to "3 days").forEach { (hours, label) ->
                        val isSelected = selectedExpiryHours == hours
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedExpiryHours = hours },
                            label = {
                                Text(
                                    label,
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelMint.copy(alpha = 0.4f),
                                selectedLabelColor = PrimaryText,
                                containerColor = PureWhite,
                                labelColor = SecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) PastelMint else WarmBorder,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Photos & Videos Attachment Section
            Text(
                text = "Photos & Videos",
                fontFamily = DMSerifFontFamily,
                fontSize = 15.sp,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_photos_videos_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (mediaUris.isEmpty()) "Attach Photos or Videos" else "Add More Media (${mediaUris.size}/5)",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = PrimaryText
                )
            }

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
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftCream)
                                .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
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
                                        .background(Color.Black.copy(alpha = 0.3f)),
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

                            // Remove Button Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
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

            Spacer(modifier = Modifier.height(20.dp))

            // Real-Time GPS Notice Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhite)
                    .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PastelCoral)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Live GPS coordinates will be attached for exact distance calculation to nearby neighbors.",
                    fontFamily = DMSansFontFamily,
                    fontSize = 12.sp,
                    color = SecondaryText,
                    lineHeight = 16.sp
                )
            }

            // Submit Button
            Button(
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
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_action_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryText,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Post Need to Neighbors"
                        1 -> "Publish Resource Offer"
                        2 -> "Submit Civic Issue"
                        else -> "Publish Neighborhood Meetup"
                    },
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PureWhite
                )
            }
        }
    }
}
