# Instagram KMP - 项目文档

这是一个基于 Kotlin Multiplatform (KMP) 和 Compose Multiplatform 实现的类 Instagram 应用程序。

## 🏗 项目架构 (MVI 设计模式)

本项目采用 MVI (Model-View-Intent) 架构模式，将 UI 层与业务逻辑层解耦。

### 1. 视图模块 (`composeApp`)
负责 UI 展示与用户交互。
- **screens/**: 完整页面组件 (如 `HomeScreen`, `DetailScreen`)。
- **components/**: 通用原子 UI 组件 (如 `PostItem`, `MyButton`)。
- **theme/**: 视觉风格定制 (颜色、字体、MaterialTheme)。
- **navigation/**: 路由与导航配置。
- **composeResources/**: 跨平台共享资源 (图标、图片)。

### 2. 业务模块 (`shared`)
包含核心业务逻辑与数据处理。
- **domain/model/**: 业务实体模型 (如 `Post`, `User`)。
- **presentation/**: 表现层逻辑。
    - **state/**: 视图状态定义 (State)。
    - **intent/**: 用户意图定义 (Intent)。
    - **viewmodel/**: MVI ViewModel 实现，处理 Intent 并更新 State。
- **platform/**: 平台特定接口声明 (expect/actual)。

---

## 📅 任务进展

### ✅ 已完成
- [x] 基础项目结构搭建。
- [x] Instagram 风格主题定制 (Colors, Typography, Theme)。
- [x] 基于 `navigation-compose` 的跨平台导航实现。
- [x] MVI 模式基础架构搭建。
- [x] 首页帖子列表 (Home Feed) 的数据模型与 ViewModel 实现。
- [x] `PostItem` 原子组件开发。

### 🚀 待办事项 (TODO)
- [ ] **图片加载集成**: 集成 Coil 或其他 KMP 图片加载库以展示真实图片。
- [ ] **底部导航栏**: 实现类似 Instagram 的底部五个主页签切换。
- [ ] **个人主页 (Profile)**: 开发个人信息展示及网格帖子列表。
- [ ] **快拍 (Stories)**: 实现首页顶部的快拍横向列表组件。
- [ ] **数据持久化**: 集成 SQLDelight 或 Room (KMP) 实现本地缓存。
- [ ] **网络请求**: 集成 Ktor 实现真实 API 调用。

---

## 🛠 开发规范 (注释规范)
本项目遵循统一的注释规范，涵盖文件头、主题、颜色、组件等，详见代码内注释。
- **文件头**: 包含 `@File`, `@Description`, `@Date`。
- **Compose**: 使用 KDoc 描述组件结构及参数。
- **MVI**: 明确区分 State, Intent 与处理逻辑。
