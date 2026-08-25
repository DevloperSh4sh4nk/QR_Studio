package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_records")
data class QrRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "GENERATED" or "SCANNED"
    val contentType: String, // "URL", "TEXT", "WIFI", "CONTACT", "EMAIL", "PHONE", "SMS", "SOCIAL"
    val title: String,
    val content: String,
    val metadataJson: String = "{}",
    val fgColorHex: String = "#0F172A",
    val bgColorHex: String = "#FFFFFF",
    val eyeColorHex: String = "#0F172A",
    val stylePattern: String = "ROUNDED", // "SQUARE", "ROUNDED", "DOTS", "SQUIRCLE"
    val eyeStyle: String = "ROUNDED", // "SQUARE", "ROUNDED", "CIRCLE"
    val logoPreset: String = "NONE", // "NONE", "CUSTOM_URI", "STAR", "WEB", "WIFI", "HEART", "TECH", "SHOP", "CODE", "SHARE", "SHIELD"
    val customLogoUri: String? = null,
    val logoSizePercent: Int = 20, // 15 to 28
    val brandLabel: String? = null,
    val brandSubtext: String? = null,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
