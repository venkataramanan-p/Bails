package org.example.bails.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class ImageSharer(private val context: Context) {
    actual fun share(imageBitmap: ImageBitmap, title: String) {
        val androidBitmap = imageBitmap.asAndroidBitmap()

        // Save to cache directory
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "scoreboard.png")
        file.outputStream().use { stream ->
            androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        // Get content URI via FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Launch share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Scoreboard"))
    }
}

@Composable
actual fun rememberImageSharer(): ImageSharer {
    val context = LocalContext.current
    return remember { ImageSharer(context) }
}
