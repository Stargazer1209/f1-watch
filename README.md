# F1 Watch (极速赛历) 🏎️

一款专为 **Android 13+** 与 **小米澎湃OS (Xiaomi HyperOS 1.0+)** 打造的现代化、高颜值 F1 赛程与桌面小组件应用。

遵循 F1 官方视觉规范，基于 **纯原生 Kotlin + Jetpack Compose + Jetpack Glance** 开发，提供秒级响应的赛历日程、比赛结果、车手/车队积分榜，以及高度可自定义的桌面小部件与一键系统日历同步功能。

---

## 🌟 核心特性

### 1. 桌面小组件矩阵 (Jetpack Glance 深度适配)
- **2×2 紧凑倒计时卡片 (`F1CompactWidget`)**：
  - 呈现下一站大奖赛国旗、分站编号、赛道名；
  - 实时天/时/分/秒高能正赛倒计时与下一阶段（如排位赛/练习赛）提示；
  - 点击直达应用内对应分站详情。
- **2×4 周末全赛程卡片 (`F1ScheduleWidget`)**：
  - 全览大奖赛周末所有阶段（一练/二练/三练/冲刺排位/冲刺赛/排位赛/正赛）；
  - 自动换算用户手机本地时区；
  - 动态状态指示器（待开始、进行中 🟢 LIVE、已完赛）。
- **澎湃OS (HyperOS) 专属适配**：
  - 采用标准网格与圆角边距，同时适配小米小部件中心（双指捏合小部件库）与原生 Android 桌面。

### 2. 丰富的主题与个性化定制
- **10 大车队专属品牌配色一键切换**：
  - 🔴 法拉利 (Ferrari Scarlet Red)
  - 🟠 迈凯伦 (McLaren Papaya Orange)
  - 🔵 红牛 (Red Bull Racing Navy)
  - 🟢 奔驰 (Mercedes-AMG Petronas Teal)
  - 🌲 阿斯顿马丁 (Aston Martin Racing Green)
  - 🔷 威廉姆斯 (Williams Racing Blue)
  - 🟢 索伯 / 奥迪 (Stake / Audi Neon Green)
  - 🌸 阿尔卑 (Alpine Pink / Blue)
  - 🔵 小红牛 (Racing Bulls Blue)
  - ⚪ 哈斯 (Haas White / Red)
  - 🏁 F1 官方极速红经典主题
- **背景不透明度调节 (0.40 ~ 1.00)**：自由搭配各种风格的桌面壁纸。
- **练习赛展示开关**：可精简小组件显示内容，聚焦排位与正赛。

### 3. 应用内三大核心页面
1. **赛历 (Schedule)**：
   - 顶部 Hero 焦点卡片展示即将到来的下一站大奖赛与倒计时；
   - 2026 全年 24 站大奖赛赛历列表，支持展开查看每场练习赛、排位赛与正赛的细分时间。
2. **比赛结果 (Results)**：
   - 分站横向滑动切换；
   - 🏆 领奖台 (Top 3) 专属高台卡片（P1 金、P2 银、P3 铜）；
   - 20 位车手全场完赛成绩榜、耗时、积分与 FL 最速圈紫圈徽章。
3. **积分榜 (Standings)**：
   - 车手积分榜 (Drivers Championship)：车手号、姓名、所属车队色条、总积分、胜场数；
   - 车队积分榜 (Constructors Championship)：车队名称、国籍、涂装代表色、总积分。

### 4. 赛程一键同步到系统日历
- 支持一键将整场周末的所有分段赛程写入澎湃OS/Android系统自带日历；
- 自动设置赛前 15/30 分钟系统闹钟提醒，不漏过任何一场精彩排位与正赛！

### 5. 零鉴权开源数据源 + 本地秒开
- **在线信源**：基于 **Jolpica F1 API**（Ergast 官方继承方案，免 API Key）；
- **离线保底**：内置 `app/src/main/assets/2026_calendar.json`，初次冷启动 0 延迟秒开；
- **后台同步**：利用 AndroidX **WorkManager** 实现省电、非侵入式的静默定时同步。

---

## 🛠️ 项目结构

```
f1-watch/
├── .tools/                     # 本地免污染工具链 (JDK 17 + Android SDK)
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml # 权限、Activity 与桌面小部件 Receiver
│   │   ├── assets/
│   │   │   └── 2026_calendar.json # 2026 赛季 24 站内置赛历
│   │   ├── java/com/f1watch/
│   │   │   ├── F1WatchApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── api/        # JolpicaF1Api & ApiClient
│   │   │   │   ├── local/      # PreferencesManager & CalendarAssetReader
│   │   │   │   ├── model/      # Race, Session, Driver, Constructor, Standings
│   │   │   │   └── repository/ # F1Repository (缓存与保底同步引擎)
│   │   │   ├── domain/         # CountdownCalculator & CalendarSyncManager
│   │   │   ├── ui/
│   │   │   │   ├── components/ # F1TopBar, HeroNextRaceCard, SessionStatusBadge
│   │   │   │   ├── results/    # ResultsScreen & ResultsViewModel
│   │   │   │   ├── schedule/   # ScheduleScreen & ScheduleViewModel
│   │   │   │   ├── settings/   # WidgetCustomizerSheet (小部件个性化配置)
│   │   │   │   ├── standings/  # StandingsScreen & StandingsViewModel
│   │   │   │   └── theme/      # F1 视觉系统、TeamColors、Typography、Theme
│   │   │   ├── widget/         # Glance 小组件 (2x2 Compact & 2x4 Schedule)
│   │   │   └── worker/         # WorkManager 定时静默更新任务
│   │   └── res/
│   │       ├── drawable/       # 图标与矢量背景
│   │       ├── values/         # strings.xml, themes.xml, colors.xml
│   │       └── xml/            # f1_compact_widget_info.xml, f1_schedule_widget_info.xml
├── gradle/
│   └── libs.versions.toml      # 现代化版本目录
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties           # 本地 JDK 与代理配置
├── local.properties            # 本地 SDK 路径配置
└── PLAN.md                     # 项目实施计划书
```

---

## 🚀 编译与安装说明 (完全隔离、零全局污染)

本项目已配置完全隔离的本地工具链环境，无需在 Windows 全局安装 JDK 或修改全局环境变量。

### 1. 编译 Debug APK
在工程目录下执行以下 PowerShell 命令：

```powershell
$env:JAVA_HOME = "$((Get-Item .).FullName)\.tools\jdk"
$env:ANDROID_HOME = "$((Get-Item .).FullName)\.tools\android-sdk"
.\gradlew.bat assembleDebug
```

编译产物位于：
`app/build/outputs/apk/debug/app-debug.apk`

### 2. 安装至安卓/澎湃OS设备
连接手机（开启 USB 调试）后执行：

```powershell
& "$((Get-Item .).FullName)\.tools\android-sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 桌面小部件添加方法

1. **澎湃OS (HyperOS)**：
   - 桌面双指捏合 -> 点击底栏【小部件】-> 找到 **F1 Watch**；
   - 选择 **2×2 倒计时卡片** 或 **2×4 周末赛程表** 拖拽添加到桌面；
2. **原生 Android (13+)**：
   - 长按桌面空白处 -> 点击【微件 / Widgets】-> 找到 **F1 Watch** 即可添加；
3. **在 App 内自定义主题**：
   - 打开 F1 Watch 应用 -> 点击右上角 ⚙️/🎨 调色图标；
   - 自由选择喜爱的车队专属色与卡片透明度，桌面小部件将实时同步刷新。
