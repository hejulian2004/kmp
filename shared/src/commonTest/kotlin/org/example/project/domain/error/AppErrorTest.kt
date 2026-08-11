/**
 * @File: AppErrorTest.kt
 * @Package: org.example.project.domain.error
 * @Description: AppError领域错误与NetworkError映射关系单元测试
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.domain.error

import kotlinx.coroutines.CancellationException
import org.example.project.core.network.error.NetworkError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AppErrorTest {

    @Test
    fun testNetworkErrorToAppErrorMapping() {
        val noConnection = NetworkError.NoConnection.toAppError()
        assertIs<AppError.NetworkUnavailable>(noConnection)

        val timeout = NetworkError.Timeout.toAppError()
        assertIs<AppError.Timeout>(timeout)

        val unauthorized = NetworkError.Unauthorized.toAppError()
        assertIs<AppError.LoginExpired>(unauthorized)

        val forbidden = NetworkError.Forbidden.toAppError()
        assertIs<AppError.PermissionDenied>(forbidden)

        val notFound = NetworkError.NotFound.toAppError()
        assertIs<AppError.NotFound>(notFound)

        val rateLimited = NetworkError.RateLimited(60L).toAppError()
        assertIs<AppError.RateLimited>(rateLimited)
        assertEquals(60L, rateLimited.retryAfterSeconds)

        val conflict = NetworkError.Conflict(409, "v2.0").toAppError()
        assertIs<AppError.DataConflict>(conflict)
        assertEquals(409, conflict.statusCode)
        assertEquals("v2.0", conflict.serverVersion)

        val server = NetworkError.Server(500).toAppError()
        assertIs<AppError.ServerFailure>(server)
        assertEquals(500, server.code)

        val update = NetworkError.UpdateRequired(200L).toAppError()
        assertIs<AppError.UpdateRequired>(update)
        assertEquals(200L, update.requiredBuild)
    }

    @Test
    fun testCancellationExceptionRethrownInThrowableToAppError() {
        val cancelException = CancellationException("Job cancelled")

        assertFailsWith<CancellationException> {
            cancelException.toAppError()
        }
    }
}
