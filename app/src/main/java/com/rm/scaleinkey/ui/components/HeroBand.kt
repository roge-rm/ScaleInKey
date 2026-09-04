package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The gradient title header, shared by both top-level screens. Runs edge-to-edge behind the
 * status bar (see enableEdgeToEdge in MainActivity) rather than sitting below a system-colored
 * bar — the background/clip apply before [Modifier.windowInsetsPadding], so only the content
 * gets pushed down. [switchIcon] is a single small glyph (kept to 1-2 characters — this is a
 * compact icon button, not a label) shown in a circular button anchored to the trailing edge; the
 * title stays visually centered via the outer [Box]'s own [Alignment.Center], since only the
 * toggle sets its own [Modifier.align].
 */
@Composable
fun HeroBand(
    onSwitchScreen: () -> Unit,
    switchIcon: String,
    switchContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ScaleInKey",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Surface(
            onClick = onSwitchScreen,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(32.dp)
                .semantics { contentDescription = switchContentDescription },
            shape = CircleShape,
            // A translucent tint of onPrimary, not the primaryContainer color the other small
            // circular icon-buttons in this app use — this one sits directly on the hero band's
            // own gradient rather than a neutral surface, so it needs to read against either end
            // of the primary->tertiary gradient rather than against the plain background.
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(switchIcon, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
