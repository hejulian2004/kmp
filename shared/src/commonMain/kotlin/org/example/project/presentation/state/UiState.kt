package org.example.project.presentation.state

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<out T>(
        val data: T,
        val refreshState: RefreshState = RefreshState.Idle
    ) : UiState<T>()
    data class Error(val message: String, val code: Int = -1) : UiState<Nothing>()
}

enum class RefreshState {
    Idle,        // 无刷新动作
    Refreshing,  // 下拉刷新
    LoadingMore  // 加载更多
}