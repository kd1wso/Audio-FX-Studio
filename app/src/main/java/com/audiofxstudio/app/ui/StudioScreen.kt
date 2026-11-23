package com.audiofxstudio.app.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun StudioScreen(vm: StudioViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); vm.select(context, it) } }
    Surface(Modifier.fillMaxSize()) {
        if (state.uri == null) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("Audio FX Studio", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { open.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Выбрать аудиофайл") }
            }
        } else Editor(state, vm, context)
    }
}

@Composable
private fun Editor(state: StudioState, vm: StudioViewModel, context: android.content.Context) {
    var player by remember { mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")) { uri -> uri?.let { target -> state.output?.inputStream()?.use { input -> context.contentResolver.openOutputStream(target)?.use(input::copyTo) } } }
    DisposableEffect(state.uri) { player = vm.createPlayer(context); onDispose { player?.release() } }
    LaunchedEffect(player) { while (true) { playing = player?.isPlaying == true; delay(200) } }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(state.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (player?.isPlaying == true) player?.pause() else player?.play() }) { Text(if (playing) "Пауза" else "Воспроизвести") }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.effects) { effect ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(effect.type.title); Switch(effect.enabled, { vm.toggle(effect.type) }) }
                        if (effect.enabled) Slider(effect.value, { vm.setValue(effect.type, it) })
                    }
                }
            }
        }
        if (state.exporting) LinearProgressIndicator(Modifier.fillMaxWidth())
        Button(onClick = { vm.export(context, context.cacheDir.resolve("audio.wav")) }, enabled = !state.exporting, modifier = Modifier.fillMaxWidth()) { Text(if (state.exporting) "Рендеринг ${state.progress}%" else "Экспортировать WAV") }
        if (state.output != null) OutlinedButton(onClick = { save.launch("audio-fx-result.wav") }, modifier = Modifier.fillMaxWidth()) { Text("Сохранить результат") }
    }
}

