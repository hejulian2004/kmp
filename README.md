# Social KMP App

基于 **Kotlin Multiplatform (KMP)** 和 **Compose Multiplatform** 实现的跨平台社交应用框架，集成了 **Airbnb 个人资料与设置**、**FeedLine 朋友圈**、**Instagram 动态流** 三大核心业务模块，全量采用 **MVI 架构**、**Room KMP 本地离线数据库（Local-First SWR）**、**Core Infrastructure 统一文件存储架构**、**跨平台数据埋点架构** 与 **SDUI（服务端驱动 UI）动态组件热更新架构**。

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
- **Core Infrastructure 统一文件存储架构 (`core/storage`)**：
  - **平台物理隔离与逻辑区域**：提供 `StorageArea.PERSISTENT`（持久数据）、`CACHE`（缓存）与 `TEMPORARY`（临时文件）抽象，屏蔽 Android (`filesDir`/`cacheDir`) 与 iOS (`Application Support`/`Caches`/`Temp`) 物理目录差异。
  - **路径安全防护与防逃逸 (`StoragePathValidator`)**：防范绝对路径与目录穿越 (`..`)，确保文件操作严格限制在逻辑根目录内。
  - **原子写入与并发锁防护 (`DefaultFileStorage`)**：默认基于 `.tmp` 临时文件 swap 实现原子写 (`ATOMIC`)，并按 Path 粒度提供 `Mutex` 并发锁保护，IO 操作统一在 `Dispatchers.IO` 下执行。
  - **测试 Mock 扩展**：提供 `FakeFileStorage` 内存实现，方便 Domain 与 Repository 进行纯粹单元测试。
- **跨平台统一初始化与数据埋点架构**：
  - `AppInitializer` 统一管理冷启动依赖链：强同步完成 数据埋点单例 ➔ 文件存储架构 ➔ 网络架构核心 ➔ Room 数据库 ➔ 冷启动事件上报，确保底层基础设施完全准备就绪。
  - 后台非阻塞异步协程并发预加载拉取 SDUI 热更 JSON 布局。
- **SDUI 动态热更新架构**：
  - **零包内打底 JSON**：无热更或断网时无缝回退至 APK 打包的原生 Compose UI（`getLayout` 返回 `null`）；服务端下发热更后通过 `FileStorage` 持久化至 `sdui/<module>/layout.json` 本地磁盘与内存，实现动态 UI 覆盖渲染。
  - **模块集中注册表**：`AirbnbSduiRegistry`、`FeedLineSduiRegistry` 与 `InstagramSduiRegistry` 零侵入绑定原生 UI 与 MVI Action。
  - **自动导出 Task**：`generateSduiJson` 编译构建时根据 `SduiVersionConfig` 一键导出模块聚合 JSON 与单组件独立 JSON 至 `build/outputs/sdui/`。
- **120Hz 高刷新率适配**：
  - 动态请求 120Hz 高刷模式，不支持设备平滑降级至系统默认刷新率。

---

## 项目结构说明

```
social-kmp-app/
├── androidApp/     # Android 原生宿主工程（Activity、120Hz高刷申请与全局AppInitializer入口）
├── composeApp/     # 跨平台 Compose UI 视图、Screen 容器与 SDUI 渲染引擎
├── shared/         # 共享业务逻辑、Room KMP 数据库、Core 文件存储、MVI ViewModel、SDUI 核心与数据埋点
├── docs/           # 架构设计规范、文件存储架构文档、SDUI 方案文档与跨平台数据埋点技术文档
└── gradle/         # 依赖版本目录 (libs.versions.toml)
```

### shared（共享业务逻辑 & 数据库 & 文件存储 & MVI & SDUI 核心）

