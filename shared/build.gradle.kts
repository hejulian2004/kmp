import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
}


kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "org.example.project.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.compose.runtime)
                implementation(libs.filekit.core)
                implementation(libs.androidx.startup.runtime)
                implementation(libs.filekit.dialogs)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.websockets)

                api(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.security.crypto)
                implementation(libs.androidx.core.ktx)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}









// ==============================================================================
// SDUI 自动导出任务配置 (组件名_版本号_导出时间.json)
// ==============================================================================
val generateSduiJsonTask = tasks.register("generateSduiJson") {
    group = "sdui"
    description = "编译构建时按 (组件名+版本号+导出时间) 格式自动导出 SDUI 热更 JSON"

    val targetDir = layout.projectDirectory.dir("../build/outputs/sdui").asFile
    val versionConfigFile = layout.projectDirectory.file("src/commonMain/kotlin/org/example/project/core/sdui/config/SduiVersionConfig.kt").asFile

    doLast {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        // 从统一配置文件 SduiVersionConfig.kt 动态正则解析版本号（彻底消除 Gradle 中的硬编码）
        var feedlineVersion = "v1.0.0"
        var instagramVersion = "v1.0.0"
        var airbnbVersion = "v1.0.0"
        var wechatMpVersion = "v1.0.0"

        if (versionConfigFile.exists()) {
            val configContent = versionConfigFile.readText()
            Regex("""MODULE_FEEDLINE_VERSION\s*=\s*"([^"]+)"""").find(configContent)?.let {
                feedlineVersion = it.groupValues[1]
            }
            Regex("""MODULE_INSTAGRAM_VERSION\s*=\s*"([^"]+)"""").find(configContent)?.let {
                instagramVersion = it.groupValues[1]
            }
            Regex("""MODULE_AIRBNB_VERSION\s*=\s*"([^"]+)"""").find(configContent)?.let {
                airbnbVersion = it.groupValues[1]
            }
            Regex("""MODULE_WECHAT_MP_VERSION\s*=\s*"([^"]+)"""").find(configContent)?.let {
                wechatMpVersion = it.groupValues[1]
            }
        }

        // 导出格式：组件名_版本号_导出时间.json
        val feedlineFileName = "FeedLine_${feedlineVersion}_${timestamp}.json"
        val instagramFileName = "Instagram_${instagramVersion}_${timestamp}.json"
        val airbnbFileName = "Airbnb_${airbnbVersion}_${timestamp}.json"
        val wechatMpFileName = "WeChatMp_${wechatMpVersion}_${timestamp}.json"

        val feedlineFile = File(targetDir, feedlineFileName)
        val instagramFile = File(targetDir, instagramFileName)
        val airbnbFile = File(targetDir, airbnbFileName)
        val wechatMpFile = File(targetDir, wechatMpFileName)

        // 写入 FeedLine 模块 JSON 内容
        val feedlineJsonContent = """
            {
              "componentType": "LazyColumn",
              "properties": { "version": "$feedlineVersion", "exportedAt": "$timestamp" },
              "children": [
                {
                  "componentType": "FeedLineTopBar",
                  "properties": { "title": "朋友圈", "version": "v1.0.0" },
                  "actions": {
                    "onShortClick": { "type": "CREATE_POST_SHORT" },
                    "onLongClick": { "type": "CREATE_POST_LONG" }
                  }
                },
                {
                  "componentType": "FeedLineNotificationBar",
                  "properties": { "count": "0", "version": "v1.0.0" }
                },
                {
                  "componentType": "FeedLinePostItem",
                  "properties": { "version": "v1.0.0" }
                }
              ]
            }
        """.trimIndent()

        // 写入 Instagram 模块 JSON 内容
        val instagramJsonContent = """
            {
              "componentType": "LazyColumn",
              "properties": { "version": "$instagramVersion", "exportedAt": "$timestamp" },
              "children": [
                {
                  "componentType": "InstagramHomeTopBar",
                  "properties": { "version": "v1.0.0" },
                  "actions": {
                    "onCameraClick": { "type": "OPEN_CAMERA" },
                    "onDirectClick": { "type": "OPEN_DIRECT" }
                  }
                },
                {
                  "componentType": "InstagramStoryTray",
                  "properties": { "version": "v1.0.0" }
                },
                {
                  "componentType": "InstagramPostItem",
                  "properties": { "version": "v1.0.0" }
                }
              ]
            }
        """.trimIndent()

        // 写入 Airbnb 模块 JSON 内容
        val airbnbJsonContent = """
            {
              "componentType": "LazyColumn",
              "properties": { "version": "$airbnbVersion", "exportedAt": "$timestamp" },
              "children": [
                {
                  "componentType": "AirbnbTopBar",
                  "properties": { "title": "个人资料", "actionText": "编辑", "version": "v1.0.0" },
                  "actions": { "onActionClick": { "type": "EDIT_PROFILE" } }
                },
                {
                  "componentType": "AirbnbHostSelector",
                  "properties": { "selectedHostId": "art-room-hk", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbProfileHeroCard",
                  "properties": { "name": "ArtRoomHK", "superHost": "true", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbHobbiesSection",
                  "properties": { "hobbies": "艺术展览,城市散步", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbPlacesSection",
                  "properties": { "places": "东京,巴黎", "isVisible": "true", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbReviewCard",
                  "properties": { "reviewerName": "Yoshimi", "stars": "5", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbListingCard",
                  "properties": { "title": "酒店式公寓", "rating": "4.84", "version": "v1.0.0" }
                },
                {
                  "componentType": "AirbnbActionItem",
                  "properties": { "icon": "⚙️", "label": "系统设置", "version": "v1.0.0" },
                  "actions": { "onClick": { "type": "OPEN_SETTINGS" } }
                }
              ]
            }
        """.trimIndent()

        // 写入 WeChatMp 微信公众号模块 JSON 内容
        val wechatMpJsonContent = """
            {
              "componentType": "LazyVerticalStaggeredGrid",
              "properties": { "version": "$wechatMpVersion", "exportedAt": "$timestamp", "columns": "2" },
              "children": [
                {
                  "componentType": "WeChatMpTopBar",
                  "properties": { "title": "公众号", "version": "v1.0.0" },
                  "actions": {
                    "onBackClick": { "type": "NAVIGATE_BACK" },
                    "onSearchClick": { "type": "OPEN_SEARCH" },
                    "onProfileClick": { "type": "OPEN_PROFILE" }
                  }
                },
                {
                  "componentType": "WeChatMpFrequentlyReadBar",
                  "properties": { "title": "常读", "version": "v1.0.0" }
                },
                {
                  "componentType": "WeChatMpFeaturedBannerCard",
                  "properties": { "version": "v1.0.0" }
                },
                {
                  "componentType": "WeChatMpHorizontalCard",
                  "properties": { "version": "v1.0.0" }
                },
                {
                  "componentType": "WeChatMpWaterfallCard",
                  "properties": { "version": "v1.0.0" }
                }
              ]
            }
        """.trimIndent()

        val subComponentsDir = File(targetDir, "components")
        if (!subComponentsDir.exists()) {
            subComponentsDir.mkdirs()
        }

        // 1. 导出主模块聚合 JSON (格式：模块名_版本号_导出时间.json)
        feedlineFile.writeText(feedlineJsonContent)
        instagramFile.writeText(instagramJsonContent)
        airbnbFile.writeText(airbnbJsonContent)
        wechatMpFile.writeText(wechatMpJsonContent)

        // 2. 导出单组件独立 JSON (格式：单组件名_版本号_导出时间.json)
        val topBarComponentFile = File(subComponentsDir, "FeedLineTopBar_v1.0.0_${timestamp}.json")
        topBarComponentFile.writeText("""
            {
              "componentType": "FeedLineTopBar",
              "properties": { "title": "朋友圈", "version": "v1.0.0" },
              "actions": {
                "onShortClick": { "type": "CREATE_POST_SHORT" },
                "onLongClick": { "type": "CREATE_POST_LONG" }
              }
            }
        """.trimIndent())

        val airbnbTopBarComponentFile = File(subComponentsDir, "AirbnbTopBar_v1.0.0_${timestamp}.json")
        airbnbTopBarComponentFile.writeText("""
            {
              "componentType": "AirbnbTopBar",
              "properties": { "title": "个人资料", "actionText": "编辑", "version": "v1.0.0" },
              "actions": {
                "onActionClick": { "type": "EDIT_PROFILE" }
              }
            }
        """.trimIndent())

        val wechatTopBarComponentFile = File(subComponentsDir, "WeChatMpTopBar_v1.0.0_${timestamp}.json")
        wechatTopBarComponentFile.writeText("""
            {
              "componentType": "WeChatMpTopBar",
              "properties": { "title": "公众号", "version": "v1.0.0" },
              "actions": {
                "onBackClick": { "type": "NAVIGATE_BACK" },
                "onSearchClick": { "type": "OPEN_SEARCH" },
                "onProfileClick": { "type": "OPEN_PROFILE" }
              }
            }
        """.trimIndent())

        println("[SDUI] 编译产出热更 JSON 文件成功:")
        println("  [模块聚合 JSON] -> ${feedlineFile.absolutePath}")
        println("  [模块聚合 JSON] -> ${instagramFile.absolutePath}")
        println("  [模块聚合 JSON] -> ${airbnbFile.absolutePath}")
        println("  [模块聚合 JSON] -> ${wechatMpFile.absolutePath}")
        println("  [单组件独立 JSON] -> ${topBarComponentFile.absolutePath}")
        println("  [单组件独立 JSON] -> ${airbnbTopBarComponentFile.absolutePath}")
        println("  [单组件独立 JSON] -> ${wechatTopBarComponentFile.absolutePath}")
    }
}


// 绑定到编译任务，在编译完成后自动触发导出
tasks.matching { it.name.startsWith("compile") }.configureEach {
    finalizedBy(generateSduiJsonTask)
}