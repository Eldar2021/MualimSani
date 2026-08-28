package com.muallimsaniqr.flutter_app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.ui.extensions.openInstagramByUserName
import com.muallimsaniqr.flutter_app.ui.extensions.openTelegramByUserName
import com.muallimsaniqr.flutter_app.ui.extensions.openWhatsappByLink
import com.muallimsaniqr.flutter_app.ui.extensions.painter
import com.muallimsaniqr.flutter_app.ui.extensions.strResource
import com.muallimsaniqr.flutter_app.ui.theme.MualimSaniTheme
import com.muallimsaniqr.flutter_app.ui.widgets.buttons.MualimButton
import com.muallimsaniqr.flutter_app.ui.widgets.cards.SocialMediaItem
import com.muallimsaniqr.flutter_app.utils.constants.CommonConstants

@Composable
fun OnboardingScreen(onHomeClick: () -> Unit = {}) {

    val vm: OnboardingVM = hiltViewModel()

    OnboardingScreenContent {
        vm.setOnboardingShown(true)
        onHomeClick()
    }

}

@Composable
fun OnboardingScreenContent(onHomeClick: () -> Unit = {}) {
    val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = R.drawable.image_book_cover.painter,
                contentDescription = "Logo",
                modifier = Modifier.weight(1f)
            )

            Text(
                text = R.string.onboarding_title.strResource,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp)
            )

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                SocialMediaItem(
                    icon = R.drawable.ic_instagram.painter,
                    modifier = Modifier.padding(vertical = 10.dp),
                    onClick = { context.openInstagramByUserName(CommonConstants.INSTAGRAM_USERNAME) })
                SocialMediaItem(
                    icon = R.drawable.ic_whatsapp.painter,
                    modifier = Modifier.padding(vertical = 10.dp),
                    onClick = { context.openWhatsappByLink(CommonConstants.WHATSAPP_LINK) })
                SocialMediaItem(
                    icon = R.drawable.ic_telegram.painter,
                    modifier = Modifier.padding(vertical = 10.dp),
                    onClick = { context.openTelegramByUserName(CommonConstants.TELEGRAM_USERNAME) })

            }

            MualimButton(
                text = R.string.to_home.strResource,
                onClick = {
                    onHomeClick()
                }
            )

        }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    MualimSaniTheme {
        OnboardingScreenContent()
    }
}
