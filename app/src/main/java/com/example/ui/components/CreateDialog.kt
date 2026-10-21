package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIntentSheet(
    initialType: String,
    onDismiss: () -> Unit,
    onCreateNeed: (title: String, desc: String, category: String, urgency: String, expiryHours: Int) -> Unit,
    onCreateOffer: (title: String, desc: String, category: String, expiryHours: Int) -> Unit,
    onCreateIssue: (title: String, desc: String, location: String, category: String) -> Unit,
    onCreateMeetup: (title: String, desc: String, category: String, hours: Int) -> Unit
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

    val categories = listOf("TOOLS", "HOUSEHOLD", "HELP", "STUDY", "SPORTS")

    val activeColor = when (selectedTab) {
        0 -> VividPink
        1 -> ElectricLime
        2 -> AcidYellow
        else -> BrightCyan
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "POST ACTION",
                    fontFamily = AntonFontFamily,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    color = activeColor
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_sheet")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = EditorialWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs for action types
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = activeColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "NEED",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = if (selectedTab == 0) VividPink else NeutralGray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "OFFER",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = if (selectedTab == 1) ElectricLime else NeutralGray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "CIVIC",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = if (selectedTab == 2) AcidYellow else NeutralGray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            "MEETUP",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = if (selectedTab == 3) BrightCyan else NeutralGray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy guarantee card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PureBlack,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZERO ADDRESS LEAK: Only your neighborhood cell and fuzzy distance band (< 400m) will be shown.",
                        fontFamily = AntonFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = EditorialWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        when (selectedTab) {
                            0 -> "What do you need? (e.g. 16ft ladder, Calculus tutor)"
                            1 -> "What are you offering? (e.g. DeWalt drill, Baby stroller)"
                            2 -> "What is the problem? (e.g. Broken streetlamp, Pothole)"
                            else -> "Activity title (e.g. Saturday Cricket, Walking group)"
                        },
                        color = NeutralGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = EditorialWhite,
                    unfocusedTextColor = EditorialWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details, availability or context", color = NeutralGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = EditorialWhite,
                    unfocusedTextColor = EditorialWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_desc_input"),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Issue specific: Location snippet
            if (selectedTab == 2) {
                OutlinedTextField(
                    value = locationOrSpot,
                    onValueChange = { locationOrSpot = it },
                    label = { Text("Approximate location (e.g. Elm St & 4th Cross)", color = NeutralGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = EditorialWhite,
                        unfocusedTextColor = EditorialWhite,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_location_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category Selector
            Text(
                text = "CATEGORY",
                fontFamily = AntonFontFamily,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = EditorialWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                tint = if (isSelected) PureBlack else NeutralGray
                            )
                        },
                        label = {
                            Text(
                                cat,
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeColor,
                            selectedLabelColor = PureBlack,
                            containerColor = DarkSurface,
                            labelColor = EditorialWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) activeColor else DarkCardBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Need specific: Urgency
            if (selectedTab == 0) {
                Text(
                    text = "URGENCY LEVEL",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = EditorialWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL", "TIME_SENSITIVE", "URGENT").forEach { urg ->
                        val isSelected = selectedUrgency == urg
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedUrgency = urg },
                            label = {
                                Text(
                                    urg.replace("_", " "),
                                    fontFamily = AntonFontFamily,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividPink,
                                selectedLabelColor = EditorialWhite,
                                containerColor = DarkSurface,
                                labelColor = EditorialWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) VividPink else DarkCardBorder,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Expiry hours
            if (selectedTab != 2) {
                Text(
                    text = "AUTO-EXPIRES AFTER",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = EditorialWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6 to "6 HOURS", 24 to "24 HOURS", 72 to "3 DAYS").forEach { (hours, label) ->
                        val isSelected = selectedExpiryHours == hours
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedExpiryHours = hours },
                            label = {
                                Text(
                                    label,
                                    fontFamily = AntonFontFamily,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor,
                                selectedLabelColor = PureBlack,
                                containerColor = DarkSurface,
                                labelColor = EditorialWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) activeColor else DarkCardBorder,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        when (selectedTab) {
                            0 -> onCreateNeed(title, description, selectedCategory, selectedUrgency, selectedExpiryHours)
                            1 -> onCreateOffer(title, description, selectedCategory, selectedExpiryHours)
                            2 -> onCreateIssue(title, description, locationOrSpot.ifBlank { "Nearby street block" }, selectedCategory)
                            3 -> onCreateMeetup(title, description, selectedCategory, selectedExpiryHours)
                        }
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_action_button"),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "POST NEED TO NEIGHBORS"
                        1 -> "PUBLISH RESOURCE OFFER"
                        2 -> "SUBMIT CIVIC ISSUE"
                        else -> "PUBLISH MICRO-MEETUP"
                    },
                    fontFamily = AntonFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = if (selectedTab == 0) EditorialWhite else PureBlack
                )
            }
        }
    }
}