```
shared/src/commonMain/kotlin/org/example/project/
├── core/
│   ├── analytics/                  # 跨平台数据埋点系统
│   │   ├── AnalyticsConfig.kt      # 埋点系统全局初始化配置项
│   │   ├── AnalyticsEvents.kt      # 全局埋点事件名称常量库
│   │   ├── AppAnalyticsManager.kt  # 全局数据埋点核心控制单例
│   │   ├── IAnalyticsTracker.kt    # 埋点日志输出与上报抽象接口
│   │   └── LogAnalyticsTracker.kt  # 控制台控制流日志埋点实现类
│   ├── database/                   # Room KMP 本地离线数据库配置
│   │   ├── AppDatabase.kt          # 全局 Room 数据库定义（HostProfileDao / FeedLineDao / InstagramDao）
│   │   └── DatabaseBuilder.kt      # 跨平台 Room 数据库构建入口句柄
│   ├── init/                       # 应用冷启动统一初始化管理器
│   │   └── AppInitializer.kt       # 集中编排 埋点 -> 存储 -> 网络 -> 数据库（强同步）与 SDUI 布局拉取（异步后台执行）
│   ├── network/                    # Ktor 网络客户端与 API 抽象
│   │   ├── ApiEndpoints.kt         # API 网络请求路径与 SDUI 热更路由
│   │   ├── AppNetworkInitializer.kt# 跨平台 HTTP Client 初始化器
│   │   └── NetworkContainer.kt     # 网络依赖提供容器
│   ├── sdui/                       # SDUI 核心数据结构、DSL Builder 与热更仓库
│   │   ├── config/
│   │   │   └── SduiVersionConfig.kt# SDUI 模块及下属子组件版本号统一集中配置文件
│   │   ├── model/
│   │   │   ├── SduiNode.kt         # SDUI AST 内存节点模型（@Serializable）
│   │   │   ├── SduiAction.kt       # SDUI 节点交互事件 Action 模型（@Serializable）
│   │   │   └── SduiStyle.kt        # SDUI 节点样式描述模型（@Serializable）
│   │   ├── builder/
│   │   │   └── SduiLayoutBuilder.kt# 强类型 Kotlin SDUI DSL 节点构建器句柄
│   │   └── repository/
│   │       └── SduiLayoutRepositoryImpl.kt # SDUI 热更 JSON 本地 FileStorage 磁盘/内存缓存与网络下载仓库实现
│   └── storage/                    # 统一文件存储基础设施 (Core Infrastructure)
│       ├── api/                    # 存储区域 (StorageArea)、路径 (StoragePath)、元数据 (StorageMetadata) 与 FileStorage 契约
│       ├── client/                 # StorageContainer 依赖容器与 AppStorageInitializer 显式单例初始化器
│       ├── internal/               # StoragePathValidator 路径安全校验、FileSystemDriver (kotlinx-io) 驱动与 DefaultFileStorage 原子写并发锁
│       ├── platform/               # StorageDirectories 平台物理目录抽象及 Android/iOS 物理映射实现
│       └── testing/                # FakeFileStorage 内存模拟实现，供单元测试消费
├── domain/
│   ├── model/                      # 领域实体数据模型（符合单一源原则）
│   │   ├── airbnb/
│   │   │   └── HostProfileModels.kt# Airbnb 房东、房源、评价与指南模型
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
│   │   ├── entity/                 # Room Entity 表实体定义
│   │   │   ├── airbnb/             # HostEntity / PropertyListingEntity / HostReviewEntity / TravelGuideEntity
│   │   │   ├── feedline/           # FeedLinePostEntity / FeedLineNotificationEntity
│   │   │   └── instagram/          # InstagramPostEntity
│   │   ├── converter/
│   │   │   └── StringListConverter.kt # Room List<String> 字段 JSON 类型转换器
│   │   └── dao/                    # Room DAO 访问接口及响应式 SQL 实现
│   │       ├── airbnb/             # HostProfileDao & HostProfileDaoImpl
│   │       ├── feedline/           # FeedLineDao & FeedLineDaoImpl
│   │       └── instagram/          # InstagramDao & InstagramDaoImpl
│   └── repository/                 # 数据仓库实现（SWR 本地优先 + Room DAO + Network）
│       ├── airbnb/HostProfileRepositoryImpl.kt
│       ├── feedline/FeedRepositoryImpl.kt
│       └── instagram/InstagramHomeRepositoryImpl.kt
└── presentation/                   # MVI 表现层架构核心
    ├── intent/                     # MVI Intent 用户意图密封接口
    │   ├── airbnb/                 # HostProfileIntent / ProfileEditIntent / SettingsIntent
    │   ├── feedline/               # FeedLineIntent
    │   └── instagram/              # InstagramIntent
    ├── state/                      # MVI UiState 页面全局不可变状态
    │   ├── airbnb/                 # HostProfileUiState / ProfileEditUiState / SettingsUiState
    │   ├── feedline/               # FeedLineUiState
    │   └── instagram/              # InstagramUiState
    ├── effect/                     # MVI Effect 单次副作用管道（Toast / Snackbar）
    └── viewmodel/                  # 共享 ViewModel（继承 androidx.lifecycle.ViewModel）
        ├── airbnb/                 # HostProfileViewModel / ProfileEditViewModel / SettingsViewModel
        ├── feedline/               # FeedLineViewModel
        └── instagram/              # InstagramViewModel
```

---

## 技术栈与依赖版本

