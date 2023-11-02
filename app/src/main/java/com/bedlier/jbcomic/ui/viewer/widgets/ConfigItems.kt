package com.bedlier.jbcomic.ui.viewer.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bedlier.jbcomic.ui.theme.IconButtonStyle
import com.bedlier.jbcomic.ui.theme.Shapes
import com.bedlier.jbcomic.ui.theme.Typography

/**
 * An item with a icon at the top and a text at the bottom
 */
@Composable
fun ConfigItem(
    icon: ImageVector,
    onClick: () -> Unit = {},
    contentDescription: String = "",
    text: String? = null,
) {
    Column {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
        text?.let {
            Text(text = it)
        }
    }
}

@Composable
fun ConfigToggleItem(
    icon: ImageVector,
    checked: Boolean,
    text: String? = null,
    contentDescription: String = "",
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            
        }
        FilledIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(IconButtonStyle.ExtraLarge),
            shape = Shapes.extraLarge,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(
                IconButtonStyle.IconSize.ExtraLarge
            ))
        }
        Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
        text?.let {
            Text(text = it, style = Typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
