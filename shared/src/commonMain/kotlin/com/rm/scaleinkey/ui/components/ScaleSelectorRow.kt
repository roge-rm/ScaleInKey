package com.rm.scaleinkey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rm.scaleinkey.music.CANONICAL_ROOTS
import com.rm.scaleinkey.music.ScaleGroup
import com.rm.scaleinkey.music.ScaleType

@Composable
fun ScaleSelectorRow(
    rootIndex: Int,
    scaleType: ScaleType,
    onRootSelected: (Int) -> Unit,
    onScaleTypeSelected: (ScaleType) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Two-level picker (Category, then Scale within it) rather than one dropdown per
    // ScaleGroup — that pattern would need a new dropdown added to this row every time a
    // scale category is added, which doesn't scale as the scale library grows.
    val scalesByGroup = remember { ScaleType.entries.groupBy { it.group } }
    val groups = remember { scalesByGroup.keys.toList() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabeledDropdown(
            label = "Root",
            selectedText = CANONICAL_ROOTS[rootIndex].displayName(),
            options = CANONICAL_ROOTS.indices.toList(),
            optionText = { index -> CANONICAL_ROOTS[index].displayName() },
            onOptionSelected = onRootSelected,
            isSelected = { index -> index == rootIndex },
            modifier = Modifier.weight(0.7f),
        )
        LabeledDropdown(
            label = "Category",
            selectedText = scaleType.group.displayName,
            options = groups,
            optionText = { it.displayName },
            onOptionSelected = { group ->
                scalesByGroup.getValue(group).first().let(onScaleTypeSelected)
            },
            isSelected = { it == scaleType.group },
            modifier = Modifier.weight(1.15f),
        )
        LabeledDropdown(
            label = "Scale",
            selectedText = scaleType.shortDisplayName,
            options = scalesByGroup.getValue(scaleType.group),
            optionText = { it.displayName },
            onOptionSelected = onScaleTypeSelected,
            isSelected = { it == scaleType },
            modifier = Modifier.weight(1.15f),
        )
    }
}
