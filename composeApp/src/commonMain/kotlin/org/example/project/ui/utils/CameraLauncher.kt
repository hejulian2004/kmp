/**
 * @File: CameraLauncher.kt
 * @Package: org.example.project.ui.utils
 * @Description: 跨平台相机启动器通用Expect定义与平台工具
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.utils

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * 相机捕获媒体类型
 */
enum class CameraMediaType {
    Photo,
    Video
}

/**
 * 相机启动器包装接口
 */
fun interface CameraLauncherWrapper {
    /**
     * 调起相机
     */
    fun launch()
}

/**
 * 创建并记住跨平台相机启动器
 *
 * @param type捕获的媒体类型（照片或视频）
 * @param onResult拍照/录制完成后的回调，返回 [PlatformFile] 文件或null
 */
@Composable
expect fun rememberCameraPickerLauncher(
    type: CameraMediaType = CameraMediaType.Photo,
    onResult: (PlatformFile?) -> Unit
): CameraLauncherWrapper
