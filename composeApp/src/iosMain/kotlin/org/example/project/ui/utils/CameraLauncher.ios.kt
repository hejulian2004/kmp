/**
 * @File: CameraLauncher.ios.kt
 * @Package: org.example.project.ui.utils
 * @Description: iOS 平台原生相机启动器 Actual 实现 (支持拍照与视频录制)
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher as rememberFileKitCameraPickerLauncher
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

/**
 * iOS 相机拾取代理，处理拍摄/录制完成后的 NSURL
 */
private class VideoCameraControllerDelegate(
    private val onVideoPicked: (NSURL?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val url = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
        onVideoPicked(url)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onVideoPicked(null)
    }
}

/**
 * iOS 平台相机的组合式实现
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
        val updatedOnResult by rememberUpdatedState(onResult)
        var activeDelegate by remember { mutableStateOf<VideoCameraControllerDelegate?>(null) }

        remember {
            CameraLauncherWrapper {
                val delegate = VideoCameraControllerDelegate { url ->
                    activeDelegate = null
                    if (url != null) {
                        updatedOnResult(PlatformFile(url))
                    } else {
                        updatedOnResult(null)
                    }
                }
                // 保持强引用，防止 Kotlin/Native GC 在录制期间回收 weak 代理
                activeDelegate = delegate

                val picker = UIImagePickerController()
                picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                picker.mediaTypes = listOf("public.movie")
                picker.delegate = delegate

                val keyWindow = UIApplication.sharedApplication.connectedScenes
                    .filterIsInstance<UIWindowScene>()
                    .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
                    ?.keyWindow
                var topController = keyWindow?.rootViewController
                while (topController?.presentedViewController != null) {
                    topController = topController.presentedViewController
                }
                topController?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
