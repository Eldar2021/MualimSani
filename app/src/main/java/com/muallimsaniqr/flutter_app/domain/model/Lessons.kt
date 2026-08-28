package com.muallimsaniqr.flutter_app.domain.model

interface Lessons

data class Lesson(
    val id: Int,
    val title: Int,
    val fileNameExceptExt: Int = 0
): Lessons

data class HeaderItem(
    val title: Int,
): Lessons