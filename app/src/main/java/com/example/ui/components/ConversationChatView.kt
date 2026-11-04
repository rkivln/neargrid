package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.ui.theme.DMSerifFontFamily
import com.example.ui.theme.DMSansFontFamily
import com.example.ui.theme.PaleWarmGray
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
import com.example.ui.theme.WarmDivider
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation.participantName,
                            fontFamily = DMSerifFontFamily,
                            fontSize = 20.sp,
                            color = PrimaryText
                        )
                        Text(
                            text = "${conversation.relatedPostTitle} • ${conversation.distanceBand}",
                            fontFamily = DMSansFontFamily,
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to inbox",
                            tint = PrimaryText
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PastelCoral,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zero home pins",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = PrimaryText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Quick meeting spot preset chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSendMessage(
                                    "Let's meet at the Oakwood Community Pavilion for a safe exchange.",
                                    true,
                                    "Oakwood Community Pavilion"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = PastelCoral,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Oakwood Pavilion",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = PrimaryText
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                onSendMessage(
                                    "I can do a contactless porch drop-off or pickup.",
                                    false,
                                    null
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftCream),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "Contactless Porch",
                                fontFamily = DMSansFontFamily,
                                fontSize = 11.sp,
                                color = PrimaryText
                            )
                        }
                    }

                    // Input field and send button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Message ${conversation.participantName}...", color = SecondaryText, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryText,
                                unfocusedBorderColor = WarmBorder,
                                focusedTextColor = PrimaryText,
                                unfocusedTextColor = PrimaryText,
                                focusedContainerColor = SoftCream,
                                unfocusedContainerColor = SoftCream
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim(), false, null)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryText)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WarmIvory)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PastelMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connected via NearGrid. Your precise residential location is never disclosed.",
                            fontFamily = DMSansFontFamily,
                            fontSize = 12.sp,
                            color = SecondaryText,
                            lineHeight = 16.sp
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
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .width(280.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SoftCream),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCoral.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PastelPeach),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = PrimaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Proposed Exchange Point",
                                        fontFamily = DMSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = PastelCoral
                                    )
                                    Text(
                                        text = msg.meetingPoint,
                                        fontFamily = DMSerifFontFamily,
                                        fontSize = 14.sp,
                                        color = PrimaryText
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        Surface(
                            modifier = Modifier
                                .pointerInput(msg.id) {
                                    detectTapGestures(
                                        onLongPress = {
                                            activeMessageForAction = msg
                                        }
                                    )
                                }
                                .testTag("chat_bubble_${msg.id}"),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) PrimaryText else PureWhite,
                            border = if (!isMe) androidx.compose.foundation.BorderStroke(1.dp, WarmBorder) else null,
                            shadowElevation = 0.5.dp
                        ) {
                            Text(
                                text = msg.text,
                                fontFamily = DMSansFontFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = if (isMe) PureWhite else PrimaryText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = activeMessageForAction?.id == msg.id,
                            onDismissRequest = { activeMessageForAction = null },
                            modifier = Modifier.background(PureWhite)
                        ) {
                            if (isMe) {
                                DropdownMenuItem(
                                    text = { Text("Edit message", fontFamily = DMSansFontFamily, color = PrimaryText) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = PastelCoral, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        val currentMsg = activeMessageForAction
                                        activeMessageForAction = null
                                        editingMessage = currentMsg
                                    },
                                    modifier = Modifier.testTag("chat_menu_edit_${msg.id}")
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete message", fontFamily = DMSansFontFamily, color = Color(0xFFD32F2F)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
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
                                text = { Text("Copy text", fontFamily = DMSansFontFamily, color = PrimaryText) },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    activeMessageForAction = null
                                },
                                modifier = Modifier.testTag("chat_menu_copy_${msg.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                    Text(
                        text = timeStr,
                        fontFamily = DMSansFontFamily,
                        fontSize = 10.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
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
                    fontFamily = DMSerifFontFamily,
                    fontSize = 20.sp,
                    color = PrimaryText
                )
            },
            text = {
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    label = { Text("Message text", fontFamily = DMSansFontFamily) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_chat_message_input"),
                    shape = RoundedCornerShape(12.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryText),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_edit_chat_message_button")
                ) {
                    Text("Save", color = PureWhite, fontFamily = DMSansFontFamily)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingMessage = null },
                    modifier = Modifier.testTag("cancel_edit_chat_message_button")
                ) {
                    Text("Cancel", color = SecondaryText, fontFamily = DMSansFontFamily)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (messageToDelete != null) {
        val targetMsg = messageToDelete!!
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = {
                Text(
                    text = "Delete Message?",
                    fontFamily = DMSerifFontFamily,
                    fontSize = 18.sp,
                    color = PrimaryText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this message? This action cannot be undone.",
                    fontFamily = DMSansFontFamily,
                    fontSize = 13.sp,
                    color = SecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetMsg.id
                        messageToDelete = null
                        onDeleteMessage?.invoke(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_delete_chat_message_button")
                ) {
                    Text("Delete", color = PureWhite, fontFamily = DMSansFontFamily)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { messageToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_chat_message_button")
                ) {
                    Text("Cancel", color = SecondaryText, fontFamily = DMSansFontFamily)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
