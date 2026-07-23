package org.example.project.ui.components.feedline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.example.project.ui.theme.InstagramTheme
import org.example.project.ui.theme.InstagramTypography

import kotlinproject.composeapp.generated.resources.*
import org.example.project.navigation.instagram.InstagramScreen
import org.jetbrains.compose.resources.stringResource

@Composable
fun PostPickerContent(
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.post_picker_title),
            style = InstagramTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PostOption(
                icon = Icons.Outlined.Image,
                label = stringResource(Res.string.post_option_image),
                onClick = {
                    onDismiss()
                    navController.navigate(InstagramScreen.ImagePickerScreen.route)
                }
            )
            PostOption(
                icon = Icons.Outlined.VideoLibrary,
                label = stringResource(Res.string.post_option_video),
                onClick = {
                    onDismiss()
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clickable{onDismiss()},
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                stringResource(Res.string.cancel),
                style = InstagramTypography.titleLarge,
            )
        }
    }
}

@Composable
private fun PostOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = InstagramTypography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PostPickerContentPreview() {
    InstagramTheme {
        PostPickerContent(
            {},
            rememberNavController()
        )
    }
}
