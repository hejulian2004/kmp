package org.example.project.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.*
import org.example.project.domain.model.FeedMedia
import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedPost
import java.lang.reflect.Type

class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(FeedMedia::class.java, FeedMediaAdapter())
        .create()

    companion object {
        private const val DATABASE_NAME = "moments_cache.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_POSTS = "feed_posts"
        private const val KEY_ID = "id"
        private const val KEY_JSON = "post_json"
        private const val KEY_CREATE_TIME = "create_time"

        private const val TABLE_NOTIFICATIONS = "feed_notifications"
        private const val KEY_NOTIFICATION_JSON = "notification_json"
        private const val KEY_NOTIFICATION_CREATED_TIME = "notification_created_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPostsTable = ("CREATE TABLE " + TABLE_POSTS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_JSON + " TEXT,"
                + KEY_CREATE_TIME + " INTEGER" + ")")
        db.execSQL(createPostsTable)

        val createNotificationsTable = ("CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_NOTIFICATION_JSON + " TEXT,"
                + KEY_NOTIFICATION_CREATED_TIME + " INTEGER" + ")")
        db.execSQL(createNotificationsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun getAllPosts(): List<FeedPost> {
        val postsList = mutableListOf<FeedPost>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_POSTS ORDER BY $KEY_CREATE_TIME DESC"
        val cursor = db.rawQuery(selectQuery, null)

        cursor.use { c ->
            val jsonIndex = c.getColumnIndex(KEY_JSON)
            if (jsonIndex != -1 && c.moveToFirst()) {
                do {
                    val json = c.getString(jsonIndex)
                    try {
                        val post = gson.fromJson(json, FeedPost::class.java)
                        if (post != null) {
                            postsList.add(post)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } while (c.moveToNext())
            }
        }
        return postsList
    }
    
    fun insertPosts(posts: List<FeedPost>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (post in posts) {
                val values = ContentValues().apply {
                    put(KEY_ID, post.id)
                    put(KEY_JSON, gson.toJson(post))
                    put(KEY_CREATE_TIME, post.createTime)
                }
                db.insertWithOnConflict(TABLE_POSTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAllPosts() {
        val db = this.writableDatabase
        db.delete(TABLE_POSTS, null, null)
    }

    fun getAllNotifications(): List<FeedNotification> {
        val notificationsList = mutableListOf<FeedNotification>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NOTIFICATIONS ORDER BY $KEY_NOTIFICATION_CREATED_TIME DESC"
        val cursor = db.rawQuery(selectQuery, null)

        cursor.use { c ->
            val jsonIndex = c.getColumnIndex(KEY_NOTIFICATION_JSON)
            if (jsonIndex != -1 && c.moveToFirst()) {
                do {
                    val json = c.getString(jsonIndex)
                    try {
                        val notification = gson.fromJson(json, FeedNotification::class.java)
                        if (notification != null) {
                            notificationsList.add(notification)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } while (c.moveToNext())
            }
        }
        return notificationsList
    }

    fun insertNotifications(notifications: List<FeedNotification>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (notification in notifications) {
                val values = ContentValues().apply {
                    put(KEY_ID, notification.id)
                    put(KEY_NOTIFICATION_JSON, gson.toJson(notification))
                    put(KEY_NOTIFICATION_CREATED_TIME, notification.createdTime)
                }
                db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAllNotifications() {
        val db = this.writableDatabase
        db.delete(TABLE_NOTIFICATIONS, null, null)
    }
}

class FeedMediaAdapter : JsonSerializer<FeedMedia>, JsonDeserializer<FeedMedia> {
    override fun serialize(src: FeedMedia, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        when (src) {
            is FeedMedia.Image -> {
                jsonObject.addProperty("type", "image")
                jsonObject.addProperty("url", src.url)
                src.width?.let { jsonObject.addProperty("width", it) }
                src.height?.let { jsonObject.addProperty("height", it) }
            }
            is FeedMedia.Video -> {
                jsonObject.addProperty("type", "video")
                jsonObject.addProperty("videoUrl", src.videoUrl)
                src.coverUrl?.let { jsonObject.addProperty("coverUrl", it) }
                src.durationSecond?.let { jsonObject.addProperty("durationSecond", it) }
                src.width?.let { jsonObject.addProperty("width", it) }
                src.height?.let { jsonObject.addProperty("height", it) }
            }
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FeedMedia {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString ?: "image"
        return if (type == "video") {
            val videoUrl = jsonObject.get("videoUrl")?.asString ?: ""
            val coverUrl = jsonObject.get("coverUrl")?.asString
            val durationSecond = jsonObject.get("durationSecond")?.asInt
            val width = jsonObject.get("width")?.asInt
            val height = jsonObject.get("height")?.asInt
            FeedMedia.Video(
                coverUrl = coverUrl,
                videoUrl = videoUrl,
                durationSecond = durationSecond,
                width = width,
                height = height
            )
        } else {
            val url = jsonObject.get("url")?.asString ?: ""
            val width = jsonObject.get("width")?.asInt
            val height = jsonObject.get("height")?.asInt
            FeedMedia.Image(
                url = url,
                width = width,
                height = height
            )
        }
    }
}

