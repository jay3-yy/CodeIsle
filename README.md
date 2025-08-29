# CodeIsle (重构版)


`CodeIsle` 是一个功能完备的 [OpenIsle](https://www.open-isle.com/) 论坛 Android 客户端。本项目的核心目标并非从零构建，而是展示如何将一个既有的、采用传统架构的应用，通过引入业界领先的技术栈和设计模式，**重构**为一个高性能、高内聚、低耦合且易于维护和扩展的现代化应用。



## 🌟 重构核心亮点 (Key Refactoring Highlights)

本次重构是一次彻底的技术革新，关键改进如下：

| 方面 (Aspect) | 旧项目 (Before Refactoring) | **重构后 (After Refactoring)** |
| :--- | :--- | :--- |
| **应用架构** | 传统 MVC/MVP 模式，逻辑耦合在 Activity 中 | **整洁架构 (Clean Architecture)** + **MVVM**，职责清晰，高度解耦 |
| **依赖注入** | 手动实例化依赖 | 全面采用 **Hilt** 进行依赖注入，实现控制反转 (IoC) |
| **异步编程** | 传统回调或 `AsyncTask` | 全面使用 **Kotlin 协程 (Coroutines)**，代码简洁且生命周期安全 |
| **UI & UX** | 标准布局，存在 UI Bug 和样式问题 | **Edge-to-Edge** 沉浸式设计，修复所有已知 UI Bug，布局样式统一美观 |
| **视图绑定** | `findViewById` | 全面采用 **View Binding**，代码更安全、更简洁 |
| **可维护性** | 低，修改一处可能影响多处 | **高**，各模块独立，易于扩展和测试 |
| **可测试性** | 极低，难以进行单元测试 | **高**，`domain` 和 `data` 层可轻松进行单元测试 |

-----

## ✨ 主要功能 (Features)

  * **动态内容流**: 以列表和网格两种布局展示帖子流，并支持一键切换。
  * **沉浸式体验**: 采用 Edge-to-Edge 设计，提供全屏沉浸式 UI。
  * **数据加载**: 支持下拉刷新和无限滚动加载更多帖子。
  * **内容浏览**: 查看帖子详情、评论以及用户发布的历史帖子和回复。
  * **分类浏览**: 通过侧滑菜单，可以筛选和浏览不同版块下的内容。

-----

## 🛠️ 技术栈与架构 (Tech Stack & Architecture)

### 技术栈 (Tech Stack)

  * **语言**: [Kotlin](https://kotlinlang.org/)
  * **架构模式**: MVVM (Model-View-ViewModel)
  * **依赖注入**: [Hilt](https://dagger.dev/hilt/)
  * **异步处理**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  * **网络请求**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/), [Gson](https://github.com/google/gson)
  * **UI**: Android Jetpack (View Binding, AppCompat, RecyclerView, Material Components), ConstraintLayout
  * **图片加载**: [Coil](https://coil-kt.github.io/coil/)
  * **Markdown 渲染**: [Markwon](https://noties.io/Markwon/)

### 架构 (Architecture)

本项目采用了**整洁架构 (Clean Architecture)**，将项目严格划分为三个核心层次：

  * **`:presentation`**: 负责 UI 和状态管理 (MVVM)。包含 Activities, ViewModels, 和 Adapters。
  * **`:domain`**: 包含核心业务逻辑和业务模型。通过 UseCases (用例) 封装功能，完全独立于 Android 框架。
  * **`:data`**: 负责所有数据的来源和管理。通过仓库模式 (Repository Pattern) 为上层提供统一的数据接口。

-----

## 🚀 快速开始 (Getting Started)

1.  **克隆此仓库**

    ```bash
    git clone https://github.com/jay3-yy/CodeIsle.git
    ```

    *(注意：仓库名已根据您的最新项目更新为 CodeIsle)*

2.  **在 Android Studio 中打开项目**
    使用最新版的 Android Studio 打开。

3.  **构建并运行应用**
    等待 Gradle 同步完成后，即可构建并运行。

-----

## 📄 许可证 (License)

本项目采用 [MIT 许可证](https://www.google.com/search?q=LICENSE) 开源。
