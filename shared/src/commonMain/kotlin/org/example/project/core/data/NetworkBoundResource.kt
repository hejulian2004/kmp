/**
 * @File: NetworkBoundResource.kt
 * @Package: org.example.project.core.data
 * @Description: 项目全局通用Stale-While-Revalidate (SWR) 本地优先数据同步管道
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.example.project.domain.error.AppError
import org.example.project.domain.error.toAppError

/**
 * 项目全局通用Stale-While-Revalidate (SWR) 本地优先数据同步Flow管道
 *
 * @param Key查询参数/标识符类型
 * @param Entity本地领域实体/数据库数据类型
 * @param NetworkDto网络请求返回的DTO数据类型
 *
 * @param key标识参数
 * @param queryLocal从本地内存/数据库获取Flow的Lambda
 * @param fetchRemote从网络API发起请求拉取NetworkDto的Lambda
 * @param saveRemoteResult将网络DTO转换为Entity并持久化保存到本地DB/内存的Lambda
 * @param shouldFetch校验是否发起网络请求的判断Lambda（默认总是发起）
 */
inline fun <Key, Entity, NetworkDto> networkBoundResource(
    key: Key,
    crossinline queryLocal: (Key) -> Flow<Entity?>,
    crossinline fetchRemote: suspend (Key) -> NetworkDto,
    crossinline saveRemoteResult: suspend (Key, NetworkDto) -> Unit,
    crossinline shouldFetch: (Entity?) -> Boolean = { true }
): Flow<ResourceState<Entity>> = flow {
    // 1. 读取当前本地数据
    val localData = queryLocal(key).firstOrNull()

    // 2. 发射Loading状态（携带本地旧缓存供UI 0ms瞬间渲染）
    emit(ResourceState.Loading(localData))

    // 3. 按条件判断是否发起网络同步
    if (shouldFetch(localData)) {
        try {
            val remoteDto = fetchRemote(key)
            saveRemoteResult(key, remoteDto)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // 捕获网络/解析异常，发射Error并附带本地旧数据供UI降级
            emit(ResourceState.Error(e.toAppError(), localData))
        }
    }

    // 4. 绑定本地DB观察流，保障SSOT权威数据变动响应
    emitAll(queryLocal(key).map { entity ->
        if (entity != null) {
            ResourceState.Success(entity)
        } else {
            ResourceState.Error(AppError.NotFound, null)
        }
    })
}
