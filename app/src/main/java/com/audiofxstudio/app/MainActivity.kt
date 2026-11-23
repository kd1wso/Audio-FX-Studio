package com.audiofxstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.audiofxstudio.app.ui.AudioFxTheme
import com.audiofxstudio.app.ui.StudioScreen
import com.audiofxstudio.app.ui.StudioViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<StudioViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AudioFxTheme { StudioScreen(viewModel) } } }
}

