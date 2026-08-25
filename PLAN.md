# F1 Watch (极速赛历) 开发与实施计划

本项目是一款面向 **Android 13+** 与 **小米澎湃OS (Xiaomi HyperOS 1.0+)** 的现代化 F1 赛程与桌面小组件应用。采用 **Kotlin + Jetpack Compose + Jetpack Glance** 纯原生技术栈，遵循 F1 官方视觉规范，提供赛历、赛果、积分榜及高度可自定义的桌面小部件。

---

## 总体技术路线与原则

1. **环境纯净与隔离**：
   - 依赖的 JDK 及构建工具全部在本地工程目录（或便携目录）下管理，不写入全局系统环境变量，避免污染系统。
   - 所有网络下载均可配置路由至本地代理 `127.0.0.1:7890`。
2. **零鉴权开源数据 + 本地秒开**：
   - 采用 **Jolpica F1 API**（Ergast 官方继承方案）作为在线同步信源。
   - 内置 `2026_calendar.json` 离线赛历数据，保证初次冷启动零网络延迟秒开。
   - 利用 **WorkManager** 实现非侵入式后台静默增量更新。
3. **原生 Material 3 + F1 赛车视觉系统**：
   - 深邃碳纤维黑底（`#15151E`）搭配 F1 经典极速红（`#E10600`）与 10 支车队专属色彩。
4. **澎湃OS & Android 13+ 小部件双重适配**：
   - 基于 **Jetpack Glance 1.1+** 构建响应式桌面小部件。
   - 精准配置 `appwidget-provider` 的尺寸网格（2×2、2×4），无缝适配小米澎湃OS卡片中心与原生 Android 桌面。
5. **系统日历无缝接入**：
   - 调用 Android `CalendarContract` 与 `Intent`，支持将大奖赛周末全阶段（练习赛/排位赛/冲刺赛/正赛）一键同步至系统日历并设置提前提醒。

---

## 分步实施阶段与任务清单

### 阶段一：本地隔离环境与工程基建
- [x] **1.1 配置本地便携 JDK 与 Gradle 构建环境**
  - 准备 JDK 17/21 本地便携版（放入 `.tools/`，不污染系统 PATH）。
  - 配置 `gradle.properties`，指定本地 JDK 路径及代理服务器 `127.0.0.1:7890`。
- [x] **1.2 初始化 Android 项目脚手架**
  - 根工程 `build.gradle.kts`、`settings.gradle.kts`。
  - `app/build.gradle.kts`：引入 Compose BOM、Material 3、Jetpack Glance 1.1+、Retrofit 2、Kotlinx Serialization、WorkManager、DataStore 等。
  - 配置 `AndroidManifest.xml`（网络权限、日历写入权限、桌面小部件 Provider 声明）。

### 阶段二：数据层与离线同步引擎
- [x] **2.1 定义 F1 核心领域数据模型**
  - `Race`（分站信息、国家、赛道、经纬度、正赛时间等）。
  - `Session`（练习赛 FP1/2/3、排位赛 Qualifying、冲刺排位 SQ、冲刺赛 Sprint、正赛 Race）。
  - `RaceResult`（正赛完赛榜、名次、车手、车队、耗时、积分、最速圈）。
  - `DriverStanding` & `ConstructorStanding`（积分榜、胜场、车手号码与车队代表色）。
- [x] **2.2 内置 2026 赛季完整赛历 Asset**
  - 编写 `2026_calendar.json`，完整覆盖 2026 赛季全年 24 站大奖赛（包含墨尔本、上海、铃鹿、巴林、吉达、摩纳哥、银石、斯帕、蒙扎、拉斯维加斯等）的全部细分阶段时间。
- [x] **2.3 实现 Jolpica F1 API 客户端与数据仓库**
  - 构建 Retrofit 接口：`GET /api/f1/2026.json`、`GET /api/f1/2026/{round}/results.json`、`GET /api/f1/2026/driverStandings.json`、`GET /api/f1/2026/constructorStandings.json`。
  - 实现 `F1Repository`：优先读取本地缓存/Asset，后台异步向 API 请求增量更新并更新本地存储。
- [x] **2.4 WorkManager 后台静默同步 Worker**
  - 实现 `F1SyncWorker`，根据比赛周末状态自动调整刷新频率。

