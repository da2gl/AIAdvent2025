package com.glavatskikh.aiadvent2025

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.glavatskikh.aiadvent2025.chat.presentation.components.ChatScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        ChatScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}