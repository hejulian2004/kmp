/**
 * @File: CameraLauncher.android.kt
 * @Package: org.example.project.ui.utils
 * @Description: Android 平台原生相机启动器 Actual 实现 (支持拍照与视频录制)
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
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
import java.io.FileOutputStream

/**
 * 视频录制的 ActivityResult 契约，兼容更多厂商相机与 URI 授权
 */
private class CaptureVideoContract : ActivityResultContract<Uri, Intent?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.clipData = ClipData.newUri(context.contentResolver, "Video", input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        return if (resultCode == Activity.RESULT_OK) intent ?: Intent() else null
    }
}

/**
 * Android 平台相机的组合式实现
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
            contract = CaptureVideoContract()
        ) { resultIntent ->
            val file = currentVideoFile
            if (resultIntent != null && file != null) {
                if (file.exists() && file.length() > 0) {
                    onResult(PlatformFile(file))
                    return@rememberLauncherForActivityResult
                }
                // 兼容部分系统相机返回 Intent.data URI 的情况
                val returnUri = resultIntent.data
                if (returnUri != null) {
                    try {
                        context.contentResolver.openInputStream(returnUri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (file.exists() && file.length() > 0) {
                            onResult(PlatformFile(file))
                            return@rememberLauncherForActivityResult
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            onResult(null)
        }

        remember(launcher, context) {
            CameraLauncherWrapper {
                try {
                    val videoDir = File(context.cacheDir, "videos").apply { mkdirs() }
                    val videoFile = File(videoDir, "video_${System.currentTimeMillis()}.mp4")
                    currentVideoFile = videoFile

                    val authority = "${context.packageName}.FileKitFileProvider"
                    val uri: Uri = FileProvider.getUriForFile(context, authority, videoFile)
                    launcher.launch(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(null)
                }
            }
        }
    }
}
