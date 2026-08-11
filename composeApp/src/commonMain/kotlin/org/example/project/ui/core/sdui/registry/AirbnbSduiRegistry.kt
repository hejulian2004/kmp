/**
 * @File: AirbnbSduiRegistry.kt
 * @Package: org.example.project.ui.core.sdui.registry
 * @Description: Airbnb模块全量SDUI动态组件适配与集中注册入口
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.ui.core.sdui.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.domain.model.airbnb.Host
import org.example.project.domain.model.airbnb.HostReview
import org.example.project.domain.model.airbnb.PropertyListing
import org.example.project.ui.components.airbnb.AboutMeSection
import org.example.project.ui.components.airbnb.ActionItem
import org.example.project.ui.components.airbnb.AvatarSection
import org.example.project.ui.components.airbnb.DestinationStamp
import org.example.project.ui.components.airbnb.DetailLine
import org.example.project.ui.components.airbnb.HobbiesSection
import org.example.project.ui.components.airbnb.HostSelector
import org.example.project.ui.components.airbnb.ListingCard
import org.example.project.ui.components.airbnb.PlacesSection
import org.example.project.ui.components.airbnb.ProfileFieldItem
import org.example.project.ui.components.airbnb.ProfileHeroCard
import org.example.project.ui.components.airbnb.ReviewCard
import org.example.project.ui.components.airbnb.SectionCard
import org.example.project.ui.components.airbnb.SectionTitle
import org.example.project.ui.components.airbnb.ToggleItem
import org.example.project.ui.components.airbnb.TopBar
import org.example.project.ui.components.airbnb.destinationEmojis
import org.example.project.ui.core.sdui.SduiComponentRegistry

/**
 * 集中注册Airbnb模块下所有16个可热更组件
 */
