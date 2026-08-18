/**
 * @File: DevMockWeChatData.kt
 * @Package: org.example.project.data.repository.wechat
 * @Description: 微信公众号信息流开发阶段高质量拟真测试数据集
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.wechat

import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.platform.currentTimeMillis

/**
 * 预置常读公众号列表
 */
fun createMockWeChatAccounts(): List<WeChatAccount> {
    return listOf(
        WeChatAccount(
            id = "acc_01",
            name = "钛媒体",
            avatarUrl = "https://picsum.photos/seed/taimeiti/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = true,
            signature = "创新连接商业与科技"
        ),
        WeChatAccount(
            id = "acc_02",
            name = "中国青年报",
            avatarUrl = "https://picsum.photos/seed/cydn/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = true,
            signature = "只为青年而生"
        ),
        WeChatAccount(
            id = "acc_03",
            name = "差评",
            avatarUrl = "https://picsum.photos/seed/chaping/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = true,
            signature = "差评，带你发现科技生活的另一面"
        ),
        WeChatAccount(
            id = "acc_04",
            name = "深圳大件事",
            avatarUrl = "https://picsum.photos/seed/szdjs/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = true,
            signature = "深圳本地权威资讯与大事件"
        ),
        WeChatAccount(
            id = "acc_05",
            name = "小红书技术",
            avatarUrl = "https://picsum.photos/seed/redtech/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = false,
            signature = "小红书官方技术团队"
        ),
        WeChatAccount(
            id = "acc_06",
            name = "极客公园",
            avatarUrl = "https://picsum.photos/seed/geekpark/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = true,
            signature = "创新者的前沿科技指南"
        ),
        WeChatAccount(
            id = "acc_07",
            name = "机器之心",
            avatarUrl = "https://picsum.photos/seed/jiqizhixin/120/120",
            isFollowed = true,
            isFrequentlyRead = true,
            hasUnread = false,
            signature = "专业的人工智能科技媒体与产业服务平台"
        )
    )
}

/**
 * 预置常读置顶头条卡片文章
 */
fun createMockFeaturedArticle(): WeChatArticle {
    val csdnAccount = WeChatAccount(
        id = "acc_csdn",
        name = "CSDN",
        avatarUrl = "https://picsum.photos/seed/csdn_avatar/120/120",
        isFollowed = true,
        isFrequentlyRead = true,
        hasUnread = false,
        signature = "专业开发者社区"
    )
    return WeChatArticle(
        id = "art_featured_01",
        account = csdnAccount,
        title = "iPhone17 本月或全球涨价，单台最高涨近千元；宇树科技发布“新人”：原地跳高2米，...",
        summary = "极客头条 技术人都在看的新闻！ NEWS",
        coverUrl = "https://picsum.photos/seed/csdn_banner/800/400",
        publishTimeText = "2分钟前",
        publishTimestamp = currentTimeMillis() - 120_000,
        cardType = WeChatCardType.FEATURED_BANNER,
        isFollowedAccount = true,
        readCount = 38400,
        likeCount = 1205,
        isTopSticky = true,
        coverAspectRatio = 2.1f
    )
}

/**
 * 预置看一看瀑布流混合卡片数据集
 */
