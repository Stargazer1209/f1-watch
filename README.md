# F1 Watch (极速赛历) 🏎️

一款专为 **Android 13+** 打造的现代化、高颜值 F1 赛程与桌面小组件应用。

遵循 F1 官方视觉规范，基于 **纯原生 Kotlin + Jetpack Compose + Jetpack Glance** 开发，提供秒级响应的赛历日程、比赛结果、车手/车队积分榜，以及高度可自定义的桌面小部件与一键系统日历同步功能。

---

## 🌟 核心特性

### 1. 桌面小组件矩阵 (Jetpack Glance 原生构建)
- **2×2 紧凑倒计时卡片 (`F1CompactWidget`)**：
  - 1:1 正方形黄金比例排版；
  - 呈现当前大奖赛国旗、分站编号、国家与中文大奖赛名称；
  - 实时正赛倒计时与下一阶段（如练习赛/排位赛）状态提示；
  - 点击直达应用内对应分站详情，支持 `singleTask` 极速唤起零重复堆栈。
- **2×4 赛道全赛程大卡片 (`F1ScheduleWidget`)**：
  - **左侧**：深色科技展台，展示当前分站 **F1DB 纯白极简矢量赛道图** 与中文赛道名称；
  - **右侧**：纵向排列全周末所有赛程阶段（一练/二练/三练/冲刺/排位/正赛）的本地时间；
  - 动态状态指示器（待开始 🔴、进行中 🟢 LIVE、已完赛 ⚪）。

### 2. 丰富的主题与个性化定制
- **11 大车队专属品牌配色一键切换**：
  - 🔴 法拉利 (Ferrari)
  - 🟠 迈凯伦 (McLaren)
  - 🔵 红牛 (Red Bull)
  - 🟢 梅赛德斯 (Mercedes)
  - 🌲 阿斯顿·马丁 (Aston Martin)
  - 🔷 威廉姆斯 (Williams)
  - 🟢 索伯 (Sauber)
  - 🌸 阿尔派 (Alpine)
  - 🔵 小牛 RB (Racing Bulls)
  - ⚪ 哈斯 (Haas)
  - 🟡 凯迪拉克 (Cadillac)
  - 🏁 F1 官方红经典主题
- **练习赛展示开关**：可自由切换 2×4 小组件是否展示练习赛，聚焦排位与正赛。

### 3. 应用内三大核心页面
1. **赛历 (Schedule)**：
   - 顶部 Hero 焦点卡片展示即将到来的下一站大奖赛与实时倒计时；
   - 2026 全年 24 站大奖赛赛历列表，展开可查看高质感纯白矢量赛道图与各阶段细分时间；
   - 全面支持一键添加到系统日历并自动创建赛前 15 分钟闹钟提醒。
2. **比赛结果 (Results)**：
   - 分站横向滑动切换；
   - 🏆 领奖台 (Top 3) 专属高台卡片（P1 金、P2 银、P3 铜）；
   - 包含 **TheSportsDB 官方高清透明车手肖像**（覆盖 22 位现役与新秀车手）；
   - 全场完赛成绩榜、耗时、积分与最速圈紫圈徽章。
3. **积分榜 (Standings)**：
   - 车手积分榜：车手照片、中文姓名、车队色条、总积分、胜场数；
   - 车队积分榜：包含 **TheSportsDB 宽幅高清赛车定妆侧视图** 与队徽、中文车队名、积分与胜场。

### 4. 健壮的四层数据缓存与 100MB 离线存储
- **在线信源**：基于 **Jolpica F1 API**（免 API Key 零鉴权）；
- **多媒体信源**：**TheSportsDB (Cloudflare R2 CDN)** 透明车手/赛车 + **F1DB** 纯白矢量 SVG；
- **全量离线保底**：内置 2026 赛季赛历、积分与完赛结果底包，冷启动 0 延迟秒开；
- **多媒体强缓存**：Coil 3.0 配置 100MB 专用磁盘缓存，加载过的赛道与图片永久离线可用。

---

## 🛠️ 项目结构

