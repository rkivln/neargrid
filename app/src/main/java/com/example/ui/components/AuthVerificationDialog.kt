package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCell
import com.example.ui.theme.AntonFontFamily
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.EditorialWhite
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PureBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthVerificationDialog(
    currentCell: GridCell?,
    onDismiss: () -> Unit,
    onVerified: (name: String) -> Unit
) {
    var residentName by remember { mutableStateOf("Elena Rostova") }
    var cellPasscode by remember { mutableStateOf("8826-OAK") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "RESIDENT PASS",
                        fontFamily = AntonFontFamily,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        color = ElectricLime
                    )
                    Text(
                        text = "Verify neighborhood credentials & unlock coordinate exchange",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditorialWhite.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_auth_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = EditorialWhite)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cell Badge
            Surface(
                color = PureBlack,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PureBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ASSIGNED TO ${currentCell?.name?.uppercase() ?: "OAKWOOD GROVE"}",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            color = EditorialWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${currentCell?.code ?: "#H3-8826"} • Zero Private Tracking Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Resident Name input
            OutlinedTextField(
                value = residentName,
                onValueChange = {
                    residentName = it
                    errorMessage = null
                },
                label = { Text("Neighbor Display Name", color = EditorialWhite.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ElectricLime)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = EditorialWhite,
                    unfocusedTextColor = EditorialWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cell Passcode / Invite code
            OutlinedTextField(
                value = cellPasscode,
                onValueChange = {
                    cellPasscode = it
                    errorMessage = null
                },
                label = { Text("Neighborhood Cell Key / Invite Code", color = EditorialWhite.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ElectricLime)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = EditorialWhite,
                    unfocusedTextColor = EditorialWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_key_input"),
                singleLine = true
            )

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (residentName.isBlank()) {
                        errorMessage = "Please enter your name"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        delay(600)
                        isLoading = false
                        isSuccess = true
                        delay(300)
                        onVerified(residentName)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_verify_button"),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = PureBlack,
                        strokeWidth = 2.5.dp
                    )
                } else if (isSuccess) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PureBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASS VERIFIED",
                            fontFamily = AntonFontFamily,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = PureBlack
                        )
                    }
                } else {
                    Text(
                        text = "VERIFY NEIGHBORHOOD PASS",
                        fontFamily = AntonFontFamily,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = PureBlack
                    )
                }
            }
        }
    }
}
