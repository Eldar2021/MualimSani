package com.muallimsaniqr.flutter_app.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.domain.model.Language
import com.muallimsaniqr.flutter_app.ui.extensions.strResource

@Composable
fun SelectLanguage(
    modifier: Modifier = Modifier,
    language: Language = Language.KYRGYZ,
    onLanguageSelected: (Language) -> Unit = {},
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(R.string.select_language.strResource, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
        NavigationDrawerItem(
            label = { Text(text = R.string.kyrgyz.strResource) },
            selected = language == Language.KYRGYZ,
            onClick = { onLanguageSelected(Language.KYRGYZ) }
        )
        NavigationDrawerItem(
            label = { Text(text = R.string.russian.strResource) },
            selected = language == Language.RUSSIAN,
            onClick = { onLanguageSelected(Language.RUSSIAN) }
        )
        NavigationDrawerItem(
            label = { Text(text = R.string.english.strResource) },
            selected = language == Language.ENGLISH,
            onClick = { onLanguageSelected(Language.ENGLISH) }
        )
        NavigationDrawerItem(
            label = { Text(text = R.string.uzbek.strResource) },
            selected = language == Language.UZBEK,
            onClick = { onLanguageSelected(Language.UZBEK) }
        )
    }

}