/**
 * @File: StoragePathValidatorTest.kt
 * @Package: org.example.project.core.storage
 * @Description: StoragePathValidator 路径安全校验与规范化单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage

import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.internal.StoragePathValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoragePathValidatorTest {

    @Test
    fun testValidRelativePathNormalization() {
        val path1 = StoragePath("feedline/images/avatar.jpg")
        assertEquals("feedline/images/avatar.jpg", StoragePathValidator.validateAndNormalize(path1))

        val path2 = StoragePath("sdui\\\\instagram\\\\layout.json")
        assertEquals("sdui/instagram/layout.json", StoragePathValidator.validateAndNormalize(path2))

        val path3 = StoragePath("  drafts/20260811.json  ")
        assertEquals("drafts/20260811.json", StoragePathValidator.validateAndNormalize(path3))
    }

    @Test
    fun testAbsolutePathsRejection() {
        val unixAbsPath = StoragePath("/data/user/0/org.example/files/data.json")
        assertFalse(StoragePathValidator.isValid(unixAbsPath))
        val exception1 = assertFailsWith<StorageException> {
            StoragePathValidator.validateAndNormalize(unixAbsPath)
        }
        assertEquals(StorageError.InvalidPath, exception1.error)

        val winAbsPath = StoragePath("C:\\Users\\Administrator\\secret.txt")
        assertFalse(StoragePathValidator.isValid(winAbsPath))
        val exception2 = assertFailsWith<StorageException> {
            StoragePathValidator.validateAndNormalize(winAbsPath)
        }
        assertEquals(StorageError.InvalidPath, exception2.error)
    }

    @Test
    fun testPathTraversalRejection() {
        val traversalPath1 = StoragePath("../config.json")
        assertFalse(StoragePathValidator.isValid(traversalPath1))

        val traversalPath2 = StoragePath("feedline/../../secret.txt")
        assertFalse(StoragePathValidator.isValid(traversalPath2))
        val exception = assertFailsWith<StorageException> {
            StoragePathValidator.validateAndNormalize(traversalPath2)
        }
        assertEquals(StorageError.InvalidPath, exception.error)
    }

    @Test
    fun testNullCharacterRejection() {
        val nullCharPath = StoragePath("images/avatar\u0000.jpg")
        assertFalse(StoragePathValidator.isValid(nullCharPath))
        assertFailsWith<StorageException> {
            StoragePathValidator.validateAndNormalize(nullCharPath)
        }
    }
}