| 依赖库 / 技术 | 版本 | 用途 |
|---|---|---|
| Kotlin | 2.4.10 | 跨平台语言核心 |
| Compose Multiplatform | 1.7.3 | 声明式跨平台 UI 框架 |
| kotlinx-io | 0.6.0 | 跨平台底座文件系统 IO 操作 |
| Room KMP | 2.7.0-alpha13 | 本地数据库 ORM 与 SWR 持久化 |
| SQLite Bundled | 2.5.0-alpha13 | 跨平台原生 SQLite 驱动 |
| Ktor Client | 3.5.1 | 跨平台 HTTP 网络请求 |
| kotlinx-serialization | 1.11.0 | JSON 序列化与反序列化 |
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

# SDUI JSON 动态组件一键导出 Task（按组件名_版本号_时间戳格式自动导出）
./gradlew :shared:generateSduiJson

# 运行单元测试 (包含 FileStorage 与系统 DAO 校验)
./gradlew :shared:testAndroidHostTest
```

---

## 核心架构设计

### 1. MVI 架构统一规范 (Model-View-Intent)
- **单一状态源 (`UiState`)**：页面全局状态由不可变的 `StateFlow<UiState>` 维护。
- **单向数据流 (UDF)**：UI 事件驱动 `UiIntent` -> ViewModel 响应处理 `handleIntent()` -> 更新 `UiState`。
- **单次副作用 (`Effect`)**：Snackbar、Toast 及页面跳转事件通过 `Channel<Effect>` 发送。

### 2. Core Infrastructure 统一文件存储架构 (`core/storage`)
遵循 `docs/architecture/文件存储架构.md` 规范：
- **逻辑存储区域划分**：`PERSISTENT`（草稿/SDUI JSON/长期持久数据）、`CACHE`（网络/图片可重新生成缓存）、`TEMPORARY`（上传/下载/解压临时文件）。
- **安全与防逃逸**：业务层严禁直接操作物理路径或 Java `File`/iOS `NSURL`，所有路径必须经过 `StoragePathValidator` 过滤，拦截绝对路径与 `..` 穿越。
- **原子写与并发锁**：`DefaultFileStorage` 在 `Dispatchers.IO` 下通过临时写与原子移动机制 (`WriteMode.ATOMIC`) 保证数据可靠性，并按 Path 粒度使用 `Mutex` 防止并发竞争。

### 3. SWR 本地优先数据同步管道 (Stale-While-Revalidate)
遵循 `docs/architecture/数据同步与离线策略.md` 规范：
- 页面优先从 **Room KMP 本地数据库** 加载数据并响应式渲染，保证秒开与离线可用。
- 后台自动拉取最新 API 数据，校验变更后无缝写入 Room DB，流转更新 UI。

### 4. 应用统一初始化与数据埋点架构
遵循 `docs/architecture/数据埋点技术文档.md` 规范：
- `AppInitializer` 统一管理冷启动依赖链：强同步完成 `AppAnalyticsManager` ➔ `AppStorageInitializer` ➔ `AppNetworkInitializer` ➔ `getRoomDatabase` ➔ 冷启动埋点上报。
- 基础设施准备就绪后，启动异步协程在后台并发预加载拉取全量模块的 SDUI 热更 JSON 布局，绝对不阻塞主线程。

### 5. SDUI 动态组件热更新机制
遵循 `docs/architecture/安卓动态组件热更技术方案.md` 与 `sdui-hotupdate` 规范：
- **零包内打底 JSON 原则**：包内无需存储默认 JSON 模板。当无热更或请求失败时，`getLayout` 返回 `null` 并直接绘制原生 Compose UI；服务端下发热更后通过 `FileStorage` 持久化至磁盘与内存，启用 SDUI 递归渲染。
- **单一集中注册表**：`AirbnbSduiRegistry`、`FeedLineSduiRegistry` 与 `InstagramSduiRegistry` 分模块集中注册，保持 Native 组件纯净。
- **自动编译 Task**：`generateSduiJson` 任务按 `模块/组件名_版本号_时间戳.json` 自动导出热更 DSL 文件。

### 6. 120Hz 高刷新率适配与全端大屏响应式
- **120Hz 动态申请**：Android 宿主主动申请设备最高 120Hz/90Hz 刷新率，不支持的设备自动平滑降级至系统默认帧率。
- **WindowSizeClass 适应**：对 Pad 及折叠屏大屏设备实施 `widthIn(max=840dp)` 水平居中约束与双窗格扩展。

---

## 预览与 UI 规范

- **强制 `@Preview` 规范**：所有 `ui/components/` 和 `ui/screens/` 中的 UI 组件末尾均提供 `@Preview(showBackground = true)` 预览函数。
- **代码注释与中英文排版规范**：注释使用中文阐述逻辑意图，保留英文字符与符号。中英文与代码符号之间严禁添加无意义空格（无空格紧凑排版）。
