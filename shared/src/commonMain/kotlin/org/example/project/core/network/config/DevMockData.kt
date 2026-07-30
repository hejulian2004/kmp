/**
 * @File: DevMockData.kt
 * @Package: org.example.project.core.network.config
 * @Description: 无后端接口开发阶段网络层假数据生成工具
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.config

import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import kotlin.math.abs
import kotlin.random.Random

/**
 * 随机ID生成工具
 */
private fun generateDevRandomId(): String {
    val pool = "0123456789abcdef"
    return (1..16).map { pool[Random.nextInt(pool.length)] }.joinToString("")
}

/**
 * [createFakeFeedPosts]
 * 生成网络层无后端接口时返回的 Mock朋友圈动态假数据列表。
 * 
 * @return 返回模拟的朋友圈动态列表
 */
fun createFakeFeedPosts(): List<FeedLinePost> {
    val user = FeedLineUser(id = "1", name = "何聚敛1", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))
    return listOf(
        createSingleFakePost(user),
        createSingleFakePost(user.copy(id = "2", name = "何聚敛2", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createSingleFakePost(user.copy(id = "3", name = "何聚敛3", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createSingleFakePost(user.copy(id = "4", name = "何聚敛4", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createSingleFakePost(user.copy(id = "5", name = "何聚敛5", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createSingleFakePost(user.copy(id = "6", name = "何聚敛6", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000)))
    )
}

private fun createSingleFakePost(user: FeedLineUser): FeedLinePost {
    val fakeLikedUsers = listOf(
        FeedLineUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
        FeedLineUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
        FeedLineUser(id = "33", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
        FeedLineUser(id = "44", name = "张三2", avatarUrl = "https://i.pravatar.cc/300?img=4"),
        FeedLineUser(id = "55", name = "李四2", avatarUrl = "https://i.pravatar.cc/300?img=5"),
        FeedLineUser(id = "66", name = "王五2", avatarUrl = "https://i.pravatar.cc/300?img=6"),
        FeedLineUser(id = "77", name = "张三3", avatarUrl = "https://i.pravatar.cc/300?img=7"),
        FeedLineUser(id = "88", name = "李四4", avatarUrl = "https://i.pravatar.cc/300?img=8"),
        FeedLineUser(id = "99", name = "王五5", avatarUrl = "https://i.pravatar.cc/300?img=9")
    )
    val postId = generateDevRandomId()
    return FeedLinePost(
        id = postId,
        postUser = user,
        content = "这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容-----------------------------------------------\n---\n---\n---\n" + abs(Random.nextLong() % 1000),
        likedUsers = fakeLikedUsers,
        commentsList = listOf(
            FeedLineComment(
                id = generateDevRandomId(),
                postId = postId,
                commentUser = FeedLineUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
                content = "这个朋友圈写得不错"
            ),
            FeedLineComment(
                id = generateDevRandomId(),
                postId = postId,
                commentUser = FeedLineUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错"
            ),
            FeedLineComment(
                id = generateDevRandomId(),
                postId = postId,
                commentUser = FeedLineUser(id = "33", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错，这个朋友圈写得不错"
            )
        )
    )
}

fun createFakeData(): List<FeedLinePost> = createFakeFeedPosts()

fun createFakePost(user: FeedLineUser = FeedLineUser(id = "1", name = "何聚敛1", avatarUrl = "https://i.pravatar.cc/300")): FeedLinePost {
    return createSingleFakePost(user)
}