```
f1-watch/
├── .tools/                     # 本地免污染工具链 (JDK 17 + Android SDK)
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml # 权限、singleTask Activity 与桌面小部件 Receiver
│   │   ├── assets/
│   │   │   ├── 2026_calendar.json              # 2026 赛季 24 站内置赛历
│   │   │   ├── 2026_driver_standings.json      # 22 位车手积分榜基准
│   │   │   ├── 2026_constructor_standings.json # 11 支车队积分榜基准
│   │   │   └── 2026_race_results.json          # 分站完赛结果基准
│   │   ├── java/com/f1watch/
│   │   │   ├── F1WatchApplication.kt           # Coil SvgDecoder 与 100MB 磁盘缓存配置
│   │   │   ├── MainActivity.kt                 # singleTask 主界面
│   │   │   ├── data/
│   │   │   │   ├── api/        # JolpicaF1Api & ApiClient
│   │   │   │   ├── local/      # PreferencesManager & CalendarAssetReader
│   │   │   │   ├── model/      # Race, Session, Driver, Constructor, Standings
│   │   │   │   └── repository/ # F1Repository (四层自愈缓存引擎)
│   │   │   ├── domain/         # CountdownCalculator & CalendarSyncManager
│   │   │   ├── ui/
│   │   │   │   ├── components/ # F1TopBar, HeroNextRaceCard, SessionStatusBadge
│   │   │   │   ├── results/    # ResultsScreen & ResultsViewModel
│   │   │   │   ├── schedule/   # ScheduleScreen & ScheduleViewModel
│   │   │   │   ├── settings/   # SettingsScreen & WidgetCustomizerSheet (小部件个性化)
│   │   │   │   ├── standings/  # StandingsScreen & StandingsViewModel
│   │   │   │   ├── theme/      # F1 视觉系统、TeamColors、Typography、Theme
│   │   │   │   └── util/       # F1MediaHelper (TheSportsDB & F1DB CDN 调度器)
│   │   │   ├── util/           # F1Localization (中英双语模糊匹配本地化引擎)
│   │   │   ├── widget/         # Glance 小组件 (2x2 Compact & 2x4 Schedule With Track Map)
│   │   │   └── worker/         # WorkManager 定时静默更新任务
│   │   └── res/
│   │       ├── drawable/       # 图标与矢量背景
│   │       ├── values/         # strings.xml, themes.xml, colors.xml
│   │       └── xml/            # f1_compact_widget_info.xml, f1_schedule_widget_info.xml
│   └── src/test/               # 完整的本地化与多媒体调度单元测试
├── gradle/
│   └── libs.versions.toml      # 现代化版本目录
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 编译与运行说明 (完全隔离、零全局污染)

本项目支持全自动、完全隔离的本地工具链环境，无需在系统全局安装 JDK、Android SDK 或修改全局环境变量。

### 1. 一键初始化本地隔离开发环境
工程根目录下通过 `uv` 或 `python3` 执行：

```bash
# 自动探测操作系统 (Linux / Windows) 及本地代理，自动下载并配置本地 JDK 17 与 Android SDK 至 .tools/
uv run python setup_env.py
```

### 2. 运行单元测试与编译 Debug APK

**Linux / macOS (Bash)**:
```bash
export JAVA_HOME="$(pwd)/.tools/jdk"
export ANDROID_HOME="$(pwd)/.tools/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew testDebugUnitTest assembleDebug
```

**Windows (PowerShell)**:
```powershell
$env:JAVA_HOME = "$((Get-Item .).FullName)\.tools\jdk"
$env:ANDROID_HOME = "$((Get-Item .).FullName)\.tools\android-sdk"
.\gradlew.bat testDebugUnitTest assembleDebug
```

编译产物位于：
`app/build/outputs/apk/debug/app-debug.apk`

### 3. 安装至 Android / 小米澎湃OS 设备

**Linux**:
```bash
./.tools/android-sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Windows (PowerShell)**:
```powershell
& "$((Get-Item .).FullName)\.tools\android-sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 桌面小部件添加方法

1. **添加小部件**：
   - 长按桌面空白处（或双指捏合）-> 点击【微件 / 小部件 / Widgets】-> 找到 **F1 Watch**；
   - 选择 **2×2 倒计时卡片** 或 **2×4 周末赛程表 (含赛道图)** 拖拽添加到桌面；
2. **在 App 内自定义主题配色**：
   - 打开 F1 Watch 应用 -> 点击右上角 ⚙️ 调色图标（或在底栏进入设置）；
   - 选择喜爱的车队主队配色，桌面小部件将实时同步更新。

---

## 📄 License

Licensed under the [Apache License 2.0](LICENSE). Any redistribution or derivative work must preserve the copyright notice and [NOTICE](NOTICE) attribution to **Xiaokang Zhao (Stargazer1209)**.