fun createMockWaterfallArticles(): List<WeChatArticle> {
    val now = currentTimeMillis()
    return listOf(
        // 卡片 1: 单列左右图文 (腾讯投资)
        WeChatArticle(
            id = "art_waterfall_01",
            account = WeChatAccount(
                id = "acc_tencent_job",
                name = "腾讯招聘",
                avatarUrl = "https://picsum.photos/seed/tencent_logo/120/120",
                isFollowed = true
            ),
            title = "腾讯投资2027校招提前全面启动！",
            coverUrl = "https://picsum.photos/seed/tencent_q/300/300",
            publishTimeText = "15分钟前",
            publishTimestamp = now - 900_000,
            cardType = WeChatCardType.HORIZONTAL_ROW,
            isFollowedAccount = true,
            readCount = 18900,
            coverAspectRatio = 1.0f
        ),

        // 卡片 2: 双列瀑布流大图 (天津大学)
        WeChatArticle(
            id = "art_waterfall_02",
            account = WeChatAccount(
                id = "acc_tju_helper",
                name = "天津大学小助手",
                avatarUrl = "https://picsum.photos/seed/tju_logo/120/120",
                isFollowed = false
            ),
            title = "原则上不鼓励双非考天津大学",
            coverUrl = "https://picsum.photos/seed/tju_campus/400/520",
            publishTimeText = "1小时前",
            publishTimestamp = now - 3600_000,
            cardType = WeChatCardType.WATERFALL_GRID,
            isFollowedAccount = false,
            readCount = 45200,
            coverAspectRatio = 0.85f
        ),

        // 卡片 3: 双列瀑布流视频卡片 (丽水乡镇)
        WeChatArticle(
            id = "art_waterfall_03",
            account = WeChatAccount(
                id = "acc_global_times",
                name = "环球网",
                avatarUrl = "https://picsum.photos/seed/global_logo/120/120",
                isFollowed = false
            ),
            title = "丽水这4个乡镇将迎来大发展",
            coverUrl = "https://picsum.photos/seed/lishui_town/400/560",
            publishTimeText = "2小时前",
            publishTimestamp = now - 7200_000,
            cardType = WeChatCardType.VIDEO_CARD,
            isFollowedAccount = false,
            videoDuration = "01:28",
            readCount = 67300,
            coverAspectRatio = 0.82f
        ),

        // 卡片 4: 单列左右图文 (马斯克Grok4.7)
        WeChatArticle(
            id = "art_waterfall_04",
            account = WeChatAccount(
                id = "acc_ai_biz",
                name = "AI企服开发者圈",
                avatarUrl = "https://picsum.photos/seed/ai_biz_logo/120/120",
                isFollowed = false
            ),
            title = "马斯克凌晨预告：Grok4.7初步训练完成，2.1万亿参数+SpaceX数据...",
            coverUrl = "https://picsum.photos/seed/elon_grok/300/300",
            publishTimeText = "3小时前",
            publishTimestamp = now - 10800_000,
            cardType = WeChatCardType.HORIZONTAL_ROW,
            isFollowedAccount = false,
            readCount = 32100,
            coverAspectRatio = 1.0f
        ),

        // 卡片 5: 单列通栏大图卡片 (EMS录取通知书)
        WeChatArticle(
            id = "art_waterfall_05",
            account = WeChatAccount(
                id = "acc_ems_cn",
                name = "EMS中国邮政速递物流",
                avatarUrl = "https://picsum.photos/seed/ems_logo/120/120",
                isFollowed = true
            ),
            title = "先录取的通知书领五大补贴！速收藏",
            coverUrl = "https://picsum.photos/seed/ems_letter/800/420",
            publishTimeText = "4小时前",
            publishTimestamp = now - 14400_000,
            cardType = WeChatCardType.BANNER_LARGE,
            isFollowedAccount = true,
            readCount = 89000,
            coverAspectRatio = 1.9f
        ),

        // 卡片 6: 双列瀑布流大图 (Qwen 3.8开源)
        WeChatArticle(
            id = "art_waterfall_06",
            account = WeChatAccount(
                id = "acc_hali_uncle",
                name = "Hali大叔",
                avatarUrl = "https://picsum.photos/seed/hali_logo/120/120",
                isFollowed = false
            ),
            title = "阿里开源27B模型，干翻一种大厂！",
            coverUrl = "https://picsum.photos/seed/qwen_poster/400/500",
            publishTimeText = "5小时前",
            publishTimestamp = now - 18000_000,
            cardType = WeChatCardType.WATERFALL_GRID,
            isFollowedAccount = false,
            readCount = 52300,
            coverAspectRatio = 0.9f
        ),

        // 卡片 7: 双列瀑布流大图 (ChatGPT Mac红绿灯)
        WeChatArticle(
            id = "art_waterfall_07",
            account = WeChatAccount(
                id = "acc_ai_shuji",
                name = "差不多有的AI书记",
                avatarUrl = "https://picsum.photos/seed/shuji_logo/120/120",
                isFollowed = false
            ),
            title = "苹果连Mac的「红绿灯」界面都改了",
            coverUrl = "https://picsum.photos/seed/mac_traffic_light/400/480",
            publishTimeText = "6小时前",
            publishTimestamp = now - 21600_000,
            cardType = WeChatCardType.WATERFALL_GRID,
            isFollowedAccount = false,
            readCount = 41200,
            coverAspectRatio = 0.95f
        ),

        // 卡片 8: 单列左右图文 (WeChat Pay HK优惠)
        WeChatArticle(
            id = "art_waterfall_08",
            account = WeChatAccount(
                id = "acc_wechat_pay_hk",
                name = "WeChat Pay HK好生活",
                avatarUrl = "https://picsum.photos/seed/wc_hk_logo/120/120",
                isFollowed = true
            ),
            title = "银联卡商务卡首刷 | 最高HK$320优惠",
            coverUrl = "https://picsum.photos/seed/hk_card_promo/300/300",
            publishTimeText = "7小时前",
            publishTimestamp = now - 25200_000,
            cardType = WeChatCardType.HORIZONTAL_ROW,
            isFollowedAccount = true,
            readCount = 14500,
            coverAspectRatio = 1.0f
        ),

        // 卡片 9: 单列左右图文 (游研社)
        WeChatArticle(
            id = "art_waterfall_09",
            account = WeChatAccount(
                id = "acc_yys_games",
                name = "游研社",
                avatarUrl = "https://picsum.photos/seed/yys_logo/120/120",
                isFollowed = false
            ),
            title = "2026下半年，去过苦日子的五家游戏公司",
            coverUrl = "https://picsum.photos/seed/game_corp/300/300",
            publishTimeText = "8小时前",
            publishTimestamp = now - 28800_000,
            cardType = WeChatCardType.HORIZONTAL_ROW,
            isFollowedAccount = false,
            readCount = 76200,
            coverAspectRatio = 1.0f
        )
    )
}
