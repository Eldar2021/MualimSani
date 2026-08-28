package com.muallimsaniqr.flutter_app.ui.widgets.toolbars

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme

@Composable
fun BasicToolbar(title: String = "", modifier: Modifier = Modifier, textAlignment: TextAlign = TextAlign.Start) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, modifier = Modifier
            .padding(horizontal = 10.dp)
            .weight(1f), style = MaterialTheme.typography.titleLarge, textAlign = textAlignment)
    }
}

@Composable
fun NavigatedToolbar(title: String = "", @DrawableRes icon: Int = R.drawable.ic_back, modifier: Modifier = Modifier, textAlignment: TextAlign = TextAlign.Start, onNavigatedIconClick: () -> Unit = {}) {
    Row(modifier = modifier) {
        Icon(painter = icon.painter, contentDescription = "Артка", modifier = Modifier
            .padding(start = 4.dp)
            .size(24.dp)
            .clickable { onNavigatedIconClick() },
            tint = MaterialTheme.colorScheme.onSurface
        )
        BasicToolbar(title = title, textAlignment = textAlignment)
    }
}

@Preview
@Composable
fun BasicToolbarPreview() {
    MualimSaniTheme {
        BasicToolbar(title = "Home page")
    }
}

@Preview
@Composable
fun NavigatedToolbarPreview() {
    MualimSaniTheme {
        NavigatedToolbar(title = "Navigated page", textAlignment = TextAlign.Center)
    }
}

