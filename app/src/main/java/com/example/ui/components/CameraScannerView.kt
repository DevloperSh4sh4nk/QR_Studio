package com.example.ui.components

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qr.QrCodeDecoder
import java.util.concurrent.Executors

@Composable
fun CameraScannerView(
    isFlashlightOn: Boolean,
    isFrontCamera: Boolean,
    onQrDecoded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var lastScannedTimestamp by remember { mutableStateOf(0L) }

    // Toggle flashlight
    LaunchedEffect(isFlashlightOn, camera) {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                it.cameraControl.enableTorch(isFlashlightOn)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val pView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                previewView = pView

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(pView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val currentTime = System.currentTimeMillis()
                        // Debounce scans by 1.5 seconds
                        if (currentTime - lastScannedTimestamp > 1500) {
                            val decoded = QrCodeDecoder.decodeImageProxy(imageProxy)
                            if (decoded != null) {
                                lastScannedTimestamp = currentTime
                                ContextCompat.getMainExecutor(ctx).execute {
                                    onQrDecoded(decoded)
                                }
                            }
                        }
                        imageProxy.close()
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                pView
            },
            update = {
                // Trigger updates if camera facing changes
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView?.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScannedTimestamp > 1500) {
                            val decoded = QrCodeDecoder.decodeImageProxy(imageProxy)
                            if (decoded != null) {
                                lastScannedTimestamp = currentTime
                                ContextCompat.getMainExecutor(context).execute {
                                    onQrDecoded(decoded)
                                }
                            }
                        }
                        imageProxy.close()
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Scanner Reticle & Laser Animation
        ScannerOverlay(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val boxSize = canvasWidth * 0.72f
        val boxLeft = (canvasWidth - boxSize) / 2f
        val boxTop = (canvasHeight - boxSize) / 2f - 40.dp.toPx()
        val boxRight = boxLeft + boxSize
        val boxBottom = boxTop + boxSize
        val cornerLength = 32.dp.toPx()
        val strokeW = 4.dp.toPx()

        // 1. Semi-transparent backdrop with clear cutout
        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(boxLeft, boxTop, boxRight, boxBottom),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            )
        }

        // Dark dim layer outside reticle
        drawRect(color = Color.Black.copy(alpha = 0.55f))
        // Cut out hole
        drawPath(
            path = cutoutPath,
            color = Color.Transparent,
            blendMode = BlendMode.Clear
        )

        // 2. Glowing Reticle Corners (Electric Cyan / Neon Blue)
        val cornerColor = Color(0xFF06B6D4)
        val cornerPaint = Stroke(width = strokeW)

        // Top-Left Corner
        drawLine(cornerColor, Offset(boxLeft, boxTop + cornerLength), Offset(boxLeft, boxTop + 16.dp.toPx()), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLength, boxTop), strokeWidth = strokeW)

        // Top-Right Corner
        drawLine(cornerColor, Offset(boxRight - cornerLength, boxTop), Offset(boxRight, boxTop), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(boxRight, boxTop), Offset(boxRight, boxTop + cornerLength), strokeWidth = strokeW)

        // Bottom-Left Corner
        drawLine(cornerColor, Offset(boxLeft, boxBottom - cornerLength), Offset(boxLeft, boxBottom), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(boxLeft, boxBottom), Offset(boxLeft + cornerLength, boxBottom), strokeWidth = strokeW)

        // Bottom-Right Corner
        drawLine(cornerColor, Offset(boxRight - cornerLength, boxBottom), Offset(boxRight, boxBottom), strokeWidth = strokeW)
        drawLine(cornerColor, Offset(boxRight, boxBottom - cornerLength), Offset(boxRight, boxBottom), strokeWidth = strokeW)

        // 3. Animated Laser Scan Bar
        val laserY = boxTop + (boxSize * laserProgress)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF06B6D4).copy(alpha = 0.85f),
                    Color(0xFF6366F1),
                    Color(0xFF06B6D4).copy(alpha = 0.85f),
                    Color.Transparent
                )
            ),
            topLeft = Offset(boxLeft + 8.dp.toPx(), laserY - 1.5.dp.toPx()),
            size = Size(boxSize - 16.dp.toPx(), 3.dp.toPx())
        )
    }
}
