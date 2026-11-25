package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltPillGradient
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.FieryCoralGlow
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleSlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationChatView(
    conversation: Conversation,
    messages: List<ChatMessage>,
    onSendMessage: (text: String, isMeetingPoint: Boolean, meetingPoint: String?) -> Unit,
    onEditMessage: ((messageId: String, newText: String) -> Unit)? = null,
    onDeleteMessage: ((messageId: String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var activeMessageForAction by remember { mutableStateOf<ChatMessage?>(null) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AuraMeshBackground(variant = AuraVariant.MIXED) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LiquidGlassBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = conversation.relatedPostTitle,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = CobaltGlow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = conversation.participantName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldSoft.copy(alpha = 0.85f))
                            .border(1.dp, EmeraldMint.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Zero exact pins shared",
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    ObsidianCanvas.copy(alpha = 0.92f),
                                    ObsidianCanvas
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Quick meeting spot Liquid Glass capsules
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassSecondaryPillButton(
                            text = "Oakwood Pavilion",
                            icon = Icons.Default.Place,
                            onClick = {
                                onSendMessage(
                                    "Let's meet at the Oakwood Community Pavilion for a safe exchange.",
                                    true,
                                    "Oakwood Community Pavilion"
                                )
                            },
                            height = 38.dp
                        )
                        GlassSecondaryPillButton(
                            text = "Contactless Porch",
                            onClick = {
                                onSendMessage(
                                    "I can do a contactless porch drop-off or pickup.",
                                    false,
                                    null
                                )
                            },
                            height = 38.dp
                        )
                    }

                    // Tonesmith-style Full-Pill Message Input + Circular Liquid Glass Cobalt Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Send A Message",
                                    color = MutedSlate,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCobalt,
                                unfocusedBorderColor = HairlineGlassBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = ObsidianInk,
                                focusedContainerColor = DarkGlassCard,
                                unfocusedContainerColor = DarkGlassCard
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        CircularOutlineIconButton(
                            icon = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim(), false, null)
                                    inputText = ""
                                }
                            },
                            size = 50.dp,
                            containerColor = ElectricCobalt.copy(alpha = 0.80f),
                            borderColor = CobaltGlow.copy(alpha = 0.65f),
                            iconTint = PureWhite,
                            modifier = Modifier.testTag("chat_send_button")
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = DarkGlassCard.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, HairlineGlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Connected via NearGrid (${conversation.distanceBand}). Exact residential location stays private.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    val isMe = msg.isFromCurrentUser

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        if (msg.isPickupLocation && msg.meetingPoint != null) {
                            Surface(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .widthIn(max = 295.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = DarkGlassCard,
                                border = BorderStroke(1.dp, ElectricCobalt.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CobaltSoft)
                                            .border(1.dp, CobaltGlow.copy(alpha = 0.45f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = CobaltGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Proposed Exchange Point",
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = CobaltGlow
                                        )
                                        Text(
                                            text = msg.meetingPoint,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }
                        }

                        Box {
                            Surface(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .pointerInput(msg.id) {
                                        detectTapGestures(
                                            onLongPress = {
                                                activeMessageForAction = msg
                                            }
                                        )
                                    }
                                    .testTag("chat_bubble_${msg.id}"),
                                shape = RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isMe) 20.dp else 6.dp,
                                    bottomEnd = if (isMe) 6.dp else 20.dp
                                ),
                                color = if (isMe) ElectricCobalt else ElevatedGlassSurface,
                                border = if (!isMe) BorderStroke(1.dp, HairlineGlassBorder) else null
                            ) {
                                Text(
                                    text = msg.text,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = if (isMe) PureWhite else ObsidianInk,
                                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = activeMessageForAction?.id == msg.id,
                                onDismissRequest = { activeMessageForAction = null },
                                modifier = Modifier.background(ElevatedGlassSurface)
                            ) {
                                if (isMe) {
                                    DropdownMenuItem(
                                        text = { Text("Edit message", fontFamily = PlusJakartaFontFamily, color = ObsidianInk) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = CobaltGlow, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            val currentMsg = activeMessageForAction
                                            activeMessageForAction = null
                                            editingMessage = currentMsg
                                        },
                                        modifier = Modifier.testTag("chat_menu_edit_${msg.id}")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete message", fontFamily = PlusJakartaFontFamily, color = FieryCoralGlow) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = FieryCoralGlow, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            val currentMsg = activeMessageForAction
                                            activeMessageForAction = null
                                            messageToDelete = currentMsg
                                        },
                                        modifier = Modifier.testTag("chat_menu_delete_${msg.id}")
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Copy text", fontFamily = PlusJakartaFontFamily, color = ObsidianInk) },
                                    leadingIcon = {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(msg.text))
                                        activeMessageForAction = null
                                    },
                                    modifier = Modifier.testTag("chat_menu_copy_${msg.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                        Text(
                            text = timeStr,
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 10.sp,
                            color = SubtleSlate,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (editingMessage != null) {
        val targetMsg = editingMessage!!
        var editedContent by remember(targetMsg.id) { mutableStateOf(targetMsg.text) }

        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = {
                Text(
                    text = "Edit Message",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite
                )
            },
            text = {
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    label = { Text("Message text", fontFamily = PlusJakartaFontFamily) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_chat_message_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = HairlineGlassBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = ObsidianInk
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedContent.isNotBlank()) {
                            val id = targetMsg.id
                            editingMessage = null
                            onEditMessage?.invoke(id, editedContent.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCobalt),
                    shape = CircleShape,
                    modifier = Modifier.testTag("save_edit_chat_message_button")
                ) {
                    Text("Save", color = PureWhite, fontFamily = PlusJakartaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingMessage = null },
                    modifier = Modifier.testTag("cancel_edit_chat_message_button")
                ) {
                    Text("Cancel", color = MutedSlate, fontFamily = PlusJakartaFontFamily)
                }
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (messageToDelete != null) {
        val targetMsg = messageToDelete!!
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = {
                Text(
                    text = "Delete Message?",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this message? This action cannot be undone.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 13.sp,
                    color = MutedSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetMsg.id
                        messageToDelete = null
                        onDeleteMessage?.invoke(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FieryCoral),
                    shape = CircleShape,
                    modifier = Modifier.testTag("confirm_delete_chat_message_button")
                ) {
                    Text("Delete", color = PureWhite, fontFamily = PlusJakartaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { messageToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_chat_message_button")
                ) {
                    Text("Cancel", color = MutedSlate, fontFamily = PlusJakartaFontFamily)
                }
            },
            containerColor = DarkGlassCard,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
