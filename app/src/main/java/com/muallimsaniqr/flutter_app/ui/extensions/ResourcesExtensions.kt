package com.muallimsaniqr.flutter_app.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

val Int.painter: Painter
    @Composable get() = painterResource(id = this)

val Int.strResource: String
    @Composable get() = stringResource(id = this)