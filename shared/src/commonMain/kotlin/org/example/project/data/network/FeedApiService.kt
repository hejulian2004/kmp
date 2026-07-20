package org.example.project.data.network

import org.example.project.domain.model.FeedPost
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApiService {

    @GET("api/feed/posts")
    suspend fun getFeedPosts(): List<FeedPost>?

    @POST("api/feed/posts/{id}/like")
    suspend fun likePost(
        @Path("id") postId: String,
        @Query("userId") userId: String
    ): String?

    @POST("api/feed/posts/{id}/unlike")
    suspend fun unlikePost(
        @Path("id") postId: String,
        @Query("userId") userId: String
    ): String?

    @POST("api/feed/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Query("userId") userId: String,
        @Query("content") content: String
    ): String?
}

