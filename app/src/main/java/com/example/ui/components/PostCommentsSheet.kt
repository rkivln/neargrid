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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeighborPost
import com.example.data.model.PostComment
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.ui.theme.SubtleSlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentsSheet(
    post: NeighborPost,
    comments: List<PostComment>,
    onSendComment: (text: String, parentId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<PostComment?>(null) }

    val parentComments = remember(comments) {
        comments.filter { it.parentId.isNullOrBlank() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("post_comments_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CobaltSoft)
                            .border(1.dp, ElectricCobalt.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = CobaltGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Grid Thread Discussion",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "${comments.size} REPLIES · ${post.cellName.uppercase()}",
                            style = AuraKickerStyle,
                            color = MutedSlate
                        )
                    }
                }

                CircularOutlineIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_comments_sheet")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Original Post Summary Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = DarkGlassCard,
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = post.title,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = PureWhite,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = ElevatedGlassSurface,
                            border = BorderStroke(1.dp, HairlineGlassBorder)
                        ) {
                            Text(
                                text = post.type.uppercase(),
                                style = AuraKickerStyle,
                                color = CobaltGlow,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (post.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.description,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = MutedSlate,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)

            // Threaded Comments List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (parentComments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No comments yet",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start the discussion and coordinate with your neighbors!",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(parentComments, key = { it.id }) { parent ->
                            val replies = comments.filter { it.parentId == parent.id }

                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = DarkGlassCard,
                                border = BorderStroke(1.dp, HairlineGlassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    CommentItem(
                                        comment = parent,
                                        isReply = false,
                                        onReply = { replyingToComment = parent }
                                    )

                                    if (replies.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 18.dp, end = 12.dp, bottom = 12.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(ElevatedGlassSurface)
                                                .border(1.dp, HairlineGlassBorder, RoundedCornerShape(16.dp))
                                        ) {
                                            replies.forEachIndexed { idx, reply ->
                                                if (idx > 0) {
                                                    HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                                                }
                                                CommentItem(
                                                    comment = reply,
                                                    isReply = true,
                                                    onReply = { replyingToComment = parent }
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

            // Replying Pill Banner
            replyingToComment?.let { replyTarget ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = CircleShape,
                    color = CobaltSoft,
                    border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Replying to ${replyTarget.authorName}",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                        }

                        IconButton(
                            onClick = { replyingToComment = null },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = ObsidianInk,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Full-pill Glass Composer Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = CircleShape,
                color = ElevatedGlassSurface,
                border = BorderStroke(1.dp, HairlineGlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                text = if (replyingToComment != null) "Write a reply..." else "Add a neighbor comment...",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 13.sp,
                                color = MutedSlate
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = ObsidianInk,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_text"),
                        singleLine = true
                    )

                    val canSend = commentText.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (canSend) ElectricCobalt else DarkGlassCard)
                            .border(
                                1.dp,
                                if (canSend) CobaltGlow.copy(alpha = 0.6f) else HairlineGlassBorder,
                                CircleShape
                            )
                            .clickable(enabled = canSend) {
                                if (canSend) {
                                    onSendComment(commentText, replyingToComment?.id)
                                    commentText = ""
                                    replyingToComment = null
                                }
                            }
                            .testTag("send_comment_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Comment",
                            tint = if (canSend) PureWhite else SubtleSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: PostComment,
    isReply: Boolean,
    onReply: () -> Unit
) {
    val formattedTime = remember(comment.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(comment.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isReply) CobaltSoft else EmeraldSoft)
                        .border(
                            1.dp,
                            if (isReply) ElectricCobalt.copy(alpha = 0.5f) else EmeraldMint.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.authorInitials,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isReply) CobaltGlow else EmeraldLight
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.authorName,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = PureWhite
                )
            }

            Text(
                text = formattedTime,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 10.sp,
                color = SubtleSlate
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = comment.text,
            fontFamily = PlusJakartaFontFamily,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = ObsidianInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(ElevatedGlassSurface)
                .border(1.dp, HairlineGlassBorder, CircleShape)
                .clickable { onReply() }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Reply,
                contentDescription = "Reply",
                tint = CobaltGlow,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "REPLY",
                style = AuraKickerStyle,
                color = ObsidianInk
            )
        }
    }
}
