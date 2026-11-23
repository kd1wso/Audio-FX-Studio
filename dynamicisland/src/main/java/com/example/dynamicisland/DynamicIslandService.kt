package com.example.dynamicisland

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class DynamicIslandService : LifecycleService() {
    private lateinit var wm: WindowManager
    private lateinit var owner: OverlayOwner
    private var view: ComposeView? = null
    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        val channel = NotificationChannel("island", "Dynamic Island", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "island").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Dynamic Island активен").setContentText("Плавающий виджет запущен").setOngoing(true).build()
        val type = if (Build.VERSION.SDK_INT >= 34) android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, 100, notification, type)
        owner = OverlayOwner().also { it.create() }
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = (38 * resources.displayMetrics.density).toInt() }
        view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner); setViewTreeViewModelStoreOwner(owner); setViewTreeSavedStateRegistryOwner(owner)
            setContent { MaterialTheme { val state by IslandController.state.collectAsState(); DynamicIslandOverlay(state, IslandController::expand, IslandController::collapse, IslandController::togglePlay) } }
        }
        try { wm.addView(view, params) } catch (_: Exception) { stopSelf() }
    }
    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }
    override fun onDestroy() { view?.let { it.disposeComposition(); runCatching { wm.removeView(it) } }; if (::owner.isInitialized) owner.destroy(); super.onDestroy() }
}

private class OverlayOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val models = ViewModelStore()
    private val saved = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore: ViewModelStore get() = models
    override val savedStateRegistry: SavedStateRegistry get() = saved.savedStateRegistry
    fun create() { saved.performRestore(null); registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE); registry.handleLifecycleEvent(Lifecycle.Event.ON_START); registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
    fun destroy() { registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE); registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP); registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY); models.clear() }
}
