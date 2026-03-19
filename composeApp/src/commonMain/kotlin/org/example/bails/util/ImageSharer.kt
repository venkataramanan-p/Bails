package org.example.bails.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

expect class ImageSharer {
    fun share(imageBitmap: ImageBitmap, title: String)
}

@Composable
expect fun rememberImageSharer(): ImageSharer
