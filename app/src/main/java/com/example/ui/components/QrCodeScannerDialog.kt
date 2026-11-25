package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.GridCell
import com.example.ui.theme.AuraKickerStyle
import com.example.ui.theme.CobaltGlow
import com.example.ui.theme.CobaltSoft
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.ElevatedGlassSurface
import com.example.ui.theme.FieryCoral
import com.example.ui.theme.HairlineGlassBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleGlassDivider
import com.example.util.QrCodePayloadHelper

@Composable
fun QrCodeScannerDialog(
    sampleCells: List<GridCell> = emptyList(),
    onScannedGeohash: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission needed for scanning QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualInput by remember { mutableStateOf("") }
    var detectedCellCode by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "qr_laser")
    val laserOffsetFraction by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "qr_laser_y"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("qr_scanner_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = DarkGlassCard,
            border = BorderStroke(1.dp, HairlineGlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CobaltSoft)
                                .border(1.dp, ElectricCobalt.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "QR SCANNER",
                                style = AuraKickerStyle,
                                color = CobaltGlow
                            )
                            Text(
                                text = "Scan Grid QR Code",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PureWhite,
                                modifier = Modifier.testTag("qr_scanner_title")
                            )
                        }
                    }

                    CircularOutlineIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.testTag("qr_scanner_close")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = SubtleGlassDivider)
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.12f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ObsidianBlack)
                        .border(1.5.dp, ElectricCobalt.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                        .testTag("camera_viewfinder_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = CobaltGlow,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Camera Permission Required",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Grant permission to scan physical QR code posters or select a sample cell below.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.sp,
                                color = MutedSlate,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            PillPrimaryButton(
                                text = "Allow Camera",
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                            )
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .size(188.dp)
                            .border(1.5.dp, Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                    ) {
                        val laserY = maxHeight * laserOffsetFraction
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = laserY)
                                .height(2.5.dp)
                                .padding(horizontal = 8.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            CobaltGlow.copy(alpha = 0.15f),
                                            FieryCoral,
                                            CobaltGlow.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        shape = CircleShape,
                        color = ObsidianCanvas.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, HairlineGlassBorder)
                    ) {
                        Text(
                            text = if (detectedCellCode != null) "DETECTED: $detectedCellCode" else "ALIGN QR CODE INSIDE FRAME",
                            style = AuraKickerStyle,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NEARBY INVITATIONS",
                        style = AuraKickerStyle,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val optionsToDisplay = sampleCells.ifEmpty {
                        listOf(
                            GridCell("cell_oakwood", "Oakwood Grove", "#9q8yyk8", activeNeighborsCount = 42),
                            GridCell("cell_pinecrest", "Pinecrest Heights", "#9q8yyk9", activeNeighborsCount = 28),
                            GridCell("cell_maplewood", "Maplewood Station", "#9q8yyka", activeNeighborsCount = 19)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(optionsToDisplay) { cell ->
                            Surface(
                                shape = CircleShape,
                                color = ElevatedGlassSurface,
                                border = BorderStroke(1.dp, HairlineGlassBorder),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        detectedCellCode = cell.code
                                        onScannedGeohash(cell.code)
                                    }
                                    .testTag("sample_qr_cell_${cell.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cell.name,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = ObsidianInk
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cell.code,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CobaltGlow
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = {
                        Text(
                            "Paste code (e.g. #9q8yyk8 or NEARGRID pass)",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.sp,
                            color = MutedSlate
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_manual_input"),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCobalt,
                        unfocusedBorderColor = HairlineGlassBorder,
                        focusedContainerColor = ElevatedGlassSurface,
                        unfocusedContainerColor = ElevatedGlassSurface,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = ObsidianInk
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (manualInput.isNotBlank()) {
                            onScannedGeohash(manualInput.trim())
                        }
                    }),
                    trailingIcon = {
                        if (manualInput.isNotBlank()) {
                            IconButton(onClick = { onScannedGeohash(manualInput.trim()) }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Submit Code",
                                    tint = CobaltGlow
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val parsedActive = remember(detectedCellCode, manualInput) {
                    val raw = detectedCellCode ?: manualInput.trim()
                    if (raw.isNotBlank()) QrCodePayloadHelper.parsePayload(raw) else null
                }

                WhiteHeroPillButton(
                    text = if (parsedActive != null) "Join ${parsedActive.name} (${parsedActive.code})" else "Join Scanned Cell",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = {
                        val target = detectedCellCode ?: manualInput.ifBlank { "#9q8yyk8" }
                        onScannedGeohash(target)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_scanned_cell_button")
                )
            }
        }
    }
}
