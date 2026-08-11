# Social KMP App

基于 **Kotlin Multiplatform (KMP)** 和 **Compose Multiplatform** 实现的跨平台社交应用框架，集成了 **Airbnb 个人资料与设置**、**FeedLine 朋友圈**、**Instagram 动态流** 三大核心业务模块，全量采用 **MVI 架构**、**Room KMP 本地离线数据库（Local-First SWR）** 与 **SDUI（服务端驱动 UI）动态组件热更新架构**。

---

## 功能概览

- **Airbnb 房东主页与系统设置**：
  - **个人资料页**：头像、基本信息、兴趣爱好、去过的地点印章、评价卡片、房源列表、旅行指南。
  - **资料编辑页**：字段编辑（ModalBottomSheet）、头像更换、实时数据双向同步。
  - **完整设置页**：深色外观、关怀模式、存储与缓存清理、国家与语言设置、退出登录弹窗。
  - **Pad 大屏与多端适配**：WindowSizeClass 响应式布局、宽度约束与内容居中。
- **FeedLine 朋友圈模块**：
  - 图文/视频多媒体流、点赞互动、嵌套评论发表与删除、未读通知中心。
- **Instagram 社交动态流**：
  - Story 快拍横向列表、Carousel 多图轮播、个性化个人主页、沉浸式全屏图文。
- **Room KMP 离线优先数据库（Local-First SWR）**：
  - 基于 Room KMP 2.7.0+ 统一封装 `AppDatabase`，提供 `HostProfileDao`、`FeedLineDao` 与 `InstagramDao` 响应式数据持久化。
  - 实现 SWR（Stale-While-Revalidate）数据同步管道，离线即时渲染与后台增量刷新。
- **SDUI 动态热更新架构**：
  - 符合 100% 商店合规要求，内置三级容灾缓存与自动导出编译 Task `generateSduiJson`。
- **120Hz 高刷新率适配**：
  - 动态请求 120Hz 高刷模式，不支持设备平滑降级至系统默认刷新率。

---

## 项目结构

```
social-kmp-app/
├── androidApp/     # Android 原生宿主工程
├── composeApp/     # 跨平台 Compose UI 视图与 Screen 容器
├── shared/         # 共享业务逻辑、Room KMP 数据库、MVI ViewModel 与领域模型
├── docs/           # 架构设计规范与 SDUI 方案文档
└── gradle/         # 依赖版本目录 (libs.versions.toml)
```

### shared（共享业务逻辑 & 数据库 & MVI ViewModel）

