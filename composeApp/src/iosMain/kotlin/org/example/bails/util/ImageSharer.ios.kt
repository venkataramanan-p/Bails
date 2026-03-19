package org.example.bails.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.kCGRenderingIntentDefault
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIWindowScene

actual class ImageSharer {
    @OptIn(ExperimentalForeignApi::class)
    actual fun share(imageBitmap: ImageBitmap, title: String) {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val pixelMap = imageBitmap.toPixelMap()

        // Convert to RGBA byte array
        val byteArray = ByteArray(width * height * 4)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelMap[x, y]
                val offset = (y * width + x) * 4
                byteArray[offset] = (color.red * 255).toInt().toByte()
                byteArray[offset + 1] = (color.green * 255).toInt().toByte()
                byteArray[offset + 2] = (color.blue * 255).toInt().toByte()
                byteArray[offset + 3] = (color.alpha * 255).toInt().toByte()
            }
        }

        // Create CGImage from pixel data
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bitmapContext = byteArray.usePinned { pinned ->
            CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = (width * 4).toULong(),
                space = colorSpace,
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
            )
        } ?: return

        val cgImage = CGBitmapContextCreateImage(bitmapContext) ?: return
        val uiImage = UIImage(cGImage = cgImage)

        // Save to temp file for sharing
        val pngData = platform.UIKit.UIImagePNGRepresentation(uiImage) ?: return
        val tempPath = NSTemporaryDirectory() + "scoreboard.png"
        pngData.writeToFile(tempPath, atomically = true)

        // Present share sheet
        val activityVC = UIActivityViewController(
            activityItems = listOf(uiImage, title),
            applicationActivities = null
        )

        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        val rootVC = scene?.windows?.firstOrNull()?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}

@Composable
actual fun rememberImageSharer(): ImageSharer {
    return remember { ImageSharer() }
}
