package org.example.project.ui.screens.instagram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.ui.theme.instagram.InstagramTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramHomeScreen() {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Home Screen (Coming Soon)", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview
@Composable
fun InstagramHomeScreenPreview() {
    InstagramTheme {
        InstagramHomeScreen()
    }
}