```
shared/src/commonMain/kotlin/org/example/project/
├── core/
│   ├── database/                   # Room KMP 本地数据库配置
│   │   ├── AppDatabase.kt          # 全局数据库定义（HostProfileDao / FeedLineDao / InstagramDao）
│   │   └── DatabaseBuilder.kt      # 跨平台 Room 数据库构建入口
│   ├── data/
│   │   └── NetworkBoundResource.kt # SWR 离线优先网络与数据库同步管道
│   └── network/                    # Ktor 客户端配置与 API 抽象
├── domain/
│   ├── model/                      # 领域数据模型
│   │   ├── airbnb/
│   │   │   └── HostProfileModels.kt# Airbnb 房东、房源、评价、指南模型
│   │   ├── feedline/
│   │   │   ├── FeedLinePost.kt     # 朋友圈动态模型
│   │   │   ├── FeedLineUser.kt     # 朋友圈用户模型
│   │   │   ├── FeedLineComment.kt  # 评论模型
│   │   │   ├── FeedLineMedia.kt    # 媒体类型模型
│   │   │   └── FeedLineNotification.kt # 通知模型
│   │   └── instagram/
│   │       ├── InstagramPost.kt    # Instagram 动态与 Story 统一模型
│   │       ├── InstagramProfileUser.kt # 个人资料模型
│   │       ├── InstagramComment.kt # 评论模型
│   │       └── InstagramMedia.kt   # 媒体模型
│   └── repository/                 # 数据仓库契约接口
│       ├── airbnb/HostProfileRepository.kt
│       ├── feedline/FeedLineRepository.kt
│       └── instagram/InstagramHomeRepository.kt
├── data/
│   ├── database/                   # Room KMP 本地数据库持久化层
│   │   ├── entity/                 # Room Entity 实体
│   │   │   ├── airbnb/             # HostEntity / PropertyListingEntity / HostReviewEntity / TravelGuideEntity
│   │   │   ├── feedline/           # FeedLinePostEntity / FeedLineNotificationEntity
│   │   │   └── instagram/          # InstagramPostEntity
│   │   ├── converter/
│   │   │   └── StringListConverter.kt # List<String> 转换器
│   │   └── dao/                    # Room DAO 接口及响应式实现
│   │       ├── airbnb/             # HostProfileDao & HostProfileDaoImpl
│   │       ├── feedline/           # FeedLineDao & FeedLineDaoImpl
│   │       └── instagram/          # InstagramDao & InstagramDaoImpl
│   └── repository/                 # 数据仓库实现（SWR + Room DAO + Network）
│       ├── airbnb/HostProfileRepositoryImpl.kt
│       ├── feedline/FeedRepositoryImpl.kt
│       └── instagram/InstagramHomeRepositoryImpl.kt
└── presentation/                   # MVI 表现层核心
    ├── intent/                     # MVI Intent 意图
    │   ├── airbnb/                 # HostProfileIntent / ProfileEditIntent / SettingsIntent
    │   ├── feedline/               # FeedLineIntent
    │   └── instagram/              # InstagramIntent
    ├── state/                      # MVI UiState 页面状态
    │   ├── airbnb/                 # HostProfileUiState / ProfileEditUiState / SettingsUiState
    │   ├── feedline/               # FeedLineUiState
    │   └── instagram/              # InstagramUiState
    ├── effect/                     # MVI Effect 一次性副作用管道
    └── viewmodel/                  # 共享 ViewModel（继承 androidx.lifecycle.ViewModel）
        ├── airbnb/                 # HostProfileViewModel / ProfileEditViewModel / SettingsViewModel
        ├── feedline/               # FeedLineViewModel
        └── instagram/              # InstagramViewModel
```

### composeApp（UI 视图与界面组装）

```
composeApp/src/commonMain/kotlin/org/example/project/
├── App.kt                          # 根 Composable；主导航路由（Airbnb / FeedLine / Instagram）
├── ui/
│   ├── screens/                    # 页面容器（符合 MVI 架构）
│   │   ├── airbnb/                 # Airbnb 页面
│   │   │   ├── AirbnbMainScreen.kt # Airbnb 子路由切换容器
│   │   │   ├── HostProfileScreen.kt# 房东主页 Screen
│   │   │   ├── HostEditScreen.kt   # 资料编辑 Screen
│   │   │   └── SettingsScreen.kt   # 设置主页 Screen
│   │   ├── feedline/               # FeedLine 朋友圈 Screen
│   │   └── instagram/              # Instagram 主页与个人 Screen
│   ├── components/                 # 独立可复用 UI 组件（均含 @Preview）
│   │   ├── airbnb/                 # Airbnb 专属组件
│   │   │   ├── ProfileHeroCard.kt  # 房东 Hero 头部卡片
│   │   │   ├── ListingCard.kt      # 房源卡片
│   │   │   ├── ReviewCard.kt       # 评价卡片
│   │   │   ├── AboutMeSection.kt   # 个人简介编辑组件
│   │   │   ├── HobbiesSection.kt   # 兴趣爱好 Flow 标签
│   │   │   ├── PlacesSection.kt    # 去过的地点印章卡片
│   │   │   ├── AvatarSection.kt    # 头像编辑组件
│   │   │   ├── EditFieldBottomSheet.kt # 底部编辑弹窗
│   │   │   ├── TopBar.kt           # 顶部导航栏
│   │   │   ├── HostSelector.kt     # 多房东切换选择器
│   │   │   ├── SectionCard.kt      # 通用圆角卡片容器
│   │   │   ├── ActionItem.kt       # 可点击设置项
│   │   │   └── ToggleItem.kt       # 通用开关组件
│   │   ├── feedline/               # FeedLine 专属组件
│   │   └── instagram/              # Instagram 专属组件
│   └── theme/                      # 设计系统与配色
│       ├── airbnb/                 # AirbnbTheme & AirbnbColor
│       ├── feedline/               # FeedLineTheme
│       └── instagram/              # InstagramTheme
└── ui/core/sdui/                   # SDUI 动态渲染引擎与组件注册表
```

