package com.glavatskikh.aiadvent2025

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.glavatskikh.aiadvent2025.chat.presentation.components.ChatScreen
import com.glavatskikh.aiadvent2025.theme.LocalThemeManager
import com.glavatskikh.aiadvent2025.theme.ThemeManager
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val themeManager = remember { ThemeManager() }
    
    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        val isDarkTheme = themeManager.isDarkTheme()
        
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
        ) {
            ChatScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}