# Social KMP App

基于 **Kotlin Multiplatform (KMP)** 和 **Compose Multiplatform** 实现的跨平台社交应用框架，集成了 **Airbnb 个人资料与设置**、**FeedLine 朋友圈**、**Instagram 动态流**、**WeChatMp 微信公众号与看一看瀑布流** 四大核心业务模块，全量采用 **MVI 架构**、**Room KMP 生产级跨平台物理数据库（Local-First SWR）**、**Core Infrastructure 统一文件存储架构**、**统一图片与视频多级离线缓存**、**跨平台数据埋点与 PlatformLock 互斥安全**、**StartupGate 响应式启动门禁** 与 **SDUI（服务端驱动 UI）动态组件热更新架构**。

---

## 功能概览

- **Airbnb 房东主页与系统设置**：
  - **个人资料页**：头像、基本信息、兴趣爱好、去过的地点印章、评价卡片、房源列表、旅行指南。
  - **资料编辑页**：字段编辑（ModalBottomSheet）、头像更换、实时数据双向同步。
  - **完整设置页**：深色外观、关怀模式、存储与缓存清理、国家与语言设置、退出登录弹窗。
  - **Pad 大屏与多端适配**：WindowSizeClass 响应式布局、宽度约束与内容居中。
- **FeedLine 朋友圈模块**：
  - 图文/视频多媒体流、点赞互动、嵌套评论发表与删除、未读通知中心。
  - **相册媒体持久化落盘 (`MediaPersister`)**：用户选取或拍摄的图片/视频自动转存至应用私有持久目录，解决 Android `content://` 临时 URI 重启失效问题。
- **Instagram 社交动态流**：
  - Story 快拍横向列表、Carousel 多图轮播、个性化个人主页、沉浸式全屏图文。
- **WeChatMp 微信公众号与看一看瀑布流**：
  - **常读公众号头像条**：横向滑动列表、未读小绿点状态与点击消点响应。
  - **常读置顶头条卡片**：大图 Banner、阅读量/点赞数统计、发布时间与“更多消息”入口。
  - **看一看混合瀑布流**：单列左文右图（“关注的号”小蓝标）、单列通栏大图、双列垂直瀑布流大图、双列视频大图（带时长与播放蒙层）。
  - **不感兴趣负反馈持久化**：ModalBottomSheet 负反馈原因提交、即时剔除并持久化写入 `StorageArea.PERSISTENT/wechat_mp/dislikes.json`，刷新与重启后持续生效。
  - **全端断点自适应**：基于 WindowSizeClass 的 1~4 列自适应瀑布流与大屏宽度约束居中。
- **真实跨平台 Room / SQLite 本地优先数据库（Local-First SWR）**：
  - 生产环境彻底剔除 Fake DAO，基于 SQLite Bundled 驱动提供 `HostProfileDao`、`FeedLineDao`、`InstagramDao` 与 `WeChatMpDao` 响应式数据持久化。
  - 支持物理 `.db` 数据库跨实例、跨生命周期重新打开持久化恢复，具备完备的集成测试校验。
- **全局统一图片与视频多级离线缓存架构**：
  - **图片多级缓存 (`AppImageLoader`)**：统一收敛 Coil 3 引擎，提供 L1 动态 LRU 内存缓存（25% 堆内存）+ L2 物理磁盘缓存（256MB LRU）+ Ktor 3 网络拉取 + FileKit 本地解码。
  - **视频磁盘缓存 (`AppVideoCacheManager`)**：全应用统一 512MB 视频磁盘文件池，`VideoPlayer` 自动拦截网络视频，命中时 0ms 离线起播，未命中时后台静默预下载。
- **Core Infrastructure 统一文件存储架构 (`core/storage`)**：
  - **逻辑区域划分**：提供 `StorageArea.PERSISTENT`（长期持久数据）、`CACHE`（快照缓存与图片/视频离线缓存）与 `TEMPORARY`（临时文件）抽象，屏蔽 Android 与 iOS 物理目录差异。
  - **路径安全防护与防逃逸 (`StoragePathValidator`)**：防范绝对路径与目录穿越 (`..`)，确保文件操作严格限制在逻辑根目录内。
  - **原子写入与并发锁防护 (`DefaultFileStorage`)**：默认基于 `.tmp` 临时文件 swap 实现原子写 (`ATOMIC`)，并按区域提供 `Mutex` 互斥保护，强制运行于 `Dispatchers.IO`。
- **平台权威启动与响应式生命周期门禁 (`StartupGate`)**：
  - 平台宿主（Android `MainActivity` / iOS `MainViewController`）为唯一权威初始化入口。
  - `StartupGate` 拦截未就绪状态，底层单例初始化完成前提供优雅加载与重试视图，强断言拦截杜绝 NPE。
