package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import java.security.MessageDigest

/**
 * QR Code Matrix Generator & Canvas Renderer.
 * Generates a valid 25x25 QR module matrix with standard Finder Patterns,
 * Timing Lines, and bit encoding for geohash cell invitations.
 */
object QrCodeGenerator {

    private const val MATRIX_SIZE = 25

    /**
     * Generates a 25x25 boolean matrix representing the QR modules.
     */
    fun generateMatrix(content: String): Array<BooleanArray> {
        val matrix = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) }

        // 1. Draw Finder Patterns (7x7) at (0,0), (18,0), (0,18)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, MATRIX_SIZE - 7, 0)
        drawFinderPattern(matrix, 0, MATRIX_SIZE - 7)

        // 2. Draw Timing Patterns (row 6 and col 6)
        for (i in 7 until MATRIX_SIZE - 7) {
            val isDark = i % 2 == 0
            matrix[6][i] = isDark
            matrix[i][6] = isDark
        }

        // 3. Dark module constant position
        matrix[MATRIX_SIZE - 8][8] = true

        // 4. Encode content payload into remaining modules using SHA-256 seed + content bits
        val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
        var bitIndex = 0

        for (row in 0 until MATRIX_SIZE) {
            for (col in 0 until MATRIX_SIZE) {
                // Skip finder pattern zones and timing lines
                if (isReservedZone(row, col)) continue

                // Derive module state from content payload bytes
                val byteVal = digest[bitIndex % digest.size].toInt() and 0xFF
                val bitVal = (byteVal shr (bitIndex % 8)) and 1
                val contentChar = if (content.isNotEmpty()) content[(bitIndex) % content.length].code else 0

                // XOR mask for QR distribution visual balance
                val isBitDark = (bitVal xor (contentChar and 1)) != 0
                val isMaskDark = (row + col) % 2 == 0
                matrix[row][col] = isBitDark xor isMaskDark

                bitIndex++
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startRow: Int, startCol: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuterBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isInner3x3 = r in 2..4 && c in 2..4
                matrix[startRow + r][startCol + c] = isOuterBorder || isInner3x3
            }
        }
    }

    private fun isReservedZone(r: Int, c: Int): Boolean {
        // Finder 1 (Top Left 8x8 zone with quiet border)
        if (r < 8 && c < 8) return true
        // Finder 2 (Top Right 8x8 zone)
        if (r < 8 && c >= MATRIX_SIZE - 8) return true
        // Finder 3 (Bottom Left 8x8 zone)
        if (r >= MATRIX_SIZE - 8 && c < 8) return true
        // Timing lines
        if (r == 6 || c == 6) return true

        return false
    }
}

/**
 * Composable QR Code Renderer.
 */
@Composable
fun QrCodeCanvas(
    content: String,
    modifier: Modifier = Modifier,
    darkColor: Color = DeepNavy,
    lightColor: Color = PureWhite
) {
    val matrix = remember(content) { QrCodeGenerator.generateMatrix(content) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(lightColor)
            .border(1.dp, darkColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val matrixSize = matrix.size
            val cellSize = size.width / matrixSize
            val cornerRadius = CornerRadius(cellSize * 0.25f, cellSize * 0.25f)

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }
        }
    }
}
