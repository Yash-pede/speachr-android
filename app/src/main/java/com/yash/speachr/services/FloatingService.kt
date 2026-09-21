package com.yash.speachr.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.yash.speachr.core.floating.FloatingViewModel
import com.yash.speachr.ui.theme.*
import kotlinx.coroutines.*
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent

class FloatingService : Service(), KoinComponent, LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var keyboardUpdateJob: Job? = null
    
    private val keyboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val height = intent?.getIntExtra("height", 0) ?: 0
            
            // Debounce keyboard updates to avoid jitter during animation
            // We wait for the keyboard to settle before moving the bubble
            keyboardUpdateJob?.cancel()
            keyboardUpdateJob = serviceScope.launch {
                delay(150) // Reduced delay for better responsiveness, but enough to debounce
                updatePositionForKeyboard(height)
            }
        }
    }

    private fun updatePositionForKeyboard(keyboardHeight: Int) {
        if (::layoutParams.isInitialized && composeView != null) {
            // Margin from the bottom of the screen (or top of keyboard)
            val baseMargin = 50
            layoutParams.y = keyboardHeight + baseMargin
            try {
                windowManager.updateViewLayout(composeView, layoutParams)
            } catch (e: Exception) {
                Log.e("FloatingService", "Failed to update layout", e)
            }
        }
    }

    /**
     * Keeps the screen awake while a transcription is in flight by toggling the
     * FLAG_KEEP_SCREEN_ON flag on our overlay window. This is scoped to our window only,
     * requires no permission and is automatically cleared when the flag is removed or the
     * view is detached.
     */
    private fun updateKeepScreenOn(keepOn: Boolean) {
        if (::layoutParams.isInitialized && composeView != null) {
            val alreadyOn = (layoutParams.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
            if (alreadyOn == keepOn) return

            layoutParams.flags = if (keepOn) {
                layoutParams.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            } else {
                layoutParams.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
            }
            try {
                windowManager.updateViewLayout(composeView, layoutParams)
            } catch (e: Exception) {
                Log.e("FloatingService", "Failed to update screen-on flag", e)
            }
        }
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val filter = IntentFilter("com.yash.speachr.KEYBOARD_UPDATED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(keyboardReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(keyboardReceiver, filter)
        }
        
        startForegroundService()
    }

    private fun startForegroundService() {
        val channelId = "speachr_floating_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Speachr Floating Bubble",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps the Speachr bubble floating over other apps" }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Speachr is active")
            .setContentText("Tap to start dictating")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
        showOverlay()
    }

    private fun showOverlay() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 40
            y = 100 // Default bottom margin
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingService)
            setViewTreeViewModelStoreOwner(this@FloatingService)
            setViewTreeSavedStateRegistryOwner(this@FloatingService)

            setContent {
                KoinAndroidContext {
                    val viewModel: FloatingViewModel = koinViewModel()
                    val sharedPrefs = remember { context.getSharedPreferences("user_settings", Context.MODE_PRIVATE) }
                    
                    val baseSize by remember { mutableStateOf(sharedPrefs.getFloat("bubble_size", 1.0f)) }
                    val baseAlpha by remember { mutableStateOf(sharedPrefs.getFloat("bubble_alpha", 1.0f)) }
                    
                    FloatingBubbleContent(
                        onUpdatePosition = { dx, dy ->
                            // Corrected for BOTTOM | END gravity
                            this@FloatingService.layoutParams.x -= dx.toInt()
                            this@FloatingService.layoutParams.y -= dy.toInt()
                            windowManager.updateViewLayout(this, this@FloatingService.layoutParams)
                        },
                        onCloseService = {
                            stopSelf()
                        },
                        onLoadingStateChanged = { keepOn ->
                            updateKeepScreenOn(keepOn)
                        },
                        onClick = { viewModel.toggleRecording() },
                        isRecording = viewModel.isRecording,
                        isLoading = viewModel.isLoading,
                        baseSize = baseSize,
                        baseAlpha = baseAlpha
                    )
                }
            }
        }

        windowManager.addView(composeView, layoutParams)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try { unregisterReceiver(keyboardReceiver) } catch (e: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let { windowManager.removeView(it) }
        store.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

@Composable
private fun RecordingRipple(rippleScale: Float, rippleAlpha: Float) {
    Canvas(modifier = Modifier.size(64.dp)) {
        drawCircle(
            color = Coral40.copy(alpha = rippleAlpha),
            radius = (size.minDimension / 2) * rippleScale
        )
    }
}

@Composable
private fun SoundwaveBars(bars: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 18.dp)
    ) {
        val barCount = bars.size
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val maxHeight = size.height

        bars.forEachIndexed { index, scale ->
            val barHeight = maxHeight * scale.coerceAtLeast(0.2f)
            val x = (barWidth + spacing) * index
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = Neutral99,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
private fun IdleBubblePlaceholder() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Coral40.copy(alpha = 0.9f))
    )
}

