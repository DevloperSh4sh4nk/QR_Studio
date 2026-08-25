package com.example.qr

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color

enum class QrDotStyle(val label: String) {
    SQUARE("Classic Square"),
    ROUNDED("Smooth Rounded"),
    DOTS("Modern Dots"),
    SQUIRCLE("Squircle")
}

enum class QrEyeStyle(val label: String) {
    SQUARE("Square Frame"),
    ROUNDED("Rounded Frame"),
    CIRCLE("Circle Frame")
}

enum class LogoPreset(val label: String, val iconResName: String) {
    NONE("No Logo", ""),
    CUSTOM_URI("Custom Image", ""),
    STAR("Star Badge", "ic_star"),
    WEB("Website", "ic_web"),
    WIFI("Wi-Fi", "ic_wifi"),
    HEART("Heart", "ic_heart"),
    TECH("Technology", "ic_tech"),
    SHOP("Store / Shop", "ic_shop"),
    CODE("Developer", "ic_code"),
    SHARE("Connect / Social", "ic_share"),
    SHIELD("Verified / Security", "ic_shield"),
    BRIEFCASE("Business", "ic_business")
}

enum class GradientType(val label: String) {
    NONE("Solid"),
    DIAGONAL("Diagonal Gradient"),
    HORIZONTAL("Horizontal Gradient"),
    RADIAL("Radial Glow")
}

data class BrandPreset(
    val name: String,
    val description: String,
    val fgColorHex: String,
    val fgGradientHex: String? = null,
    val bgColorHex: String,
    val eyeColorHex: String,
    val dotStyle: QrDotStyle,
    val eyeStyle: QrEyeStyle,
    val logoPreset: LogoPreset = LogoPreset.NONE
)

object BrandPresets {
    val PRESETS = listOf(
        BrandPreset(
            name = "Cyber Neon",
            description = "Electric cyan on deep obsidian with rounded dots",
            fgColorHex = "#06B6D4",
            fgGradientHex = "#3B82F6",
            bgColorHex = "#0F172A",
            eyeColorHex = "#22D3EE",
            dotStyle = QrDotStyle.ROUNDED,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.TECH
        ),
        BrandPreset(
            name = "Royal Luxury",
            description = "Warm gold with dark slate elegance",
            fgColorHex = "#D97706",
            fgGradientHex = "#F59E0B",
            bgColorHex = "#18181B",
            eyeColorHex = "#FBBF24",
            dotStyle = QrDotStyle.SQUIRCLE,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.STAR
        ),
        BrandPreset(
            name = "Emerald Nature",
            description = "Lush botanical greens with soft circular dots",
            fgColorHex = "#059669",
            fgGradientHex = "#10B981",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#047857",
            dotStyle = QrDotStyle.DOTS,
            eyeStyle = QrEyeStyle.CIRCLE,
            logoPreset = LogoPreset.HEART
        ),
        BrandPreset(
            name = "Sunset Coral",
            description = "Vibrant rose-orange blend for social profiles",
            fgColorHex = "#E11D48",
            fgGradientHex = "#F97316",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#BE123C",
            dotStyle = QrDotStyle.ROUNDED,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.SHARE
        ),
        BrandPreset(
            name = "Corporate Navy",
            description = "Trustworthy deep indigo and cobalt for business",
            fgColorHex = "#1E3A8A",
            fgGradientHex = "#3B82F6",
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#1E40AF",
            dotStyle = QrDotStyle.SQUARE,
            eyeStyle = QrEyeStyle.SQUARE,
            logoPreset = LogoPreset.BRIEFCASE
        ),
        BrandPreset(
            name = "Monochrome Dark",
            description = "High contrast stark white on pure pitch dark",
            fgColorHex = "#FFFFFF",
            fgGradientHex = null,
            bgColorHex = "#000000",
            eyeColorHex = "#FFFFFF",
            dotStyle = QrDotStyle.ROUNDED,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.NONE
        ),
        BrandPreset(
            name = "Classic Minimal",
            description = "Crisp obsidian on pure white for universal clarity",
            fgColorHex = "#0F172A",
            fgGradientHex = null,
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#0F172A",
            dotStyle = QrDotStyle.ROUNDED,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.NONE
        )
    )
}

data class QrStyleConfig(
    val fgColorHex: String = "#0F172A",
    val fgGradientHex: String? = null,
    val gradientType: GradientType = GradientType.NONE,
    val bgColorHex: String = "#FFFFFF",
    val eyeColorHex: String = "#0F172A",
    val dotStyle: QrDotStyle = QrDotStyle.ROUNDED,
    val eyeStyle: QrEyeStyle = QrEyeStyle.ROUNDED,
    val logoPreset: LogoPreset = LogoPreset.NONE,
    val customLogoUri: String? = null,
    val logoSizePercent: Int = 20, // 15 to 28
    val brandLabel: String? = null,
    val brandSubtext: String? = null
) {
    fun parseFgColor(): Int = parseColorSafely(fgColorHex, AndroidColor.parseColor("#0F172A"))
    fun parseGradientColor(): Int? = fgGradientHex?.let { parseColorSafely(it, parseFgColor()) }
    fun parseBgColor(): Int = parseColorSafely(bgColorHex, AndroidColor.WHITE)
    fun parseEyeColor(): Int = parseColorSafely(eyeColorHex, parseFgColor())

    companion object {
        fun parseColorSafely(hex: String, defaultColor: Int): Int {
            return try {
                val cleaned = if (hex.startsWith("#")) hex else "#$hex"
                AndroidColor.parseColor(cleaned)
            } catch (e: Exception) {
                defaultColor
            }
        }
    }
}
