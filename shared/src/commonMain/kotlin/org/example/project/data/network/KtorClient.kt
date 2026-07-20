package org.example.project.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
// import io.ktor.client.call.body
// import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
// import io.ktor.serialization.kotlinx.json.json
// import kotlinx.serialization.json.Json
import org.example.project.domain.model.FeedPost

// TODO: Note that full Ktor serialization requires configuring `kotlinx-serialization`
// in the build.gradle.kts and adding the `@Serializable` annotation to the models.
// For now, this is a structural mock of the Ktor client for KMP.

object KtorClient {
    private const val BASE_URL = "https://api.example.invalid"

    val httpClient = HttpClient {
        // install(ContentNegotiation) {
        //     json(Json {
        //         ignoreUnknownKeys = true
        //         prettyPrint = true
        //     })
        // }
    }

    suspend fun getFeedPosts(): List<FeedPost>? {
        return try {
            // val response: List<FeedPost> = httpClient.get("$BASE_URL/api/feed/posts").body()
            // return response
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun likePost(postId: String, userId: String): String? {
        return try {
            // httpClient.post("$BASE_URL/api/feed/posts/$postId/like") {
            //     parameter("userId", userId)
            // }
            "Success"
        } catch (e: Exception) {
            null
        }
    }
}
