package com.example.dynamicisland

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DynamicIslandOverlay(state: IslandState, onExpand: () -> Unit, onCollapse: () -> Unit, onTogglePlay: () -> Unit) {
    Surface(modifier = Modifier.animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)), shape = RoundedCornerShape(if (state.expanded) 26.dp else 22.dp), color = Color(0xFF111316), shadowElevation = 12.dp) {
        if (state.expanded) {
            Column(Modifier.width(330.dp).pointerInput(Unit) { var distance = 0f; detectVerticalDragGestures(onVerticalDrag = { _, amount -> distance += amount }, onDragEnd = { if (distance > 50f) onCollapse(); distance = 0f }) }.padding(16.dp)) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(38.dp, 4.dp).background(Color(0x44666666), RoundedCornerShape(2.dp)))
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).background(Color(0xFF2B6D54), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Icon(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(state.title, color = Color.White, fontWeight = FontWeight.Bold); Text(state.subtitle, color = Color(0xB3FFFFFF)) }
                    IconButton(onClick = onCollapse) { Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.LightGray) }
                }
                Spacer(Modifier.height(16.dp))
                Surface(onClick = onTogglePlay, modifier = Modifier.size(46.dp).align(Alignment.CenterHorizontally), shape = CircleShape, color = Color.White) { Box(contentAlignment = Alignment.Center) { Icon(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Воспроизведение", tint = Color.Black) } }
            }
        } else {
            Row(Modifier.size(156.dp, 40.dp).pointerInput(Unit) { detectTapGestures { onExpand() } }.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(Modifier.size(9.dp).background(Color(0xFF65E69A), CircleShape)); Spacer(Modifier.width(8.dp)); Text(state.title, color = Color.White, maxLines = 1)
            }
        }
    }
}
