package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VividViolet

@Composable
fun InboxScreen(
    conversations: List<Conversation>,
    onSelectConversation: (Conversation) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(PureBlack)) {
        // Oversized background typographic word
        Text(
            text = "INBOX",
            fontFamily = AntonFontFamily,
            fontSize = 140.sp,
            color = CharcoalBlack.copy(alpha = 0.5f),
            letterSpacing = 6.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp)
                .offset(x = 10.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalBlack,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "COORDINATION INBOX",
                                fontFamily = AntonFontFamily,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp,
                                color = VividViolet
                            )
                            Text(
                                text = "Private handoff coordination & safe exchange protocols",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VividViolet
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = EditorialWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ENCRYPTED",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = EditorialWhite
                                )
                            }
                        }
                    }
                }
            }

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = NeutralGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO CONVERSATIONS YET",
                            fontFamily = AntonFontFamily,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = EditorialWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When you connect with a neighbor about a tool, need, or meetup, your private chat will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Safe Handoff Guide Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VividViolet.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(VividViolet),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Handshake,
                                        contentDescription = null,
                                        tint = EditorialWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SAFE HANDOFF PROTOCOL",
                                        fontFamily = AntonFontFamily,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp,
                                        color = VividViolet
                                    )
                                    Text(
                                        text = "Agree on verified public spots like Oakwood Pavilion or use contactless porch drop-offs.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralGray
                                    )
                                }
                            }
                        }
                    }

                    items(conversations, key = { it.id }) { conv ->
                        ConversationItemCard(
                            conversation = conv,
                            onClick = { onSelectConversation(conv) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItemCard(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("conversation_item_${conversation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(VividViolet),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.participantName.take(2).uppercase(),
                    fontFamily = AntonFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp,
                    color = EditorialWhite
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.participantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EditorialWhite
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VividViolet,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = conversation.distanceBand.uppercase(),
                            fontFamily = AntonFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = NeutralGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "RE: ${conversation.relatedPostTitle.uppercase()}",
                    fontFamily = AntonFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = VividViolet,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NeutralGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
