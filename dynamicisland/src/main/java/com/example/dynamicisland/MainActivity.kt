package com.example.dynamicisland

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val overlayGranted = mutableStateOf(false)
    private val overlayPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        overlayGranted.value = Settings.canDrawOverlays(this)
        if (overlayGranted.value) startIsland()
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayGranted.value = Settings.canDrawOverlays(this)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF101313)) {
                    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Dynamic Island", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text("Плавающий музыкальный виджет", modifier = Modifier.padding(top = 8.dp, bottom = 24.dp), color = Color.LightGray)
                        Button(onClick = { if (Settings.canDrawOverlays(this@MainActivity)) startIsland() else overlayPermission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }) { Text("Показать остров") }
                        Button(onClick = { stopService(Intent(this@MainActivity, DynamicIslandService::class.java)) }, modifier = Modifier.padding(top = 12.dp)) { Text("Остановить") }
                    }
                }
            }
        }
    }

    override fun onResume() { super.onResume(); overlayGranted.value = Settings.canDrawOverlays(this) }
    private fun startIsland() { ContextCompat.startForegroundService(this, Intent(this, DynamicIslandService::class.java)) }
}