---

## 技术栈与依赖版本

| 依赖库 / 技术 | 版本 | 用途 |
|---|---|---|
| Kotlin | 2.4.10 | 跨平台语言核心 |
| Compose Multiplatform | 1.7.3 | 声明式跨平台 UI 框架 |
| Room KMP | 2.7.0-alpha13 | 本地数据库 ORM 与 SWR 持久化 |
| SQLite Bundled | 2.5.0-alpha13 | 跨平台原生 SQLite 驱动 |
| Ktor Client | 3.1.0 | 跨平台 HTTP 网络请求 |
| kotlinx-serialization | 1.7.3 | JSON 序列化与反序列化 |
| Coil3 | 3.5.0 | 跨平台异步图片加载与缓存 |
| AndroidX ViewModel | 2.11.0 | 状态持久与生命周期感知 |
| FileKit | 0.14.2 | 跨平台文件与媒体选择器 |

---

## 构建与运行

```bash
# Android Debug 构建与校验
./gradlew :androidApp:assembleDebug

# 全量模块构建
./gradlew assembleDebug

# SDUI JSON 动态组件一键导出 Task
./gradlew :shared:generateSduiJson
```

---

## 核心架构设计

### 1. MVI 架构统一规范 (Model-View-Intent)
- **单一状态源 (`UiState`)**：页面全局状态由不可变的 `StateFlow<UiState>` 维护。
- **单向数据流 (UDF)**：UI 事件驱动 `UiIntent` -> ViewModel 响应处理 `handleIntent()` -> 更新 `UiState`。
- **单次副作用 (`Effect`)**：Snackbar、Toast 及页面跳转事件通过 `Channel<Effect>` 发送。

### 2. SWR 本地优先数据同步管道 (Stale-While-Revalidate)
遵循 `docs/architecture/数据同步与离线策略.md` 规范：
- 页面优先从 **Room KMP 本地数据库** 加载数据并响应式渲染，保证秒开与离线可用。
- 后台自动拉取最新 API 数据，校验变更后无缝写入 Room DB，流转更新 UI。

### 3. SDUI 动态组件热更新机制
- **5 阶段渲染流转**：组件集中注册 ➔ 3 级容灾缓存获取 ➔ DSL 解析 ➔ 动态递归渲染 ➔ MVI 事件响应。
- **自动编译 Task**：`generateSduiJson` 根据版本配置导出模块聚合 JSON 与单组件独立 JSON。

### 4. 120Hz 高刷新率适配与全端大屏响应式
- **120Hz 动态申请**：Android 宿主主动申请设备最高 120Hz/90Hz 刷新率，不支持的设备自动平滑降级至系统默认帧率。
- **WindowSizeClass 适应**：对 Pad 及折叠屏大屏设备实施 `widthIn(max=840dp)` 水平居中约束与双窗格扩展。

---

## 预览与 UI 规范

- **强制 `@Preview` 规范**：所有 `ui/components/` 和 `ui/screens/` 中的 UI 组件末尾均提供 `@Preview(showBackground = true)` 预览函数。
- **代码规范与标准头**：全量保留完整的作者、日期及文件头注释，遵循中文与英文/代码符号之间无缝无多余空格的排版习惯。