### 阶段三：核心业务能力与系统日历集成
- [x] **3.1 倒计时与当前阶段状态计算器 (`CountdownCalculator`)**
  - 计算距离下一场大奖赛、下一节练习赛/排位赛的实时天、时、分、秒。
  - 自动识别比赛当前状态（未开始、进行中 LIVE、已结束 COMPLETED）。
- [x] **3.2 系统日历同步管理器 (`CalendarSyncManager`)**
  - 构建标准日历事件数据结构（标题、地点、起止时间、详情描述）。
  - 支持一键将整站所有 Session 或核心正赛/排位添加到系统日历，设置前置 15/30 分钟闹钟提醒。

### 阶段四：App 界面开发 (Jetpack Compose)
- [x] **4.1 F1 视觉设计系统与主题 (`F1Theme`)**
  - 颜色（F1 Red, Carbon Dark, 10 支车队主题色）。
  - 字体、卡片圆角、毛玻璃与流光赛车质感。
- [x] **4.2 底部导航栏与主界面结构**
  - 3 大主要 Tab：【赛历 Schedule】、【赛果 Results】、【积分榜 Standings】。
- [x] **4.3 页面一：赛历页 (`ScheduleScreen`)**
  - 顶部 Hero 卡片：聚焦下一场大奖赛，赛道轮廓图、国旗、正赛倒计时、一键添加日历按钮。
  - 全年分站列表卡片：支持展开查看各 Session 详情与本地时区时间。
- [x] **4.4 页面二：比赛结果页 (`ResultsScreen`)**
  - 分站选择器 + 领奖台前三名专属高亮卡片（P1 黄金、P2 白银、P3 青铜）。
  - 完整 20 位车手完赛明细表、最速圈紫圈徽章。
- [x] **4.5 页面三：车手与车队积分榜 (`StandingsScreen`)**
  - 车手积分榜：排名、车手号、姓名缩写、所属车队色条、当前总积分、胜场数。
  - 车队积分榜：排名、车队品牌色块、车队名称、总积分、主力车手。
- [x] **4.6 小组件自定义与预览弹窗 (`WidgetCustomizerSheet`)**
  - 支持在 App 内实时预览 2x2 和 2x4 小组件。
  - 自定义主队配色切换（Ferrari / McLaren / Red Bull / Mercedes / Aston Martin 等）。
  - 背景透明度与显示项目调节。

### 阶段五：桌面小组件开发与澎湃OS适配 (Jetpack Glance)
- [x] **5.1 2×2 紧凑倒计时小组件 (`F1CompactWidget`)**
  - 下场大奖赛国旗、站名、赛道、正赛天/时/分倒计时、下一阶段提示。
  - 点击直达对应分站详情。
- [x] **5.2 2×4 完整周末赛程小组件 (`F1ScheduleWidget`)**
  - 包含整场周末的 FP1/FP2/FP3/Qualifying/Sprint/Race 详细时间与状态指示器。
  - 自定义车队主题色高亮条与背景透明度。
- [x] **5.3 澎湃OS / Android 13+ 小部件规范配置文件**
  - `f1_compact_widget_info.xml` 与 `f1_schedule_widget_info.xml`。
  - 配置 `targetCellWidth="2"` / `targetCellHeight="2"` 和 `targetCellWidth="4"` / `targetCellHeight="2"`。
  - 注册 `GlanceAppWidgetReceiver` 并处理系统广播刷新。

### 阶段六：构建验证与交付
- [x] **6.1 代码完整性校验与单元/集成测试**
- [x] **6.2 生成便携开发与运行说明文档 (`README.md`)**

---

## 关键技术配置速查

| 维度 | 配置值 / 说明 |
| :--- | :--- |
| **开发语言** | Kotlin 1.9+ / 2.0+ |
| **Minimum SDK** | API 33 (Android 13.0) |
| **Target / Compile SDK** | API 34 / 35 (适配 Android 14/15 及 HyperOS 1/2) |
| **UI 框架** | Jetpack Compose + Material 3 |
| **小组件框架** | Jetpack Glance 1.1.0 (`androidx.glance:glance-appwidget`) |
| **网络层** | Retrofit 2.9 + Kotlinx Serialization JSON |
| **后台更新** | AndroidX WorkManager |
| **离线存储** | DataStore Preferences + 本地 JSON 缓存 |
| **代理支持** | `http://127.0.0.1:7890` (按需开启) |