- **多线程并发安全 (`PlatformLock`) 与全链路数据埋点**：
  - 提供 `expect/actual` 平台级重入互斥锁（Android `ReentrantLock` / iOS `NSLock`）。
  - `AppAnalyticsManager` 使用快照隔离机制分发事件，彻底解决高并发多协程埋点数据竞争。
- **SDUI 动态热更新架构 (非阻塞 Suspend I/O)**：
  - 彻底消除阻塞式 `runBlocking` 磁盘 IO，采用纯协程挂起 API 与内存缓存互斥锁。
  - **自动导出 Task**：`generateSduiJson` 编译构建时根据 `SduiVersionConfig` 一键导出模块聚合 JSON 与单组件独立 JSON 至 `build/outputs/sdui/`。
- **120Hz 高刷新率适配**：
  - 动态请求 120Hz 高刷模式，不支持设备平滑降级至系统默认刷新率。

---

## 项目结构说明

```
social-kmp-app/
├── androidApp/     # Android 原生宿主工程（MainActivity、120Hz高刷申请与StartupGate门禁）
├── composeApp/     # 跨平台 Compose UI 视图、Screen 容器、多媒体缓存与 SDUI 渲染引擎
├── shared/         # 共享业务逻辑、真实 Room SQLite 数据库、Core 文件存储、MVI ViewModel、SDUI 核心与数据埋点
├── docs/           # 架构设计规范、文件存储架构文档、SDUI 方案文档与跨平台数据埋点技术文档
├── .github/        # GitHub Actions CI 工作流（Android / iOS 编译与跨平台测试）
└── gradle/         # 依赖版本目录 (libs.versions.toml)
```

### shared（共享业务逻辑 & 物理数据库 & 文件存储 & MVI & SDUI 核心）

```
shared/src/commonMain/kotlin/org/example/project/
├── core/
│   ├── analytics/                  # 跨平台数据埋点系统（PlatformLock 互斥与快照隔离）
│   │   ├── AnalyticsConfig.kt      # 埋点系统全局初始化配置项
│   │   ├── AnalyticsEvents.kt      # 全局埋点事件名称常量库
│   │   ├── AppAnalyticsManager.kt  # 全局数据埋点核心控制单例
│   │   └── IAnalyticsTracker.kt    # 埋点日志输出与上报抽象接口
│   ├── concurrent/                 # 跨平台并发锁基础设施
│   │   └── PlatformLock.kt         # 平台重入互斥锁 (Android: ReentrantLock, iOS: NSLock)
│   ├── config/                     # 全局假数据与后端联调开关配置
│   │   └── AppMockConfig.kt        # 假数据总开关与各模块独立 Mock 开关
│   ├── database/                   # 跨平台 Room / SQLite 本地物理数据库
│   │   ├── AppDatabase.kt          # 数据库抽象契约定义
│   │   ├── RealSqliteAppDatabase.kt# 基于 BundledSQLiteDriver 的真实跨平台 SQLite 驱动实现
│   │   └── DatabaseBuilder.kt      # 跨平台数据库构建句柄
│   ├── init/                       # 应用冷启动权威初始化管理器
│   │   └── AppInitializer.kt       # 集中编排 埋点 -> 存储 -> 网络 -> 数据库（强同步）与状态重置
│   ├── network/                    # Ktor 网络客户端与 API 抽象
│   │   ├── ApiEndpoints.kt         # API 网络请求路径与 SDUI 热更路由
│   │   ├── AppNetworkInitializer.kt# 跨平台 HTTP Client 初始化器（未初始化强断言）
│   │   └── NetworkContainer.kt     # 网络依赖提供容器
│   ├── sdui/                       # SDUI 核心数据结构、DSL Builder 与热更仓库（非阻塞 Suspend I/O）
│   │   ├── config/SduiVersionConfig.kt
│   │   ├── model/                  # AST 节点、Action 与 Style 模型（@Serializable）
│   │   ├── builder/SduiLayoutBuilder.kt
│   │   └── repository/SduiLayoutRepositoryImpl.kt # 纯挂起式 SDUI 本地与网络加载仓库
│   └── storage/                    # 统一文件存储基础设施 (Core Infrastructure)
│       ├── api/                    # 存储区域 (StorageArea)、路径 (StoragePath)、元数据与 FileStorage 契约
│       ├── client/                 # StorageContainer 依赖容器与 AppStorageInitializer 显式初始化器
│       ├── internal/               # StoragePathValidator 路径安全校验、FileSystemDriver (kotlinx-io) 驱动与 DefaultFileStorage 原子写并发锁
│       └── platform/               # StorageDirectories 平台物理目录抽象及 Android/iOS 物理映射实现
├── domain/
│   ├── model/                      # 领域实体数据模型（符合单一源原则）
│   │   ├── airbnb/HostProfileModels.kt
│   │   ├── feedline/FeedLinePost.kt / FeedLineUser.kt / FeedLineMedia.kt
│   │   ├── instagram/InstagramPost.kt / InstagramMedia.kt
│   │   └── wechat/WeChatAccount.kt / WeChatArticle.kt / WeChatCardType.kt
│   └── repository/                 # 数据仓库契约接口
├── data/
│   ├── database/                   # 数据库持久化层
│   │   ├── entity/                 # HostEntity / FeedLinePostEntity / InstagramPostEntity / WeChatArticleEntity
│   │   ├── converter/              # Room List<String> 字段 JSON 类型转换器
│   │   └── dao/                    # HostProfileDao / FeedLineDao / InstagramDao / WeChatMpDao 响应式接口
│   └── repository/                 # 数据仓库实现（SWR 本地优先 + SQLite DAO + FileStorage + Network）
│       ├── airbnb/HostProfileRepositoryImpl.kt
│       ├── feedline/FeedRepositoryImpl.kt
│       ├── instagram/InstagramHomeRepositoryImpl.kt
│       └── wechat/WeChatMpRepositoryImpl.kt # 支持不感兴趣持久化过滤与磁盘快照
└── presentation/                   # MVI 表现层架构核心
    ├── intent/                     # MVI Intent 用户意图密封接口
    ├── state/                      # MVI UiState 页面全局不可变状态
    ├── effect/                     # MVI Effect 单次副作用管道（Toast / Snackbar）
    └── viewmodel/                  # 共享 ViewModel（继承 androidx.lifecycle.ViewModel）
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
| Coil3 | 3.5.0 | 跨平台异步图片加载与多级缓存 (Memory 25% + Disk 256MB) |
| AndroidX ViewModel | 2.11.0 | 状态持久与生命周期感知 |
| FileKit | 0.14.2 | 跨平台文件与媒体选择器 |

---

## 构建与测试

```bash
# Android Debug 构建与校验
./gradlew :androidApp:assembleDebug

