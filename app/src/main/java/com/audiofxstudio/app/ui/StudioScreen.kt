@Composable private fun Editor(state: StudioState, vm: StudioViewModel, save: (String)->Unit)
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable fun StudioScreen(vm: StudioViewModel) {
    val state by vm.state.collectAsStateWithLifecycle(); val context=LocalContext.current
    val open=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri -> uri?.let { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); vm.select(context,it) } }
    val save=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("audio/wav")){ uri -> uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> state.output?.inputStream()?.use { input -> input.copyTo(out) } } } }
    Surface(Modifier.fillMaxSize()) { if(state.uri==null) EmptyState { open.launch(arrayOf("audio/mpeg","audio/flac","audio/wav","audio/ogg","audio/mp4","audio/aac")) } else Editor(state,vm,save) }
}

@Composable private fun EmptyState(onOpen:()->Unit) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.Center) { Text("Audio FX Studio", style=MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); Text("Локальный редактор аудио", color=MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(32.dp)); Button(onClick=onOpen, modifier=Modifier.fillMaxWidth().height(56.dp)){ Text("Выбрать аудиофайл") }; Spacer(Modifier.height(16.dp)); Text("MP3  ·  FLAC  ·  WAV  ·  OGG  ·  M4A  ·  AAC", style=MaterialTheme.typography.labelMedium) } }

@Composable private fun Editor(state: StudioState, vm: StudioViewModel, save: (String)->Unit) { val context=LocalContext.current; var player by remember { mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null) }; DisposableEffect(state.uri) { player=vm.createPlayer(context); onDispose { player?.release(); player=null } }; Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) { Row(Modifier.fillMaxWidth().padding(vertical=16.dp), horizontalArrangement=Arrangement.SpaceBetween){ Column(Modifier.weight(1f)){ Text(state.name, style=MaterialTheme.typography.titleLarge, maxLines=1); Text("Локальный файл", style=MaterialTheme.typography.labelMedium) }; OutlinedButton(onClick={}){Text("Заменить")} }; Card(Modifier.fillMaxWidth()){ Column(Modifier.padding(16.dp)){ Text("Предпрослушивание", style=MaterialTheme.typography.titleMedium); Spacer(Modifier.height(12.dp)); LinearProgressIndicator({ if(player?.duration?:0>0) (player?.currentPosition?:0).toFloat()/(player?.duration?:1) else 0f }, Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.Center){ Button(onClick={ if(player?.isPlaying==true)player?.pause() else player?.play() }){Text(if(player?.isPlaying==true)"Пауза" else "Воспроизвести")} } } }; Spacer(Modifier.height(12.dp)); Text("Пресеты", style=MaterialTheme.typography.titleMedium); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)){ listOf("Nightcore","Slowed+Reverb","8D","Bass Boost","Сброс").forEach { AssistChip(onClick={applyPreset(it,vm)}, label={Text(it)}) } }; Spacer(Modifier.height(8.dp)); LazyColumn(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(8.dp)){ items(state.effects){ effect -> EffectRow(effect,vm) } }; if(state.exporting) LinearProgressIndicator(Modifier.fillMaxWidth()); Button(onClick={ save("audio.wav") }, enabled=!state.exporting, modifier=Modifier.fillMaxWidth().padding(vertical=12.dp)){ Text(if(state.exporting)"Рендеринг ${state.progress}%" else "Экспортировать WAV") } } }

@Composable private fun EffectRow(effect: EffectState, vm: StudioViewModel) { Card(Modifier.fillMaxWidth()){ Column(Modifier.padding(12.dp)){ Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text(effect.type.title, style=MaterialTheme.typography.titleMedium); Switch(effect.enabled,{vm.toggle(effect.type)}) }; if(effect.enabled && effect.type!=EffectType.SPEED){ Slider(effect.value,{vm.setValue(effect.type,it)},Modifier.fillMaxWidth()); Text(if(effect.type==EffectType.BASS)"${(effect.value*15).toInt()} dB" else "${(effect.value*100).toInt()}%", style=MaterialTheme.typography.labelSmall) } } } }
private fun applyPreset(name:String,vm:StudioViewModel){ val types=when(name){"8D"->listOf(EffectType.EIGHT_D);"Bass Boost"->listOf(EffectType.BASS);"Slowed+Reverb"->listOf(EffectType.REVERB);"Nightcore"->listOf(EffectType.SPEED);else->emptyList()}; types.forEach { vm.toggle(it) } }

