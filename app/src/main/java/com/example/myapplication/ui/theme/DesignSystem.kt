package com.example.myapplication.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * BizEng Design System
 * Consistent spacing, colors, and component styles across the app
 */
object BizEngDesign {

    // ==================== SPACING ====================
    object Spacing {
        val sectionVertical = 24.dp      // Between major sections
        val elementVertical = 16.dp      // Between elements in a section
        val small = 12.dp                // Small gaps
        val tiny = 8.dp                  // Minimal spacing
        val large = 32.dp                // Extra space

        val cardPadding = 20.dp          // Internal card padding
        val screenPadding = 16.dp        // Screen edge padding
    }

    // ==================== COLORS ====================
    // Use MaterialTheme colors, but define semantic meanings
    object Colors {
        // Already defined in theme, just document usage:
        // Primary → Purple accent (buttons, highlights)
        // Surface → White cards
        // Background → Pale lavender
        // Error → Red-orange
        // Success → Use primary for now

        val divider = Color(0xFFE0E0E0)
        val textSecondary = Color(0xFF757575)
    }

    // ==================== SHAPES ====================
    object Shapes {
        val card = RoundedCornerShape(16.dp)
        val button = RoundedCornerShape(12.dp)
        val chip = RoundedCornerShape(20.dp)
        val small = RoundedCornerShape(8.dp)
    }

    // ==================== ELEVATIONS ====================
    object Elevations {
        val card = 2.dp
        val cardHovered = 4.dp
        val none = 0.dp
    }

    // ==================== CARD STYLES ====================
    @Composable
    fun standardCardElevation(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Elevations.card,
        pressedElevation = Elevations.cardHovered,
        hoveredElevation = Elevations.cardHovered
    )

    fun Modifier.sectionSpacing() = this.padding(vertical = Spacing.sectionVertical)
    fun Modifier.elementSpacing() = this.padding(vertical = Spacing.elementVertical)
}