# 运行全量单元测试与集成测试（包含 Room 跨生命周期持久化、Startup 启动门禁、并发锁校验）
./gradlew :shared:testAndroidHostTest

# SDUI JSON 动态组件一键导出 Task（按组件名_版本号_时间戳格式自动导出）
./gradlew :shared:generateSduiJson
```

---

## 核心架构设计

### 1. MVI 架构统一规范 (Model-View-Intent)
- **单一状态源 (`UiState`)**：页面全局状态由不可变的 `StateFlow<UiState>` 维护。
- **单向数据流 (UDF)**：UI 事件驱动 `UiIntent` -> ViewModel 响应处理 `handleIntent()` -> 更新 `UiState`。
- **单次副作用 (`Effect`)**：Snackbar、Toast 及页面跳转事件通过 `Channel<Effect>` 发送。

### 2. 真实跨平台 SQLite / Room 持久化与 SWR 管道
- 生产环境统一接入 `RealSqliteAppDatabase` 与原生 SQLite 驱动引擎，杜绝假数据 DAO。
- 页面优先从本地数据库加载并响应式渲染，后台静默拉取网络并增量入库同步。

### 3. 全局统一图片与视频多级离线缓存
- **图片缓存 (`AppImageLoader`)**：统一收敛 Coil 3 实例，挂载 25% 堆内存 `MemoryCache` 与 256MB 磁盘 `DiskCache`。
- **视频缓存 (`AppVideoCacheManager`)**：统一 512MB 视频磁盘文件池，`VideoPlayer` 自动拦截，命中时 0ms 本地起播，未命中时后台静默预下载。
- **媒体持久化 (`MediaPersister`)**：相册选取与拍摄的媒体在发布前统一持久化落盘至私有沙盒，入库绝对物理路径。

### 4. Core Infrastructure 统一文件存储架构 (`core/storage`)
- **逻辑区域划分**：`PERSISTENT`（长期持久数据与相册媒体）、`CACHE`（快照缓存与图片/视频离线缓存）、`TEMPORARY`（上传/下载/解压临时文件）。
- **安全防逃逸与原子写入**：所有路径经由 `StoragePathValidator` 防范目录穿越，`DefaultFileStorage` 默认采用 `.tmp` 原子替换，保障数据绝对完整。

### 5. 平台权威启动与生命周期门禁 (`StartupGate`)
- 启动收敛至 Android `MainActivity` 与 iOS `MainViewController`。
- `StartupGate` 在核心基础设施（Analytics ➔ Storage ➔ Network ➔ Database）准备就绪前展示品牌加载或失败重试视图，彻底杜绝未初始化访问导致的崩溃。
