/**
 * @File: WeChatMpSduiRegistry.kt
 * @Package: org.example.project.ui.core.sdui.registry
 * @Description: 微信公众号模块全量SDUI动态组件适配与集中注册入口
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.sdui.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.data.repository.wechat.createMockFeaturedArticle
import org.example.project.data.repository.wechat.createMockWaterfallArticles
import org.example.project.data.repository.wechat.createMockWeChatAccounts
import org.example.project.ui.components.wechat.WeChatMpDislikeBottomSheet
import org.example.project.ui.components.wechat.WeChatMpFeaturedBannerCard
import org.example.project.ui.components.wechat.WeChatMpFrequentlyReadBar
import org.example.project.ui.components.wechat.WeChatMpHorizontalCard
import org.example.project.ui.components.wechat.WeChatMpTopBar
import org.example.project.ui.components.wechat.WeChatMpWaterfallCard
import org.example.project.ui.core.sdui.SduiComponentRegistry

/**
 * 集中注册微信公众号模块下所有可热更动态组件
 */
fun registerWeChatMpSduiComponents() {
    val sampleAccounts = createMockWeChatAccounts()
    val sampleFeatured = createMockFeaturedArticle()
    val sampleWaterfall = createMockWaterfallArticles()

    // 1. 顶部导航栏 (WeChatMpTopBar)
    SduiComponentRegistry.register("WeChatMpTopBar") { node, onAction ->
        val title = node.properties["title"] ?: "公众号"
        WeChatMpTopBar(
            title = title,
            onBackClick = { node.actions["onBackClick"]?.let { action -> onAction(action) } },
            onSearchClick = { node.actions["onSearchClick"]?.let { action -> onAction(action) } },
            onProfileClick = { node.actions["onProfileClick"]?.let { action -> onAction(action) } }
        )
    }

    // 2. 常读号滑动条 (WeChatMpFrequentlyReadBar)
    SduiComponentRegistry.register("WeChatMpFrequentlyReadBar") { node, onAction ->
        WeChatMpFrequentlyReadBar(
            accounts = sampleAccounts,
            onAccountClick = { node.actions["onAccountClick"]?.let { action -> onAction(action) } }
        )
    }

    // 3. 常读置顶大图头条卡片 (WeChatMpFeaturedBannerCard)
    SduiComponentRegistry.register("WeChatMpFeaturedBannerCard") { node, onAction ->
        WeChatMpFeaturedBannerCard(
            article = sampleFeatured,
            onArticleClick = { node.actions["onArticleClick"]?.let { action -> onAction(action) } },
            onMoreMessagesClick = { node.actions["onMoreMessagesClick"]?.let { action -> onAction(action) } },
            onMenuClick = { node.actions["onMenuClick"]?.let { action -> onAction(action) } }
        )
    }

    // 4. 单列左右图文与通栏大图卡片 (WeChatMpHorizontalCard)
    SduiComponentRegistry.register("WeChatMpHorizontalCard") { node, onAction ->
        val firstCard = sampleWaterfall.firstOrNull() ?: sampleFeatured
        WeChatMpHorizontalCard(
            article = firstCard,
            onArticleClick = { node.actions["onArticleClick"]?.let { action -> onAction(action) } },
            onDislikeClick = { node.actions["onDislikeClick"]?.let { action -> onAction(action) } }
        )
    }

    // 5. 双列瀑布流大图与视频卡片 (WeChatMpWaterfallCard)
    SduiComponentRegistry.register("WeChatMpWaterfallCard") { node, onAction ->
        val waterfallItem = sampleWaterfall.getOrNull(1) ?: sampleFeatured
        WeChatMpWaterfallCard(
            article = waterfallItem,
            onArticleClick = { node.actions["onArticleClick"]?.let { action -> onAction(action) } },
            onDislikeClick = { node.actions["onDislikeClick"]?.let { action -> onAction(action) } }
        )
    }

    // 6. 不感兴趣操作弹窗 (WeChatMpDislikeBottomSheet)
    SduiComponentRegistry.register("WeChatMpDislikeBottomSheet") { node, onAction ->
        WeChatMpDislikeBottomSheet(
            targetArticle = sampleFeatured,
            onSelectReason = { node.actions["onSelectReason"]?.let { action -> onAction(action) } },
            onDismiss = { node.actions["onDismiss"]?.let { action -> onAction(action) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpSduiRegistryPreview() {
    registerWeChatMpSduiComponents()
}