fun registerAirbnbSduiComponents() {
    val sampleHost = Host(
        id = "art-room-hk",
        name = "ArtRoomHK",
        reviewCount = 2066,
        rating = 4.85,
        yearsHosting = 7,
        totalListings = 11,
        languages = "中文和英语",
        identityVerified = true,
        superHost = true,
        about = "ArtRoom 是一个极具艺术气息的空间。",
        occupation = "艺术家 / 策展人",
        livesIn = "香港",
        hobbies = listOf("艺术展览", "城市散步"),
        places = listOf("东京", "巴黎"),
        avatarUrl = ""
    )

    // 1. 顶部导航栏 (AirbnbTopBar)
    SduiComponentRegistry.register("AirbnbTopBar") { node, onAction ->
        val title = node.properties["title"] ?: "个人资料"
        val actionText = node.properties["actionText"] ?: "编辑"
        TopBar(
            title = title,
            actionText = actionText,
            onActionClick = { node.actions["onActionClick"]?.let { action -> onAction(action) } }
        )
    }

    // 2. 房东选择器 (AirbnbHostSelector)
    SduiComponentRegistry.register("AirbnbHostSelector") { node, onAction ->
        val selectedHostId = node.properties["selectedHostId"] ?: sampleHost.id
        HostSelector(
            hosts = listOf(sampleHost),
            selectedHostId = selectedHostId,
            onHostSelected = { node.actions["onHostSelected"]?.let { action -> onAction(action) } }
        )
    }

    // 3. 房东主卡片 (AirbnbProfileHeroCard)
    SduiComponentRegistry.register("AirbnbProfileHeroCard") { node, _ ->
        val name = node.properties["name"] ?: sampleHost.name
        val superHost = node.properties["superHost"]?.toBooleanStrictOrNull() ?: sampleHost.superHost
        val rating = node.properties["rating"]?.toDoubleOrNull() ?: sampleHost.rating
        val reviewCount = node.properties["reviewCount"]?.toIntOrNull() ?: sampleHost.reviewCount
        val yearsHosting = node.properties["yearsHosting"]?.toIntOrNull() ?: sampleHost.yearsHosting
        val avatarUrl = node.properties["avatarUrl"] ?: sampleHost.avatarUrl

        val host = sampleHost.copy(
            name = name,
            superHost = superHost,
            rating = rating,
            reviewCount = reviewCount,
            yearsHosting = yearsHosting,
            avatarUrl = avatarUrl
        )
        ProfileHeroCard(host = host)
    }

    // 4. 通用容器卡片 (AirbnbSectionCard)
    SduiComponentRegistry.register("AirbnbSectionCard") { _, _ ->
        SectionCard { }
    }

    // 5. 详情行单项 (AirbnbDetailLine)
    SduiComponentRegistry.register("AirbnbDetailLine") { node, _ ->
        val icon = node.properties["icon"] ?: "🌐"
        val text = node.properties["text"] ?: "信息详情"
        DetailLine(icon = icon, text = text)
    }

    // 6. 模块小标题 (AirbnbSectionTitle)
    SduiComponentRegistry.register("AirbnbSectionTitle") { node, _ ->
        val titleText = node.properties["text"] ?: "小标题"
        SectionTitle(text = titleText)
    }

    // 7. 兴趣爱好区域 (AirbnbHobbiesSection)
    SduiComponentRegistry.register("AirbnbHobbiesSection") { node, onAction ->
        val hobbiesStr = node.properties["hobbies"] ?: "艺术展览,城市散步"
        val hobbiesList = hobbiesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        HobbiesSection(
            hobbies = hobbiesList,
            onAddHobby = { node.actions["onAddHobby"]?.let { action -> onAction(action) } },
            onDeleteHobby = { node.actions["onDeleteHobby"]?.let { action -> onAction(action) } }
        )
    }

    // 8. 去过的地点区域 (AirbnbPlacesSection)
    SduiComponentRegistry.register("AirbnbPlacesSection") { node, onAction ->
        val placesStr = node.properties["places"] ?: "东京,巴黎"
        val isVisible = node.properties["isVisible"]?.toBooleanStrictOrNull() ?: true
        val placesList = placesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        PlacesSection(
            places = placesList,
            isVisible = isVisible,
            onToggle = { node.actions["onToggle"]?.let { action -> onAction(action) } }
        )
    }

    // 9. 去过的地点卡片 (AirbnbDestinationStamp)
    SduiComponentRegistry.register("AirbnbDestinationStamp") { node, _ ->
        val name = node.properties["name"] ?: "东京"
        val emoji = node.properties["emoji"] ?: (destinationEmojis[name] ?: "📍")
        DestinationStamp(name = name, emoji = emoji)
    }

    // 10. 房东评价卡片 (AirbnbReviewCard)
    SduiComponentRegistry.register("AirbnbReviewCard") { node, _ ->
        val reviewerName = node.properties["reviewerName"] ?: "Yoshimi"
        val reviewerLocation = node.properties["reviewerLocation"] ?: "达拉斯"
        val reviewerAvatarUrl = node.properties["reviewerAvatarUrl"] ?: ""
        val stars = node.properties["stars"]?.toIntOrNull() ?: 5
        val dateText = node.properties["dateText"] ?: "2周前"
        val content = node.properties["content"] ?: "很喜欢这里，入住极其方便安全。"
        val review = HostReview(
            id = "r_sdui",
            hostId = sampleHost.id,
            reviewerName = reviewerName,
            reviewerLocation = reviewerLocation,
            reviewerAvatarUrl = reviewerAvatarUrl,
            stars = stars,
            dateText = dateText,
            content = content
        )
        ReviewCard(review = review)
    }

    // 11. 房源名片 (AirbnbListingCard)
    SduiComponentRegistry.register("AirbnbListingCard") { node, _ ->
        val title = node.properties["title"] ?: "酒店式公寓"
        val subtitle = node.properties["subtitle"] ?: "ArtRoom 6 - 睡眠舱女生共享空间"
        val rating = node.properties["rating"]?.toDoubleOrNull() ?: 4.84
        val reviewCount = node.properties["reviewCount"]?.toIntOrNull() ?: 88
        val imageUrl = node.properties["imageUrl"] ?: ""
        val listing = PropertyListing(
            id = "p_sdui",
            hostId = sampleHost.id,
            title = title,
            subtitle = subtitle,
            rating = rating,
            reviewCount = reviewCount,
            imageUrl = imageUrl
        )
        ListingCard(listing = listing)
    }

    // 12. 设置与行动列表项 (AirbnbActionItem)
    SduiComponentRegistry.register("AirbnbActionItem") { node, onAction ->
        val icon = node.properties["icon"] ?: "⚙️"
        val label = node.properties["label"] ?: "系统设置"
        ActionItem(
            icon = icon,
            label = label,
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } }
        )
    }

    // 13. 个人简介编辑区 (AirbnbAboutMeSection)
    SduiComponentRegistry.register("AirbnbAboutMeSection") { node, onAction ->
        val text = node.properties["text"] ?: ""
        val maxLength = node.properties["maxLength"]?.toIntOrNull() ?: 500
        AboutMeSection(
            text = text,
            maxLength = maxLength,
            onTextChange = { node.actions["onTextChange"]?.let { action -> onAction(action) } }
        )
    }

    // 14. 头像编辑区 (AirbnbAvatarSection)
    SduiComponentRegistry.register("AirbnbAvatarSection") { node, onAction ->
        val avatarUrl = node.properties["avatarUrl"] ?: ""
        AvatarSection(
            avatarUrl = avatarUrl,
            onAvatarClick = { node.actions["onAvatarClick"]?.let { action -> onAction(action) } }
        )
    }

    // 15. 个人资料单项信息栏 (AirbnbProfileFieldItem)
    SduiComponentRegistry.register("AirbnbProfileFieldItem") { node, onAction ->
        val label = node.properties["label"] ?: "职业"
        val value = node.properties["value"] ?: "艺术家"
        val showDivider = node.properties["showDivider"]?.toBooleanStrictOrNull() ?: true
        ProfileFieldItem(
            label = label,
            value = value,
            showDivider = showDivider,
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } }
        )
    }

    // 16. 开关切换栏 (AirbnbToggleItem)
    SduiComponentRegistry.register("AirbnbToggleItem") { node, onAction ->
        val title = node.properties["title"] ?: "超赞房东认证"
        val checked = node.properties["checked"]?.toBooleanStrictOrNull() ?: true
        ToggleItem(
            title = title,
            checked = checked,
            onCheckedChange = { node.actions["onCheckedChange"]?.let { action -> onAction(action) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AirbnbSduiRegistryPreview() {
    registerAirbnbSduiComponents()
}
