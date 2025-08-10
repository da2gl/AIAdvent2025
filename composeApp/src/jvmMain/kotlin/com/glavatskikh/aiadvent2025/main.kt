package com.glavatskikh.aiadvent2025

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AIAdvent2025",
    ) {
        App()
    }
}