/**
 * @File: FeedLinePublishScreen.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 发布新动态的图文编辑与上传界面（基于FileKit0.13.0跨平台适配）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.example.project.ui.utils.CameraMediaType
import org.example.project.ui.utils.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.ui.theme.feedline.FeedLineBackgroundGray
import org.example.project.ui.theme.feedline.FeedLineMediaPlaceholderGray
import org.example.project.ui.theme.feedline.FeedLinePrimaryGreen
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.cancel
import kotlinproject.composeapp.generated.resources.feedline_confirm_delete
import kotlinproject.composeapp.generated.resources.feedline_delete
import kotlinproject.composeapp.generated.resources.feedline_delete_photo_prompt
import kotlinproject.composeapp.generated.resources.feedline_delete_video_prompt
import kotlinproject.composeapp.generated.resources.feedline_placeholder
import kotlinproject.composeapp.generated.resources.feedline_publish
import kotlinproject.composeapp.generated.resources.feedline_publish_text
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PublishScreen(
    initialMediaList: List<FeedLineMedia>,
    onCancelClick: () -> Unit,
    onPostClick: (String, List<FeedLineMedia>) -> Unit,
    modifier: Modifier = Modifier,
    isTextOnly: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var textContent by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf(initialMediaList) }
    var mediaToDelete by remember { mutableStateOf<FeedLineMedia?>(null) }
    var activeImageUrl by remember { mutableStateOf<String?>(null) }
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }
    var isPublishing by remember { mutableStateOf(false) }

    if (isPublishing) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = FeedLinePrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "正在发表...", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }

    if (activeImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = activeImageUrl!!,
            onDismissRequest = { activeImageUrl = null }
        )
    }

    if (activeVideoUrl != null) {
        VideoPlayerDialog(
            videoUrl = activeVideoUrl!!,
            onDismissRequest = { activeVideoUrl = null }
        )
    }

    val remainingMediaSpace = (9 - selectedMedia.size).coerceAtLeast(1)
    // 从相册选择图片或视频Launcher (FileKit 0.13.0)
    val photoPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.ImageAndVideo,
        mode = FileKitMode.Multiple(maxItems = remainingMediaSpace),
        onResult = { files ->
            files?.let { list ->
                coroutineScope.launch {
                    val newMedia = list.map { file -> org.example.project.ui.utils.persistPickedMedia(file) }
                    selectedMedia = (selectedMedia + newMedia).take(9)
                }
            }
        }
    )

    // 拍摄照片Launcher
    val takePictureLauncher = rememberCameraPickerLauncher(
        type = CameraMediaType.Photo,
        onResult = { file ->
            file?.let {
                coroutineScope.launch {
                    val newMedia = org.example.project.ui.utils.persistPickedMedia(it)
                    selectedMedia = (selectedMedia + newMedia).take(9)
                }
            }
        }
    )

    // 拍摄视频Launcher
    val takeVideoLauncher = rememberCameraPickerLauncher(
        type = CameraMediaType.Video,
        onResult = { file ->
            file?.let {
                coroutineScope.launch {
                    val newMedia = org.example.project.ui.utils.persistPickedMedia(it)
                    selectedMedia = (selectedMedia + newMedia).take(9)
                }
            }
        }
    )

    var showAddMoreBottomSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(color = FeedLineBackgroundGray)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 顶部栏
            Box(
                modifier = Modifier
                    .background(color = FeedLineBackgroundGray)
                    .padding(horizontal = 12.dp)
                    .height(56.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onCancelClick() }
                        .padding(8.dp),
                    text = stringResource(Res.string.cancel),
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = if (isTextOnly) stringResource(Res.string.feedline_publish_text) else stringResource(Res.string.feedline_publish),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextButton(
                    enabled = textContent.isNotBlank() || (!isTextOnly && selectedMedia.isNotEmpty()),
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        isPublishing = true
                        coroutineScope.launch {
                            onPostClick(textContent, selectedMedia)
                            isPublishing = false
                        }
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.feedline_publish),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (textContent.isBlank() && (isTextOnly || selectedMedia.isEmpty())) Color.LightGray else FeedLinePrimaryGreen
                    )
                }
            }

            // 可滚动区域内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 文本输入框
                TextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    placeholder = { Text(stringResource(Res.string.feedline_placeholder), color = Color.Gray, fontSize = 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                if (!isTextOnly) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val spacing = 8.dp
                    val rows = if (selectedMedia.size < 9) {
                        (selectedMedia + null).chunked(3)
                    } else {
                        selectedMedia.chunked(3)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rows.forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 0 until 3) {
                                    if (i < rowItems.size) {
                                        val media = rowItems[i]
                                        if (media == null) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(FeedLineMediaPlaceholderGray)
                                                    .clickable {
                                                        showAddMoreBottomSheet = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "添加",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(FeedLineBackgroundGray)
                                                    .combinedClickable(
                                                        onClick = {
                                                            if (media is FeedLineMedia.Video) {
                                                                activeVideoUrl = media.videoUrl
                                                            } else if (media is FeedLineMedia.Image) {
                                                                activeImageUrl = media.url
                                                            }
                                                        },
                                                        onLongClick = {
                                                            mediaToDelete = media
                                                        }
                                                    )
                                            ) {
                                                if (media is FeedLineMedia.Image) {
                                                    if (media.url.isNotEmpty()) {
                                                        AsyncImage(
                                                            model = media.url,
                                                            contentDescription = null,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                } else if (media is FeedLineMedia.Video) {
                                                    VideoThumbnail(
                                                        videoUrl = media.videoUrl,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                if (media is FeedLineMedia.Video) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = "视频",
                                                        tint = Color.White,
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .align(Alignment.Center)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 删除确认弹窗
    if (mediaToDelete != null) {
        val isVideo = mediaToDelete is FeedLineMedia.Video
        AlertDialog(
            onDismissRequest = { mediaToDelete = null },
            title = { Text(stringResource(Res.string.feedline_confirm_delete)) },
            text = { Text(if (isVideo) stringResource(Res.string.feedline_delete_video_prompt) else stringResource(Res.string.feedline_delete_photo_prompt)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMedia = selectedMedia.filterNot { it == mediaToDelete }
                        mediaToDelete = null
                    }
                ) {
                    Text(stringResource(Res.string.feedline_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mediaToDelete = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    // 添加更多媒体底栏
    if (showAddMoreBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMoreBottomSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            BottomSheet(
                onTakePhotoClick = {
                    showAddMoreBottomSheet = false
                    if (selectedMedia.size < 9) takePictureLauncher.launch()
                },
                onRecordVideoClick = {
                    showAddMoreBottomSheet = false
                    if (selectedMedia.size < 9) takeVideoLauncher.launch()
                },
                onChooseClick = {
                    showAddMoreBottomSheet = false
                    if (selectedMedia.size < 9) photoPickerLauncher.launch()
                },
                onCancelClick = {
                    showAddMoreBottomSheet = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PublishScreenPreview() {
    PublishScreen(
        initialMediaList = emptyList(),
        onCancelClick = {},
        onPostClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun PublishScreenTextOnlyPreview() {
    PublishScreen(
        initialMediaList = emptyList(),
        isTextOnly = true,
        onCancelClick = {},
        onPostClick = { _, _ -> }
    )
}
