package com.audiofxstudio.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable fun AudioFxTheme(dark: Boolean = true, content: @Composable () -> Unit) { MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=Color(0xFFA5D6A7), background=Color(0xFF101412), surface=Color(0xFF18201B)) else lightColorScheme(primary=Color(0xFF356B3B)), content=content) }

