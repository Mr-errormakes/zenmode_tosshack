package com.zenlauncher.zenmode.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zenlauncher.zenmode.ui.theme.ZenTheme

/**
 * 18-Glyph ZenMode Design System Vector Icon Suite
 * Powered by tokens.json foundations & ZenTheme color primitives.
 */

@Composable
fun ZenIconBase(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand,
    strokeWidth: Dp = 1.7.dp,
    drawPath: (Path, Float, Float) -> Unit
) {
    Canvas(modifier = modifier.size(size)) {
        val path = Path()
        drawPath(path, size.toPx(), size.toPx())
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// 1. IconHome
@Composable
fun ZenIconHome(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.125f, h * 0.45f)
        path.lineTo(w * 0.5f, h * 0.125f)
        path.lineTo(w * 0.875f, h * 0.45f)
        path.lineTo(w * 0.875f, h * 0.875f)
        path.lineTo(w * 0.125f, h * 0.875f)
        path.close()
    }
}

// 2. IconSearch
@Composable
fun ZenIconSearch(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textSecondary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.15f, w * 0.65f, h * 0.65f))
        path.moveTo(w * 0.52f, h * 0.52f)
        path.lineTo(w * 0.85f, h * 0.85f)
    }
}

// 3. IconMessage
@Composable
fun ZenIconMessage(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.125f, h * 0.2f)
        path.lineTo(w * 0.875f, h * 0.2f)
        path.lineTo(w * 0.875f, h * 0.7f)
        path.lineTo(w * 0.4f, h * 0.7f)
        path.lineTo(w * 0.2f, h * 0.85f)
        path.lineTo(w * 0.2f, h * 0.7f)
        path.lineTo(w * 0.125f, h * 0.7f)
        path.close()
    }
}

// 4. IconComment
@Composable
fun ZenIconComment(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textSecondary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.15f, h * 0.25f)
        path.lineTo(w * 0.85f, h * 0.25f)
        path.lineTo(w * 0.85f, h * 0.75f)
        path.lineTo(w * 0.15f, h * 0.75f)
        path.close()
    }
}

// 5. IconHeart
@Composable
fun ZenIconHeart(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFFF16351)
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.5f, h * 0.85f)
        path.cubicTo(w * 0.15f, h * 0.6f, w * 0.05f, h * 0.35f, w * 0.25f, h * 0.18f)
        path.cubicTo(w * 0.4f, h * 0.05f, w * 0.5f, h * 0.25f, w * 0.5f, h * 0.25f)
        path.cubicTo(w * 0.5f, h * 0.25f, w * 0.6f, h * 0.05f, w * 0.75f, h * 0.18f)
        path.cubicTo(w * 0.95f, h * 0.35f, w * 0.85f, h * 0.6f, w * 0.5f, h * 0.85f)
    }
}

// 6. IconAdd
@Composable
fun ZenIconAdd(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.actionPrimary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.1f, h * 0.1f, w * 0.9f, h * 0.9f))
        path.moveTo(w * 0.5f, h * 0.3f)
        path.lineTo(w * 0.5f, h * 0.7f)
        path.moveTo(w * 0.3f, h * 0.5f)
        path.lineTo(w * 0.7f, h * 0.5f)
    }
}

// 7. IconClose
@Composable
fun ZenIconClose(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textPrimary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.2f, h * 0.2f)
        path.lineTo(w * 0.8f, h * 0.8f)
        path.moveTo(w * 0.8f, h * 0.2f)
        path.lineTo(w * 0.2f, h * 0.8f)
    }
}

// 8. IconMore
@Composable
fun ZenIconMore(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textSecondary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.2f, h * 0.45f, w * 0.3f, h * 0.55f))
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.45f, h * 0.45f, w * 0.55f, h * 0.55f))
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.7f, h * 0.45f, w * 0.8f, h * 0.55f))
    }
}

// 9. IconVerified
@Composable
fun ZenIconVerified(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.1f, h * 0.1f, w * 0.9f, h * 0.9f))
        path.moveTo(w * 0.3f, h * 0.5f)
        path.lineTo(w * 0.45f, h * 0.65f)
        path.lineTo(w * 0.7f, h * 0.35f)
    }
}

// 10. Lock
@Composable
fun ZenIconLock(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.borderFocus
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.3f, h * 0.45f)
        path.lineTo(w * 0.3f, h * 0.3f)
        path.cubicTo(w * 0.3f, h * 0.15f, w * 0.7f, h * 0.15f, w * 0.7f, h * 0.3f)
        path.lineTo(w * 0.7f, h * 0.45f)
        path.addRect(androidx.compose.ui.geometry.Rect(w * 0.2f, h * 0.45f, w * 0.8f, h * 0.85f))
    }
}

// 11. Fire
@Composable
fun ZenIconFire(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFFEBDE27)
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.5f, h * 0.15f)
        path.cubicTo(w * 0.75f, h * 0.45f, w * 0.85f, h * 0.65f, w * 0.75f, h * 0.8f)
        path.cubicTo(w * 0.65f, h * 0.95f, w * 0.35f, h * 0.95f, w * 0.25f, h * 0.8f)
        path.cubicTo(w * 0.15f, h * 0.65f, w * 0.25f, h * 0.45f, w * 0.5f, h * 0.15f)
    }
}

// 12. Coin
@Composable
fun ZenIconCoin(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFFB9E234)
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.1f, h * 0.1f, w * 0.9f, h * 0.9f))
        path.moveTo(w * 0.45f, h * 0.3f)
        path.lineTo(w * 0.55f, h * 0.3f)
        path.moveTo(w * 0.5f, h * 0.3f)
        path.lineTo(w * 0.5f, h * 0.7f)
    }
}

// 13. Bolt
@Composable
fun ZenIconBolt(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.borderFocus
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.55f, h * 0.1f)
        path.lineTo(w * 0.2f, h * 0.55f)
        path.lineTo(w * 0.5f, h * 0.55f)
        path.lineTo(w * 0.45f, h * 0.9f)
        path.lineTo(w * 0.8f, h * 0.45f)
        path.lineTo(w * 0.5f, h * 0.45f)
        path.close()
    }
}

// 14. Copy
@Composable
fun ZenIconCopy(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addRect(androidx.compose.ui.geometry.Rect(w * 0.35f, h * 0.35f, w * 0.85f, h * 0.85f))
        path.moveTo(w * 0.65f, h * 0.35f)
        path.lineTo(w * 0.65f, h * 0.15f)
        path.lineTo(w * 0.15f, h * 0.15f)
        path.lineTo(w * 0.15f, h * 0.65f)
        path.lineTo(w * 0.35f, h * 0.65f)
    }
}

// 15. Delete
@Composable
fun ZenIconDelete(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFFF16351)
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.2f, h * 0.25f)
        path.lineTo(w * 0.8f, h * 0.25f)
        path.moveTo(w * 0.3f, h * 0.25f)
        path.lineTo(w * 0.35f, h * 0.85f)
        path.lineTo(w * 0.65f, h * 0.85f)
        path.lineTo(w * 0.7f, h * 0.25f)
    }
}

// 16. ArrowBack
@Composable
fun ZenIconArrowBack(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textPrimary
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.moveTo(w * 0.8f, h * 0.5f)
        path.lineTo(w * 0.2f, h * 0.5f)
        path.lineTo(w * 0.45f, h * 0.25f)
        path.moveTo(w * 0.2f, h * 0.5f)
        path.lineTo(w * 0.45f, h * 0.75f)
    }
}

// 17. Accessibility
@Composable
fun ZenIconAccessibility(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.4f, h * 0.1f, w * 0.6f, h * 0.3f))
        path.moveTo(w * 0.2f, h * 0.4f)
        path.lineTo(w * 0.8f, h * 0.4f)
        path.moveTo(w * 0.5f, h * 0.4f)
        path.lineTo(w * 0.5f, h * 0.65f)
        path.lineTo(w * 0.35f, h * 0.9f)
        path.moveTo(w * 0.5f, h * 0.65f)
        path.lineTo(w * 0.65f, h * 0.9f)
    }
}

// 18. Settings
@Composable
fun ZenIconSettings(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ZenTheme.colors.textBrand
) {
    ZenIconBase(modifier = modifier, size = size, tint = tint) { path, w, h ->
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.3f, h * 0.3f, w * 0.7f, h * 0.7f))
        path.addOval(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.15f, w * 0.85f, h * 0.85f))
    }
}
