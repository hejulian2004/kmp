/**
 * @File: HostProfileEffect.kt
 * @Package: org.example.project.presentation.effect.airbnb
 * @Description: Airbnb模块MVI单次事件Effect
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.effect.airbnb

sealed interface HostProfileEffect {
    data class ShowToast(val message: String) : HostProfileEffect
}
