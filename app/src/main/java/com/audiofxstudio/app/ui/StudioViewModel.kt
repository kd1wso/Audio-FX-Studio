package com.audiofxstudio.app.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.Transformer
import com.audiofxstudio.app.audio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class EffectType(val title:String){SPEED("Speed"),REVERB("Reverb"),EIGHT_D("8D Audio"),BASS("Bass Boost")}
data class EffectState(val type:EffectType,val enabled:Boolean=false,val value:Float=.5f)
data class StudioState(val uri:Uri?=null,val name:String="",val effects:List<EffectState> = EffectType.entries.map{EffectState(it)},val exporting:Boolean=false,val progress:Int=0,val output:File?=null)
class StudioViewModel:ViewModel(){
 private val _state=MutableStateFlow(StudioState());val state:StateFlow<StudioState> = _state.asStateFlow();val speed=SpeedProcessor();val reverb=ReverbProcessor();val bass=BassBoostProcessor();val eightD=EightDProcessor()
 fun select(c:Context,u:Uri){_state.value=_state.value.copy(uri=u,name=c.contentResolver.query(u,null,null,null,null)?.use{x->val k=x.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);if(x.moveToFirst()&&k>=0)x.getString(k)else"Audio file"}?:"Audio file")}
 fun toggle(t:EffectType){_state.value=_state.value.copy(effects=_state.value.effects.map{if(it.type==t)it.copy(enabled=!it.enabled)else it})}
 fun setValue(t:EffectType,v:Float){_state.value=_state.value.copy(effects=_state.value.effects.map{if(it.type==t)it.copy(value=v)else it});when(t){EffectType.SPEED->speed.speed=.5f+v*1.5f;EffectType.REVERB->reverb.wet=v;EffectType.BASS->bass.gainDb=v*15;EffectType.EIGHT_D->eightD.depth=v}}
 private fun processors()=_state.value.effects.filter{it.enabled}.map{when(it.type){EffectType.SPEED->speed;EffectType.REVERB->reverb;EffectType.BASS->bass;EffectType.EIGHT_D->eightD}}
 fun createPlayer(c:Context)=_state.value.uri?.let{u->androidx.media3.exoplayer.ExoPlayer.Builder(c).build().also{p->p.setMediaItem(MediaItem.fromUri(u));p.prepare()}}
 fun export(c:Context,d:File){val u=_state.value.uri?:return;viewModelScope.launch(Dispatchers.IO){_state.value=_state.value.copy(exporting=true);val item=EditedMediaItem.Builder(MediaItem.fromUri(u)).setEffects(Effects(processors(),emptyList())).build();Transformer.Builder(c).addListener(object:Transformer.Listener{override fun onCompleted(x:androidx.media3.transformer.Composition,r:androidx.media3.transformer.ExportResult){_state.value=_state.value.copy(exporting=false,progress=100,output=d)};override fun onError(x:androidx.media3.transformer.Composition,r:androidx.media3.transformer.ExportResult,e:ExportException){_state.value=_state.value.copy(exporting=false)}}).build().start(item,d.absolutePath)}}
}

