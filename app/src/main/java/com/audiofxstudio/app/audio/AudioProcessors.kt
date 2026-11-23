package com.audiofxstudio.app.audio

import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class SpeedProcessor:BaseAudioProcessor(){@Volatile var speed=1f;override fun queueInput(b:ByteBuffer){val i=b.order(ByteOrder.LITTLE_ENDIAN);val fb=2*inputAudioFormat.channelCount;val raw=ByteArray(i.remaining());i.get(raw);val o=replaceOutputBuffer(raw.size);val step=if(speed>=1)speed.toInt().coerceAtLeast(1)else 1;var f=0;var p=0;while(p+fb<=raw.size){if(speed<1||f++%step==0)o.put(raw,p,fb);p+=fb};o.flip()}}
class ReverbProcessor:BaseAudioProcessor(){@Volatile var wet=.35f;private var d=FloatArray(1);private var p=0;override fun onConfigure(f:AudioFormat):AudioFormat{d=FloatArray((f.sampleRate*.1f).toInt().coerceAtLeast(1)*f.channelCount);p=0;return f};override fun queueInput(b:ByteBuffer){val i=b.order(ByteOrder.LITTLE_ENDIAN);val o=replaceOutputBuffer(i.remaining());while(i.hasRemaining()){val s=i.short.toFloat()/32768;val e=d[p%d.size];d[p%d.size]=s+e*.35f;p++;o.putShort(((s*(1-wet)+e*wet).coerceIn(-1f,1f)*32767).toInt().toShort())};o.flip()}}
class BassBoostProcessor:BaseAudioProcessor(){@Volatile var gainDb=0f;private var prev=0f;override fun queueInput(b:ByteBuffer){val i=b.order(ByteOrder.LITTLE_ENDIAN);val o=replaceOutputBuffer(i.remaining());val g=exp(gainDb/20f*2.302585f);while(i.hasRemaining()){val s=i.short.toFloat()/32768;val low=prev+(s-prev)*.08f;prev=low;o.putShort((s+(low*g-low)).coerceIn(-1f,1f).times(32767).toInt().toShort())};o.flip()}}
class EightDProcessor:BaseAudioProcessor(){@Volatile var rotationHz=.12f;@Volatile var depth=.8f;private var n=0L;private var rate=44100;override fun onConfigure(f:AudioFormat):AudioFormat{rate=f.sampleRate;n=0;return f};override fun queueInput(b:ByteBuffer){val i=b.order(ByteOrder.LITTLE_ENDIAN);val o=replaceOutputBuffer(i.remaining());val c=inputAudioFormat.channelCount;while(i.remaining()>=2*c){val l=i.short.toFloat()/32768;val r=if(c>1)i.short.toFloat()/32768 else l;val pan=(sin(2*PI*rotationHz*n++/rate).toFloat()*depth).coerceIn(-1f,1f);o.putShort((l*sqrt((1-pan)/2)*32767).toInt().toShort());if(c>1)o.putShort((r*sqrt((1+pan)/2)*32767).toInt().toShort())};o.flip()}}

