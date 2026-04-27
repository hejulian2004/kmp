package org.example.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.delay
import org.example.project.presentation.intent.PostEditIntent
import org.example.project.presentation.viewmodel.PostEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(
    navController: NavHostController
) {
    val viewModel: PostEditViewModel = viewModel { PostEditViewModel() }
    val state by viewModel.state.collectAsState()

    var previewImage by remember { mutableStateOf<PlatformFile?>(null) }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = state.maxImageCount - state.images.size),
        onResult = { files ->
            files?.let {
                it.forEach { image ->
                    viewModel.onIntent(PostEditIntent.AddImages(image))
                }
            }
        }
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {

                },
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                ImageHorizontalList(
                    images = state.images,
                    maxCount = state.maxImageCount,
                    onAddClick = { launcher.launch() },
                    onRemoveClick = { index -> viewModel.onIntent(PostEditIntent.RemoveImage(index)) },
                    onImageClick = { file -> previewImage = file }
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = state.title,
                    onValueChange = { viewModel.onIntent(PostEditIntent.UpdateTitle(it)) },
                    placeholder = { Text("标题", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = state.body,
                    onValueChange = { viewModel.onIntent(PostEditIntent.UpdateBody(it)) },
                    placeholder = { Text("正文", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.onIntent(PostEditIntent.Publish) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("发布", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        ErrorOverlay(
            error = state.errorMessage,
            onDismiss = { viewModel.onIntent(PostEditIntent.ClearError) }
        )
    }

    previewImage?.let { file ->
        ImagePreviewDialog(
            file = file,
            onDismiss = { previewImage = null },
            onRemove = {
                val index = state.images.indexOf(file)
                if (index >= 0) viewModel.onIntent(PostEditIntent.RemoveImage(index))
                previewImage = null
            }
        )
    }
}

@Composable
fun ImageHorizontalList(
    images: List<PlatformFile>,
    maxCount: Int,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit,
    onImageClick: (PlatformFile) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(images, key = { _, file -> file.name }) { index, file ->
            Box(modifier = Modifier.size(90.dp)) {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(file) } // 点击查看大图
                )

                // 移除按钮
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(20.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .clickable { onRemoveClick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (images.size < maxCount) {
            item {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加",
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "${images.size}/${maxCount}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ImagePreviewDialog(
    file: PlatformFile,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            )

            // 顶部工具栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White
                    )
                }
                TextButton(onClick = onRemove) {
                    Text("移除", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ErrorOverlay(
    error: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                Text(
                    text = error ?: "",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }

        if (error != null) {
            LaunchedEffect(error) {
                delay(2000)
                onDismiss()
            }
        }
    }
}

@Composable
@Preview
fun ImagePreviewDialogPreview(){

}