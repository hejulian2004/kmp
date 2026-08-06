/**
 * @File: CameraLauncher.android.kt
 * @Package: org.example.project.ui.utils
 * @Description: Android平台原生相机启动器Actual实现 (支持拍照与视频录制)
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher as rememberFileKitCameraPickerLauncher
import java.io.File

/**
 * Android平台相机的组合式实现
 */
@Composable
actual fun rememberCameraPickerLauncher(
    type: CameraMediaType,
    onResult: (PlatformFile?) -> Unit
): CameraLauncherWrapper {
    return if (type == CameraMediaType.Photo) {
        val launcher = rememberFileKitCameraPickerLauncher(onResult = onResult)
        remember(launcher) {
            CameraLauncherWrapper { launcher.launch() }
        }
    } else {
        val context = LocalContext.current
        var currentVideoFile by remember { mutableStateOf<File?>(null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CaptureVideo()
        ) { success ->
            val file = currentVideoFile
            if (success && file != null && file.exists() && file.length() > 0) {
                onResult(PlatformFile(file))
            } else {
                onResult(null)
            }
        }

        remember(launcher, context) {
            CameraLauncherWrapper {
                try {
                    val videoDir = File(context.cacheDir, "videos").apply { mkdirs() }
                    val videoFile = File(videoDir, "video_${System.currentTimeMillis()}.mp4")
                    currentVideoFile = videoFile

                    val authority = "${context.packageName}.filekit.fileprovider"
                    val uri: Uri = FileProvider.getUriForFile(context, authority, videoFile)
                    launcher.launch(uri)
                } catch (_: Exception) {
                    onResult(null)
                }
            }
        }
    }
}
