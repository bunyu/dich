package com.bunyu.dich.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.bunyu.dich.engine.PlayState
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bunyu.dich.R
import com.bunyu.dich.dichApp
import com.bunyu.dich.engine.Engine
import com.bunyu.dich.ui.theme.Green
import kotlin.math.max
import kotlin.math.min

@Composable
fun CameraScreen(onBack: () -> Unit, vm: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) askPermission.launch(Manifest.permission.CAMERA)
    }
    // Leaving the screen always starts fresh next time.
    DisposableEffect(Unit) { onDispose { vm.reset() } }

    if (!hasPermission) {
        PermissionNeeded(onAsk = { askPermission.launch(Manifest.permission.CAMERA) }, onBack = onBack)
        return
    }

    when (val s = vm.state) {
        CameraState.Aiming -> Aiming(onPhoto = vm::onPhoto, onError = vm::onCaptureError, onBack = onBack)
        is CameraState.Working -> Working(s, onBack = onBack)
        is CameraState.Done -> Result(s, vm = vm, onBack = onBack)
        is CameraState.Failed -> Failed(s, onRetake = vm::reset, onBack = onBack)
    }
}

private fun Context.hasCameraPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@Composable
private fun Aiming(onPhoto: (Bitmap) -> Unit, onError: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            val p = future.get()
            provider = p
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            p.unbindAll()
            camera = p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BackButton(onBack)
                BigButton(
                    text = stringResource(R.string.torch),
                    icon = if (torchOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                    onClick = {
                        torchOn = !torchOn
                        camera?.cameraControl?.enableTorch(torchOn)
                    },
                    container = if (torchOn) Color(0xFFFFC107) else Color(0xFF333333),
                    content = if (torchOn) Color.Black else Color.White,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.camera_hint),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(12.dp),
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (capturing) return@Button
                    capturing = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val photo = image.toBitmap().upright(image.imageInfo.rotationDegrees)
                                image.close()
                                capturing = false
                                onPhoto(photo)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                onError()
                            }
                        },
                    )
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(56.dp))
                    Text(stringResource(R.string.camera_translate), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Rotates the photo upright and shrinks it: OCR does not need 50 MP and memory is limited. */
private fun Bitmap.upright(rotationDegrees: Int, maxSide: Int = 2400): Bitmap {
    val scale = min(1f, maxSide.toFloat() / max(width, height))
    val matrix = Matrix().apply {
        postRotate(rotationDegrees.toFloat())
        postScale(scale, scale)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

@Composable
private fun Working(state: CameraState.Working, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            bitmap = state.photo.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.35f,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BackButton(onBack, Modifier.align(Alignment.Start))
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(color = Color.White, strokeWidth = 8.dp, modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(state.message),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun Result(
    state: CameraState.Done,
    vm: CameraViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val speaker = context.dichApp.speaker
    val textSize = remember { context.dichApp.prefs.textSizeSp }
    var showOriginal by remember { mutableStateOf(false) }
    val playing = speaker.playState
    val current = if (playing == PlayState.Idle) -1 else speaker.segmentIndex

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Spacer(Modifier.weight(1f))
            BigButton(
                text = stringResource(R.string.retake),
                icon = Icons.Filled.CameraAlt,
                onClick = vm::reset,
                container = Green,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.translated_from, stringResource(state.from.label)) + "  ·  " +
                stringResource(if (state.engine == Engine.Online) R.string.engine_online else R.string.engine_offline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
                // Each paragraph is tappable (read from here) and highlighted while it is read.
                state.blocks.forEachIndexed { i, block ->
                    Text(
                        block.vietnamese,
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.35f).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (i == current) Highlight else Color.Transparent)
                            .clickable { vm.readAloud(from = i) }
                            .padding(8.dp),
                    )
                }
                if (showOriginal) {
                    Spacer(Modifier.height(24.dp))
                    Text(state.original, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
            }
        }
        TextButton(onClick = { showOriginal = !showOriginal }, modifier = Modifier.align(Alignment.End)) {
            Text(
                stringResource(if (showOriginal) R.string.hide_original else R.string.show_original),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when (playing) {
            PlayState.Playing -> BigButton(
                text = stringResource(R.string.pause),
                icon = Icons.Filled.Pause,
                onClick = vm::pause,
                container = Color(0xFFB26A00),
                modifier = Modifier.fillMaxWidth().height(88.dp),
            )
            PlayState.Paused -> BigButton(
                text = stringResource(R.string.resume),
                icon = Icons.Filled.PlayArrow,
                onClick = vm::resume,
                modifier = Modifier.fillMaxWidth().height(88.dp),
            )
            PlayState.Idle -> BigButton(
                text = stringResource(R.string.read_aloud),
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                onClick = { vm.readAloud() },
                modifier = Modifier.fillMaxWidth().height(88.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BigButton(
                text = stringResource(R.string.replay),
                icon = Icons.Filled.Replay,
                onClick = vm::replay,
                container = Color(0xFF555555),
                modifier = Modifier.weight(1f),
            )
            BigButton(
                text = stringResource(R.string.stop_reading),
                icon = Icons.AutoMirrored.Filled.VolumeOff,
                onClick = vm::stopReading,
                container = Color(0xFF555555),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val Highlight = Color(0xFFFFE082)

@Composable
private fun Failed(state: CameraState.Failed, onRetake: () -> Unit, onBack: () -> Unit) {
    MessageWithActions(
        message = stringResource(state.message),
        primaryText = stringResource(R.string.retake),
        onPrimary = onRetake,
        onBack = onBack,
    )
}

@Composable
private fun PermissionNeeded(onAsk: () -> Unit, onBack: () -> Unit) {
    MessageWithActions(
        message = stringResource(R.string.camera_permission),
        primaryText = stringResource(R.string.allow),
        onPrimary = onAsk,
        onBack = onBack,
    )
}

@Composable
private fun MessageWithActions(message: String, primaryText: String, onPrimary: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BackButton(onBack)
        Text(
            message,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        BigButton(
            text = primaryText,
            icon = Icons.Filled.CameraAlt,
            onClick = onPrimary,
            container = Green,
            modifier = Modifier.fillMaxWidth().height(88.dp),
        )
    }
}
