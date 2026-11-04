package com.example.data.model

data class AppSettings(
    val themeAccent: String = "WARM_AMBER",
    val layoutDensity: String = "COMFORT",
    val hapticEnabled: Boolean = true,
    val backgroundSyncMinutes: Int = 15,
    val autoTrimCacheEnabled: Boolean = true,
    val silentTelemetryEnabled: Boolean = true,
    val appVersion: String = "v1.0.3",
    val buildId: String = "2026.11.04-prod"
)
