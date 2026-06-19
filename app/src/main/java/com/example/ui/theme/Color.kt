package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Editorial Aesthetic Theme Cohesive Colors
val EditorialDeepBlue = Color(0xFF001D36)     // Deep rich navy
val EditorialBlue = Color(0xFF0061A4)         // Elegant primary blue
val EditorialLightBlue = Color(0xFFD3E2FF)    // Light blue accent container
val EditorialProgressBlue = Color(0xFFB1C5FF) // Vibrant light-blue meter / highlight
val EditorialGray = Color(0xFFE1E2EC)         // Custom slate card bg
val EditorialLightGray = Color(0xFFF1F4F9)    // Custom soft row bg
val EditorialWhite = Color(0xFFFFFFFF)

// Core mapping
val LightPrimary = EditorialBlue
val LightSecondary = EditorialDeepBlue
val LightTertiary = EditorialProgressBlue
val LightBackground = Color(0xFFF7F9FC)
val LightSurface = EditorialWhite
val LightOnSurface = Color(0xFF1A1C1E)

// Dark Counterpart for matching the beautiful high-contrast look
val DarkPrimary = EditorialProgressBlue
val DarkSecondary = EditorialLightBlue
val DarkTertiary = EditorialBlue
val DarkBackground = EditorialDeepBlue
val DarkSurface = Color(0xFF0C2B47) // Matching dark navy surface
val DarkOnSurface = Color(0xFFF7F9FC)

// Accents & Priorities (mapped from spec or polished beautifully)
val HighPriorityColor = Color(0xFFEF4444) // Bright Soft Red
val MediumPriorityColor = Color(0xFFF59E0B) // Amber Gold
val LowPriorityColor = Color(0xFF3B82F6) // Electric Blue
val CompletedColor = Color(0xFF10B981) // Energetic Green

// Spec priority badge colors
val HighPriorityBg = Color(0xFFFFDAD6)
val HighPriorityText = Color(0xFF410002)
val NormalBadgeBg = Color(0xFFC2E8FF)
val NormalBadgeText = Color(0xFF001E2E)
