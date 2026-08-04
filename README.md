# Social KMP App

这是一个基于 **Kotlin Multiplatform (KMP)** 和 **Compose Multiplatform** 实现的跨平台社交应用程序示例，高度参考了 Instagram 的 UI/UX 设计，并内置了符合硅谷/国内大厂（Airbnb/Lyft/DoorDash）标准的 **SDUI（服务端驱动 UI）动态组件热更新架构**。

---

## 1. 项目概览

本项目旨在展示如何在 KMP 项目中组织复杂的 UI 模块、统一的 MVI 架构、导航逻辑以及 SDUI 动态组件热更新机制。目前包含两个核心业务模块：**Instagram** 和 **FeedLine**。

---

## 2. 项目结构

项目采用典型的 KMP 多模块分层结构：

- **`:androidApp`**: Android 原生宿主工程。
- **`:composeApp`**: 共享 UI 模块（`commonMain`），包含 Compose 页面、动态 UI 渲染引擎与组件注册表。
- **`:shared`**: 共享业务逻辑模块，包含：
  - **`core/sdui/`**: SDUI 节点模型 (`SduiNode`)、Kotlin DSL Builder 构建器、统一版本中心 (`SduiVersionConfig`)、三级缓存仓库 (`SduiLayoutRepositoryImpl`)。
  - **`data/`**: Repository 实现及数据源。
  - **`domain/`**: 领域模型与 UseCase 业务用例。
  - **`presentation/`**: 跨平台共享 ViewModel、MVI `UiState`、`UiIntent` 与 `Effect`。

---

## 3. 核心架构规范 (Unified MVI & SDUI)

### 3.1 MVI 架构原则
1. **单一状态源 (Single Source of Truth)**：每个页面由唯一的 `UiState` 对象描述。
2. **单向数据流 (UDF)**：UI 发射 `UiIntent` -> ViewModel 统一处理 -> 更新 `UiState` -> UI 响应。
3. **副作用处理 (Side Effects)**：使用 `Effect` (通过 `Channel`) 处理一次性事件，如弹窗提示、消息通知或页面跳转。

### 3.2 SDUI 动态热更架构与 5 阶段渲染流转
项目构建了符合 100% 商店合规要求、基于 Compose Multiplatform 的 SDUI 热更新框架：

```
[1. 组件集中注册] ➔ [2. 布局获取与3级兜底] ➔ [3. DSL节点树解析] ➔ [4. Compose动态递归渲染] ➔ [5. MVI事件闭环]
```

- **控制反转组件注册表 (`SduiComponentRegistry`)**：彻底隔绝渲染引擎与具体业务组件，内置 `exportRegisteredLayoutJson()` 提供一键自动拼装全量注册组件的 JSON DSL 功能。
- **模块集中式注册表 (`FeedLineSduiRegistry` / `InstagramSduiRegistry`)**：按业务模块集中收拢全量组件的 SDUI 节点解析与原生映射，杜绝文件碎片化。
- **统一版本管理 (`SduiVersionConfig`)**：集中统一配置所有大模块版本号及子组件版本号。
- **构建自动化与零硬编码**：Gradle `generateSduiJson` Task 在编译构建时动态解析 `SduiVersionConfig.kt` 中的版本号，自动导出“模块聚合 JSON”和“单组件独立 JSON”两种粒度产物（命名格式：`组件名_版本号_导出时间.json`）。
- **三级降级容灾机制**：加载顺序遵循 **内存缓存 -> 本地磁盘缓存 -> Asset 内置 JSON 打底模板**。服务端无响应或网络断开时无缝降级，保证 100% 打底渲染原生打包 UI，防空白防崩溃。
- **MVI Action 透传**：动态组件中的交互事件通过结构化 `SduiAction` 自动转换为 Native `UiIntent` 传递给 ViewModel 处理。

---

## 4. 核心业务模块

### 4.1 FeedLine 朋友圈模块
- **全量组件 SDUI 热更**：包含 15 个组件的集中热更支持与注册解耦。
- **多媒体与评论列表**：支持跨平台图片/视频多图展示与平滑点赞评论交互。

### 4.2 Instagram 模块
- **配置驱动导航与全量热更**：包含 16 个组件的集中热更支持与注册解耦。
- **高度定制化主题 (`InstagramTheme`)**：一套独立于 Material 3 默认样式的颜色与间距系统。

---

## 5. 技术栈与架构规范文档

- **UI 框架**: Compose Multiplatform (Android/Desktop/iOS)
- **SDUI 引擎**: 自研 Compose 动态递归渲染引擎、Kotlin DSL Builder 与零配置自动 JSON 导出器
- **导航**: Jetpack Navigation (Compose)
- **图片加载**: Coil3
- **文件选择**: FileKit
- **依赖注入/ViewModel**: AndroidX ViewModel (KMP 支持)
- **网络/序列化**: Ktor, Coroutines, kotlinx.serialization
- **架构规范文档**: [安卓端与 KMP 动态组件热更新架构约束规范 (SDUI)](file:///D:/coding/social-kmp-app/docs/architecture/%E5%AE%89%E5%8D%93%E5%8A%A8%E6%80%81%E7%BB%84%E4%BB%B6%E7%83%AD%E6%9B%B4%E6%8A%80%E6%9C%AF%E6%96%B9%E6%A1%88.md)

---

## 6. 运行与预览

- **Preview 支持**: 所有 UI 组件与 SDUI 注册表均配置了 `@Preview(showBackground = true)` 函数，支持在 Android Studio 中直接预览。
- **多端运行**: 支持 Android 端直接运行，桌面端可通过 `./gradlew :composeApp:run` 启动。
