package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ScanRecord
import com.example.ui.components.ActionBadge
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.UiState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay

@Composable
fun ScannerScreen(
    state: UiState,
    onScanToken: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lang = state.language

    var tokenInput by remember { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                if (lang == "ar") "إذن الكاميرا مطلوب لمسح الرموز مباشرة" else "L'autorisation de la caméra est requise pour scanner",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Auto-request camera permission if not granted yet
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera state controls
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var lastScannedToken by remember { mutableStateOf<String?>(null) }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }

    // Audio feedback on scan
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {}
        }
    }

    // Picker for QR images from Gallery
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val found = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        if (found != null && found.rawValue != null) {
                            try {
                                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            } catch (_: Exception) {}
                            onScanToken(found.rawValue!!)
                            Toast.makeText(
                                context,
                                if (lang == "ar") "تم مسح الرمز من الصورة بنجاح!" else "QR code scanné depuis l'image!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                if (lang == "ar") "لم يتم العثور على رمز QR واضح في الصورة" else "Aucun QR code lisible trouvé dans l'image",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            if (lang == "ar") "فشل تحليل الصورة" else "Échec de l'analyse de l'image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (lang == "ar") "خطأ في قراءة الصورة: ${e.message}" else "Erreur: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Laser beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    // Green scan success indicator state
    var scanSuccessActive by remember { mutableStateOf(false) }

    LaunchedEffect(state.scanSuccessCount) {
        if (state.scanSuccessCount > 0) {
            scanSuccessActive = true
            delay(5000)
            scanSuccessActive = false
        }
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (scanSuccessActive || state.lastScanResult != null) StatusSuccess else PetrolBlue,
        animationSpec = tween(400),
        label = "border_color"
    )

    val animatedLaserColor by animateColorAsState(
        targetValue = if (scanSuccessActive || state.lastScanResult != null) StatusSuccess else BrandOrange,
        animationSpec = tween(400),
        label = "laser_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top instruction card with dynamic status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (scanSuccessActive) StatusSuccessBg else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (scanSuccessActive) StatusSuccess else PetrolBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (scanSuccessActive) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = if (scanSuccessActive) Color.White else PetrolBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (scanSuccessActive) {
                            if (lang == "ar") "تم المسح بنجاح! ✓" else "Scanné avec succès! ✓"
                        } else {
                            AppStrings.t("scanner", lang)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (scanSuccessActive) StatusSuccess else PetrolBlue
                        )
                    )
                    Text(
                        text = if (scanSuccessActive) {
                            if (lang == "ar") "تم تسجيل حركة التلميذ وتحديث الحضور" else "Présence de l'élève mise à jour"
                        } else {
                            if (lang == "ar") "وجّه الكاميرا نحو رمز QR الخاص بالتلميذ" else "Dirigez la caméra vers le QR code de l'élève"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (scanSuccessActive) StatusSuccess else TextMuted
                        )
                    )
                }
            }
        }

        // Scanner Viewport Frame with REAL ML Kit Barcode Analyzer
        Box(
            modifier = Modifier
                .size(290.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF0F1E24))
                .border(
                    width = if (scanSuccessActive) 4.5.dp else 3.dp,
                    color = animatedBorderColor,
                    shape = RoundedCornerShape(26.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                key(lensFacing) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val executor = ContextCompat.getMainExecutor(ctx)

                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    val barcodeScanner = BarcodeScanning.getClient(
                                        BarcodeScannerOptions.Builder()
                                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
                                            .build()
                                    )

                                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val inputImage = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val raw = barcode.rawValue
                                                        if (!raw.isNullOrBlank()) {
                                                            val now = System.currentTimeMillis()
                                                            if (now - lastScannedTimestamp > 2500 || raw != lastScannedToken) {
                                                                lastScannedTimestamp = now
                                                                lastScannedToken = raw
                                                                try {
                                                                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                                                } catch (_: Exception) {}
                                                                onScanToken(raw)
                                                            }
                                                            break
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    val cameraSelector = CameraSelector.Builder()
                                        .requireLensFacing(lensFacing)
                                        .build()

                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraControl = camera.cameraControl
                                    camera.cameraControl.enableTorch(isTorchOn)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, executor)

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == "ar") "انقر لتفعيل الكاميرا" else "Activer la caméra",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (lang == "ar") "إذن الكاميرا" else "Autoriser")
                    }
                }
            }

            // Animated laser scan line overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (290 * laserOffset).dp)
                    .background(animatedLaserColor)
            )

            // Prominent Green overlay when scan completed!
            androidx.compose.animation.AnimatedVisibility(
                visible = scanSuccessActive,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StatusSuccess.copy(alpha = 0.95f),
                    shadowElevation = 10.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Scanned",
                                tint = StatusSuccess,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "ar") "تم المسح بنجاح" else "Scanné avec succès",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        state.lastScanResult?.let { last ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = last.studentName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Camera Quick Action Bar: Flashlight, Camera Flip, Gallery QR Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flashlight / Torch Toggle
                TextButton(
                    onClick = {
                        val nextState = !isTorchOn
                        isTorchOn = nextState
                        try {
                            cameraControl?.enableTorch(nextState)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.testTag("scanner_torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (isTorchOn) BrandOrange else PetrolBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTorchOn) {
                            if (lang == "ar") "إطفاء الفلاش" else "Flash On"
                        } else {
                            if (lang == "ar") "تشغيل الفلاش" else "Flash"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isTorchOn) BrandOrange else PetrolBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Switch Camera Lens (Front/Back)
                TextButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier.testTag("scanner_switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = PetrolBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == "ar") "تبديل الكاميرا" else "Inverser",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PetrolBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Pick QR image from Gallery
                TextButton(
                    onClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("scanner_pick_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Pick Image",
                        tint = PetrolBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == "ar") "صورة QR" else "Galerie",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PetrolBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Last Scanned Result Banner - Styled in Bright Green indicating "تم المسح"
        state.lastScanResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StatusSuccessBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusSuccess),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ar") "تم المسح بنجاح ✓" else "Scanné avec succès ✓",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusSuccess
                                )
                            )
                        }
                        ActionBadge(action = result.action)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = result.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.studentCode} • ${result.roomName} • ${result.time}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF1E3A34),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Manual QR Input Section with SOLID BLACK TYPING FONT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppStrings.t("manual_scan", lang),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PetrolBlue
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Input with explicitly black text
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    placeholder = {
                        Text(
                            text = AppStrings.t("enter_token", lang),
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = PetrolBlue)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanner_token_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (tokenInput.isNotBlank()) {
                            try {
                                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            } catch (_: Exception) {}
                            onScanToken(tokenInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("scanner_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    enabled = tokenInput.isNotBlank() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = AppStrings.t("scan_qr", lang),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick test tokens
                val sampleTokens = remember(state.students) {
                    val fromStudents = state.students.filter { it.qrToken.isNotBlank() }.map { it.qrToken }
                    if (fromStudents.isNotEmpty()) fromStudents else listOf(
                        "STU-1254-AB3F7C",
                        "STU-1001-FF22AA",
                        "STU-2002-C9D8E7"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = AppStrings.t("quick_test_codes", lang),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleTokens) { token ->
                        Surface(
                            onClick = {
                                tokenInput = token
                                try {
                                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                } catch (_: Exception) {}
                                onScanToken(token)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Text(
                                text = token,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PetrolBlue
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Quick Student Attendance List (Direct 1-tap scan test for any student)
                if (state.students.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "ar") "قائمة التلاميذ للمسح السريع:" else "Élèves pour scan rapide :",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.students.take(5).forEach { student ->
                            Surface(
                                onClick = {
                                    val token = student.qrToken.ifEmpty { student.studentId }
                                    tokenInput = token
                                    try {
                                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                    } catch (_: Exception) {}
                                    onScanToken(token)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = student.fullName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextMain
                                            )
                                        )
                                        Text(
                                            text = "${student.studentId} • ${student.roomName ?: ""}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextMuted
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BrandOrange.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = null,
                                                tint = BrandOrange,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "ar") "مسح" else "Scanner",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = BrandOrange,
                                                    fontWeight = FontWeight.Bold
                                                )
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
    }
}
