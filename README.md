# Social KMP App

这是一个基于 **Kotlin Multiplatform (KMP)** 和 **Compose Multiplatform** 实现的跨平台社交应用程序示例，高度参考了 Instagram 的 UI/UX 设计。

## 📱 项目概览

本项目旨在展示如何在 KMP 项目中组织复杂的 UI 模块、导航逻辑以及共享业务逻辑。目前包含两个核心业务模块：**Instagram** 和 **FeedLine**。

---

## 📂 项目结构

项目采用典型的 KMP 多模块结构：

- **`:androidApp`**: Android 原生宿主工程。
- **`:composeApp`**: 共享 UI 模块（`commonMain`），包含大部分 Compose 页面、组件和导航定义。
- **`:shared`**: 共享业务逻辑模块，包含：
  - **Data**: Repository 实现及数据源。
  - **Domain**: 领域模型（Entity）。
  - **Presentation**: 跨平台共享的 ViewModel（基于 `androidx.lifecycle.ViewModel`）。

---

## 🏛️ 架构规范 (Unified MVI)

本项目统一采用 **MVI (Model-View-Intent)** 架构模式，以确保跨平台逻辑的一致性和 UI 状态的可预测性。

### 核心原则
1. **单一状态源 (Single Source of Truth)**：每个页面由唯一的 `UiState` 对象描述。
2. **单向数据流 (UDF)**：UI 发射 `Intent` -> ViewModel 处理 -> 更新 `UiState` -> UI 响应。
3. **副作用处理 (Side Effects)**：使用 `Effect` (通过 `Channel`) 处理一次性事件，如弹窗提示、消息通知或页面跳转。

### 工程标准
- **文件拆分**：`UiState`、`Intent`、`Effect` 必须定义在独立文件中，存放于 `presentation` 的对应子目录下（如 `state/`, `intent/`, `effect/`）。
- **状态封装**：推荐使用泛型 `UiState<T>` (包含 Idle, Loading, Success, Error) 来处理局部 UI 状态，避免布尔值地狱，增强 UI 的表达力。
- **业务解耦**：ViewModel 内部逻辑应尽可能通过 `UseCase` 封装，以便在不同平台间复用纯业务逻辑。

---

## 🏗️ 模块亮点

### 1. Instagram 模块
- **配置驱动导航 (`InstagramScreen`)**:
  - 使用 `sealed class` 定义路由，对象内封装了图标、标题及**点击行为 (`NavAction`)**。
  - 支持标准跳转 (`Navigate`) 和 弹窗逻辑 (`Popup`)。
- **高度定制化主题**:
  - 通过 `InstagramTheme` 实现了一套独立于 Material 3 默认样式的颜色与间距系统。

### 2. FeedLine 模块
- **极致组件化**:
  - UI 拆分为微小组件（如 `FeedLinePostItem`, `FeedLineActionBar`），极大地提升了在不同布局中的复用能力。
- **完整的 MVI 实践**:
  - 拥有最完善的 `Intent` 分发和 `Effect` 副作用处理流程。

---

## 🛠️ 技术栈

- **UI 框架**: Compose Multiplatform (Android/Desktop/iOS/...)
- **导航**: Jetpack Navigation (Compose)
- **图片加载**: Coil3
- **文件选择**: FileKit
- **依赖注入/ViewModel**: AndroidX ViewModel (KMP 支持)
- **网络/IO**: Ktor (可扩展), Coroutines, Serialization

---

## 🚀 运行与预览

- **Preview 支持**: 大部分核心页面均已配置 `@Preview` 函数，支持在 Android Studio 中直接查看 UI。
- **多端运行**: 支持 Android 端直接运行，桌面端可通过 `./gradlew :composeApp:run` 启动。
