package com.glavatskikh.aiadvent2025

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform