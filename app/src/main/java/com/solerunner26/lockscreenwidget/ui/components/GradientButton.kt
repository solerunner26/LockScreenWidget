package com.solerunner26.lockscreenwidget.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.solerunner26.lockscreenwidget.ui.theme.QuickLockTheme

/**
 * The single, uniform primary-button style used everywhere in the app:
 * full-width pill filled with the active palette's two-tone gradient, with a soft
 * press-scale for tactile feedback.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(), label = "press")
    val brush: Brush = if (enabled) {
        QuickLockTheme.extras.gradient
    } else {
        Brush.linearGradient(listOf(Color(0xFF8A8496), Color(0xFF8A8496)))
    }
    Row(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(brush)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}
