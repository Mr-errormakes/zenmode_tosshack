package com.zenlauncher.zenmode.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.zenlauncher.zenmode.DistractingAppsRepository

/**
 * Utility that converts distracting app icons to a monochrome zen-green tint,
 * stripping the original colorful branding to reduce visual addiction triggers.
 *
 * Non-distracting app icons pass through unchanged.
 *
 * Lives in its own file to minimize merge conflict surface with HomeScreen.kt.
 */
object ZenIconTint {

    // ZenMode brand green (#00FF41) in RGB
    private const val ZEN_GREEN_R = 0f / 255f   // 0.0
    private const val ZEN_GREEN_G = 255f / 255f  // 1.0
    private const val ZEN_GREEN_B = 65f / 255f   // ~0.255

    /**
     * Returns true if [packageName] belongs to a distracting app (social, video, game,
     * or user-selected).
     */
    fun isDistracting(context: Context, packageName: CharSequence): Boolean {
        return try {
            DistractingAppsRepository.isDistracting(
                context,
                context.packageManager,
                packageName.toString()
            )
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Applies a monochrome zen-green tint to a [Drawable].
     *
     * Process:
     * 1. Convert to grayscale (desaturate).
     * 2. Map the grayscale luminance to a zen-green color channel output.
     *
     * The result is a drawable that retains its shape/silhouette but rendered
     * entirely in the ZenMode brand green palette.
     */
    fun toMonochromeZen(drawable: Drawable, sizePx: Int = 128): Drawable {
        // Convert drawable to bitmap
        val originalBitmap = drawable.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

        // Create output bitmap
        val monoBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(monoBitmap)

        // ColorMatrix: first desaturate, then tint to zen green
        // Desaturation matrix converts RGB → grayscale luminance
        val desatMatrix = ColorMatrix()
        desatMatrix.setSaturation(0f)

        // Tint matrix: multiply grayscale output by zen green channel values
        // This takes the grayscale luminance and remaps it into zen-green tones
        val tintMatrix = ColorMatrix(floatArrayOf(
            ZEN_GREEN_R, 0f, 0f, 0f, 0f,   // R = luminance * green_r
            0f, ZEN_GREEN_G, 0f, 0f, 0f,   // G = luminance * green_g
            0f, 0f, ZEN_GREEN_B, 0f, 0f,   // B = luminance * green_b
            0f, 0f, 0f, 1f, 0f             // A = alpha preserved
        ))

        // Chain: desaturate first, then apply green tint
        tintMatrix.preConcat(desatMatrix)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(tintMatrix)
            isAntiAlias = true
        }

        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        return BitmapDrawable(android.content.res.Resources.getSystem(), monoBitmap)
    }

    /**
     * Convenience: if the app is distracting, return a monochrome zen-green icon;
     * otherwise return the original icon unchanged.
     */
    fun processIcon(context: Context, packageName: CharSequence, icon: Drawable, sizePx: Int = 128): Drawable {
        return if (isDistracting(context, packageName)) {
            toMonochromeZen(icon, sizePx)
        } else {
            icon
        }
    }
}
