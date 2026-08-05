/**
 * @File: HostProfileEffect.kt
 * @Package: org.example.project.presentation.effect.airbnb
 * @Description: Airbnb 模块 MVI 单次事件 Effect
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.effect.airbnb

sealed interface HostProfileEffect {
    data class ShowToast(val message: String) : HostProfileEffect
}
