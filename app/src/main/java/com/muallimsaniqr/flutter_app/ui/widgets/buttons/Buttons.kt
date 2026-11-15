package com.muallimsaniqr.flutter_app.ui.widgets.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme

@Composable
fun MualimButton(
    text: String = "",
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.elevatedShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
    ),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    onClick: () -> Unit = {}
) {


    Button(onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border
        ) {
        // Uzun metin iki satira kirildiginda da ortali kalsin.
        Text(text = text, style = textStyle, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
fun MualimButtonPreview() {
    MualimSaniTheme {
        MualimButton(text = "Button")
    }
}