@Composable
private fun LoadingSpinner() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        // Soft coral glow that gently breathes behind the spinner
        Canvas(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = 0.35f
                }
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Coral40.copy(alpha = 0.9f), Color.Transparent)
                )
            )
        }

        // Rotating gradient arc
        Canvas(
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer { rotationZ = rotation }
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color.Transparent, Coral40, Coral80)
                ),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun FloatingBubbleContent(
    onUpdatePosition: (dx: Float, dy: Float) -> Unit,
    onCloseService: () -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    isRecording: Boolean,
    isLoading: Boolean = false,
    baseSize: Float = 1.0f,
    baseAlpha: Float = 1.0f
) {
    var isDragging by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }

    // Notify the service so it can keep the screen awake only while loading.
    LaunchedEffect(isLoading) {
        onLoadingStateChanged(isLoading)
    }

    // Ensure the keep-awake flag is always cleared if the bubble leaves composition.
    DisposableEffect(Unit) {
        onDispose { onLoadingStateChanged(false) }
    }

    LaunchedEffect(isClosing) {
        if (isClosing) {
            delay(250)
            onCloseService()
        }
    }

    val bubbleScale by animateFloatAsState(
        targetValue = when {
            isClosing -> 0f
            isLoading -> 0.9f * baseSize
            isDragging -> 1.15f * baseSize
            isRecording -> 1.05f * baseSize
            else -> 1f * baseSize
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bubbleScale"
    )
    val bubbleAlpha by animateFloatAsState(
        targetValue = if (isClosing) 0f else 1f * baseAlpha,
        animationSpec = tween(250),
        label = "bubbleAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    val b1 by infiniteTransition.animateFloat(
        0.3f,
        1f,
        infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "b1"
    )
    val b2 by infiniteTransition.animateFloat(
        1f,
        0.4f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "b2"
    )
    val b3 by infiniteTransition.animateFloat(
        0.5f,
        0.9f,
        infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "b3"
    )
    val bars = listOf(b1, b2, b3, b2, b1)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .graphicsLayer {
                scaleX = bubbleScale
                scaleY = bubbleScale
                alpha = bubbleAlpha
            }
            .pointerInput(isLoading) {
                if (!isLoading) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            isClosing = true
                        },
                    )
                }
            }
            .pointerInput(isLoading) {
                if (!isLoading) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    ) { change, dragAmount ->
                        change.consume()
                        onUpdatePosition(dragAmount.x, dragAmount.y)
                    }
                }
            }
    ) {
        if (isRecording && !isDragging && !isLoading) {
            RecordingRipple(rippleScale, rippleAlpha)
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = when {
                        isLoading -> Brush.linearGradient(listOf(Neutral17, Neutral10))
                        isRecording -> Brush.linearGradient(listOf(Coral40, Coral80))
                        else -> Brush.linearGradient(listOf(Neutral17, Neutral10))
                    }
                )
                .then(
                    if (isLoading) {
                        Modifier.border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(Coral40, Coral80)),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isLoading) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }

            when {
                isLoading -> LoadingSpinner()
                isRecording -> SoundwaveBars(bars)
                else -> IdleBubblePlaceholder()
            }
        }
    }
}
