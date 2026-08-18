/**
 * @File: StartupGate.kt
 * @Package: org.example.project.ui.components
 * @Description: 应用启动生命周期拦截门（StartupGate），保证核心基础设施就绪前阻断业务UI与ViewModel初始化
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.core.init.AppInitFailure
import org.example.project.core.init.AppInitParams
import org.example.project.core.init.AppInitializer

sealed interface StartupState {
    data object Initializing : StartupState
    data object Ready : StartupState
    data class Failed(val failures: List<AppInitFailure>) : StartupState
}

@Composable
fun StartupGate(
    initParams: AppInitParams,
    content: @Composable () -> Unit
) {
    var startupState by remember { mutableStateOf<StartupState>(StartupState.Initializing) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(retryTrigger) {
        startupState = StartupState.Initializing
        val result = AppInitializer.init(initParams)
        startupState = if (result.success) {
            StartupState.Ready
        } else {
            StartupState.Failed(result.failures)
        }
    }

    when (val state = startupState) {
        StartupState.Initializing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("应用基础设施初始化中...")
                }
            }
        }
        StartupState.Ready -> {
            content()
        }
        is StartupState.Failed -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "初始化失败",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    state.failures.forEach { failure ->
                        Text(
                            text = "${failure.module}: ${failure.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { retryTrigger++ }) {
                        Text("重试")
                    }
                }
            }
        }
    }
}
