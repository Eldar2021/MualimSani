package com.muallimsaniqr.flutter_app.ui.widgets.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaItem(
    text: String = "",
    icon: Painter? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Card(
        modifier = modifier,
        onClick = onClick
    ) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            icon?.let {
                Image(painter = icon, contentDescription = "Social media icon")
            }
            if (text.isNotEmpty())
                Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(10.dp))
        }

    }

}

@Composable
@Preview
fun SocialMediaItemPreview() {
    MualimSaniTheme {
        Column {
            SocialMediaItem(text = "Instagram", icon = R.drawable.ic_instagram.painter, modifier = Modifier.padding(10.dp))
            SocialMediaItem(text = "Whatsapp", icon = R.drawable.ic_whatsapp.painter, modifier = Modifier.padding(10.dp))
            SocialMediaItem(text = "Telegram", icon = R.drawable.ic_telegram.painter, modifier = Modifier.padding(10.dp))
            SocialMediaItem(icon = R.drawable.ic_telegram.painter, modifier = Modifier.padding(10.dp))
        }
    }
}