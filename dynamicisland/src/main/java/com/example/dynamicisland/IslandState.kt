package com.example.dynamicisland

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IslandState(val expanded: Boolean = false, val title: String = "Midnight City", val subtitle: String = "Dynamic Island", val playing: Boolean = true)

object IslandController {
    private val _state = MutableStateFlow(IslandState())
    val state = _state.asStateFlow()
    fun expand() { _state.value = _state.value.copy(expanded = true) }
    fun collapse() { _state.value = _state.value.copy(expanded = false) }
    fun togglePlay() { _state.value = _state.value.copy(playing = !_state.value.playing) }
}
