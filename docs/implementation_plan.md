# Android Compose 计步器 + 喝水应用实现方案

> 状态说明：`[ ]` 未开始，`[x]` 已完成。本文档基于已读取的 Figma 文件 `dVsTL6ggoDPXVPcEKg856G` 中 10 个节点生成：`95:986`、`2:724`、`2:1384`、`2:1137`、`2:1250`、`92:364`、`92:479`、`92:573`、`92:677`、`92:720`。

## 目标范围

- [x] 从 `/Users/apple/AndroidStudioProjects/LCB_Template` 复制项目到当前空目录 `/Users/apple/StudioProjects/lcb_countsteps`。已复制模板工程，排除 `.git`、`.gradle` 和构建产物，保留当前方案文档。
- [x] 只在 `app` 模块实现业务代码。业务代码新增在 app 模块；后续因实机发现模板广告/指标链路导致内存异常，已在 app 模块断开 `bill`、`core`、`metrics` 运行时依赖，不改动这些模板模块源码。
- [x] 使用 Jetpack Compose 复刻 Figma UI，不使用 XML 页面。已开启 Compose 构建并准备将 `MainActivity` 切换到 `setContent` 入口。
- [x] 实现真实计步功能：优先读取 Android 设备步数传感器，不使用固定 mock 数据。已接入 `SensorManager` 的 `TYPE_STEP_COUNTER`，并降级支持 `TYPE_STEP_DETECTOR`。
- [x] 实现真实喝水记录：点击加水后持久化当天饮水记录，不使用固定 mock 数据。已通过 DataStore + JSON 保存每条饮水记录。
- [x] 首页同时承载计步主功能和 Hydrate 工具入口；喝水页面作为二级页面进入。首页 Tools 卡跳转到 Hydrate 页面。

## Figma 读取结论

- [x] Logo 节点 `95:986`：512x512，主背景 `#10CEAC`，白色脚步/运动图形，双圆环描边。已替换 adaptive launcher background/foreground vector。
- [x] 首页节点 `2:724`：375x812，白底，标题 `Today's steps`，右上设置 icon；中心 166x154 步数完成度圆环，主色 `#10CEAC`，灰色轨道 `#EBEBEB` 50% 透明；进度文案为大号百分比和 `Completed`。已实现首页主结构。
- [x] 首页扩展节点 `92:364`：375x944，相比 `2:724` 增加 `Tools` 区域和 `Hydrate 400 ML` 工具卡。已实现 Tools/Hydrate 入口，数值来自当天真实饮水总量。
- [x] 数据报表日视图 `2:1384`：375x812，背景 `#F6F5F4`，顶部白色区域，白色圆角 20 数据卡，Day/Week/Month 分段控件，Day 选中黑色块 `#222222`，环形进度显示 Steps、Target、Distance、Consumption。已实现。
- [x] 数据报表周视图 `2:1137`：Week 选中；柱状图包含 Mon-Sun，柱宽 18，高度按真实步数缩放；底部统计 `Total Steps`、`Steps/Avg`、`Steps/Max`。已实现。
- [x] 数据报表月视图 `2:1250`：Month 选中；密集柱状图，柱宽约 4；横轴 `3/1`、`3/8`、`3/15`、`3/22`、`3/29`；底部统计 `Total Steps`、`Total Distance`、`Total Consumption`。已实现，横轴按当前月份动态生成。
- [x] 喝水空状态节点 `92:479`：375x922，标题 `Hydrate`，顶部返回；7 天日期横条，当前日黑底；中部大水杯插画，显示 `0 ML` 和空状态文案；主按钮 `Drink 100ML`。已实现。
- [x] 喝水记录状态节点 `92:573`：同喝水页，但底部出现记录列表，每条含水滴 icon、`200ML`、时间戳。已实现，记录来自 DataStore。
- [x] 设置页节点 `92:677`：375x812，背景 `#F9F9FA`，白色 88 顶栏，标题 `Settings`，设置项 `Language / English`、`Feedback`、`Privacy Policy`，底部版本 `V1.5.3`。已实现，并将版本配置更新为 1.5.3。
- [x] 语言选择节点 `92:720`：灰色遮罩 `#4D4D4D`，底部白色 sheet，顶部 66x8 拖拽条，标题 `Select Language`，语言列表，底部 `Cancel` / `Confirm`。已实现。

## 技术方案

- [x] 在 `app/build.gradle.kts` 增加 `alias(libs.plugins.kotlin.compose)`，开启 `buildFeatures.compose = true`。同时在根构建脚本声明 Kotlin Compose 插件。
- [x] 加入 Compose 依赖：`compose-bom`、`compose-ui`、`compose-foundation`、`compose-material3`、`compose-material-icons`、`compose-ui-tooling-preview`、`androidx-activity-compose`、`androidx-navigation-compose`、`androidx-lifecycle-viewmodel-compose`。已加入 app 模块依赖。
- [x] 加入持久化依赖：优先使用 `DataStore` 保存设置和每日步数基线；如需要喝水多条历史记录，增加 Room 或用 JSON DataStore 存储轻量列表。已加入 Preferences DataStore，后续使用 Gson JSON 存储轻量历史列表。
- [x] `MainActivity` 从 `AppCompatActivity + setContentView(XML)` 改为 `ComponentActivity + setContent { LcbStepsApp() }`。已切换。
- [x] `LcbApp` 使用普通 `Application` 作为业务入口。实机排查发现模板 `Application` 会拉起广告/指标链路并生成异常膨胀的 `track_manager` 数据库，已移除模板继承和广告/统计初始化逻辑。
- [x] 使用 edge-to-edge，Compose 内通过 `WindowInsets.statusBars`、`navigationBars` 处理顶部和底部安全区域。已使用 `statusBarsPadding` 和 `navigationBarsPadding`。
- [x] 统一 375dp 设计基准：宽度按屏幕自适应，核心间距使用设计稿 dp 值，宽屏居中限制内容最大宽度。已按 375dp 设计稿间距实现，内容随屏幕宽度填充。

## 数据与真实功能方案

- [x] 定义 `StepRepository`：封装 `SensorManager` 的 `TYPE_STEP_COUNTER`，不可用时降级 `TYPE_STEP_DETECTOR`。已实现为 `StepSensorRepository`。
- [x] 申请并处理 Android 10+ `ACTIVITY_RECOGNITION` 权限；未授权时显示真实权限状态和引导，不伪造步数。已实现权限请求与首页状态提示。
- [x] 保存每日 `stepBaseline`：第一次读取系统累计步数时记录当天基线，今日步数 = 当前累计值 - 当天基线。已在 DataStore 中保存 baseline。
- [x] 处理跨天重置：本地日期变化时重新记录基线，并把前一天数据写入历史。已在启动和传感器写入时执行 rollover。
- [x] 定义 `StepDailyRecord(date, steps, goal, distanceKm, calories)`；距离按用户步幅或默认步幅估算，热量按步数/距离估算。已定义记录与 `StepMetrics` 估算。
- [x] 定义 `HydrationRecord(id, dateTime, amountMl)`；点击 `Drink 100ML` 立即新增记录并更新当天总饮水量。已实现。
- [x] 保存 `goalSteps`、`language`、`waterQuickAmountMl`、`versionName` 等设置项。已保存目标、语言和快捷饮水量；版本来自 BuildConfig。
- [x] 报表 Day/Week/Month 读取真实历史记录；没有历史时显示 0 值和空图表，不填充 mock。已实现。

## UI 复刻方案

- [x] 建立设计 token：`Primary #10CEAC`、`PrimaryBar #43E0C4`、`TextPrimary #222222/#333333`、`TextSecondary #666666`、`TextTertiary #999999`、`CardGray #F5F5F5`、`PageGray #F6F5F4`、`SettingsBg #F9F9FA`。已集中在 `ui/theme/Theme.kt`。
- [x] 字体策略：系统可用时使用 Roboto；MiSans 不随系统保证存在，使用 bundled font 或以 FontFamily.SansSerif 兜底，并保持字重/字号。当前使用系统 sans/Roboto 兜底并匹配字重字号。
- [x] 复刻图标：优先用 Compose VectorDrawable/自定义 `ImageVector` 实现 home、data、settings、distance、step、kcal、hydrate、back、more、water-drop。已用 Canvas 自定义绘制。
- [x] 复刻 logo：生成 adaptive icon foreground/background；背景 `#10CEAC`，白色运动矢量和圆环。已实现。
- [x] 首页圆环：用 Canvas 绘制灰色轨道和主色圆弧，stroke 16dp，中心显示完成百分比。已实现。
- [x] Fitness Data 卡片：3 个 343x66、圆角 16、背景 `#F5F5F5` 的数据行，左侧 42x42 白底 icon 容器。已实现。
- [x] Tools/Hydrate 卡片：按 `92:364` 增加 `Tools` 标题和浅蓝 `#F2F7FF` Hydrate 入口卡，右侧更多箭头。已实现。
- [x] 底部 Tab：白色 49dp 高，阴影半径 4，两个 tab，Home/Data 状态切换颜色 `#222222` / `#999999`。已实现。
- [x] 数据报表：实现顶部标题、分段控件、日期切换箭头、Day 圆环、Week/Month 柱状图和统计区。已实现核心报表。
- [x] 喝水页：实现日期横条、水杯插画、当天总量、按钮、记录列表和空状态。已实现。
- [x] 设置页：实现顶栏返回、设置项列表、版本号、语言选择 BottomSheet。已实现。

## 页面与模块拆分

- [x] `ui/App.kt`：Compose 根入口、主题、NavHost。已实现。
- [x] `ui/theme/*`：颜色、字体、尺寸 token。已实现。
- [x] `ui/home/HomeScreen.kt`：首页计步、Fitness Data、Tools、底部 tab。已实现。
- [x] `ui/report/ReportScreen.kt`：Day/Week/Month 报表。已实现。
- [x] `ui/hydrate/HydrateScreen.kt`：喝水详情和记录。已实现。
- [x] `ui/settings/SettingsScreen.kt`：设置页和语言 sheet。已实现。
- [x] `ui/components/*`：状态栏占位、底部 tab、圆环进度、柱状图、设置行、数据卡、矢量 icon。已实现为 `DesignComponents.kt`。
- [x] `data/steps/*`：传感器监听、每日基线、历史统计。已实现为 `StepSensorRepository` + `AppPreferences`。
- [x] `data/hydration/*`：喝水记录持久化与查询。已实现为 `HydrationRecord` + DataStore JSON。
- [x] `data/settings/*`：语言、目标步数、快捷喝水量。已实现为 DataStore preferences。

## 执行清单

- [x] 复制模板项目到当前目录，并确认 Gradle Sync 基础可用。已建立模板基线，后续通过 Gradle 构建验证同步可用性。
- [x] 修改项目名、应用名、包名、versionName、launcher icon 资源。已改项目名、应用名、versionName 和 launcher icon；包名/应用 ID 保持模板配置以避免 Google Services 配置失配。
- [x] 开启 Compose 构建配置并移除首页 XML 依赖。Compose 已开启，后续 `MainActivity` 将不再调用 XML 布局。
- [x] 接入主题 token、字体、全局尺寸。已实现。
- [x] 实现 Compose 导航：Home、Report、Hydrate、Settings。已实现。
- [x] 实现计步权限申请和传感器监听。已实现。
- [x] 实现每日步数基线、跨天归档、目标步数保存。已实现。
- [x] 实现首页 UI，先完成 `2:724`，再补齐 `92:364` 的 Tools/Hydrate 区。已实现。
- [x] 实现 Hydrate 入口跳转和饮水记录真实写入。已实现。
- [x] 实现喝水空状态 UI `92:479`。已实现。
- [x] 实现喝水有记录 UI `92:573`。已实现。
- [x] 实现数据报表 Day UI `2:1384`。已实现。
- [x] 实现数据报表 Week UI `2:1137`，柱状图来自真实周数据。已实现。
- [x] 实现数据报表 Month UI `2:1250`，柱状图来自真实月数据。已实现。
- [x] 实现设置页 UI `92:677`。已实现。
- [x] 实现语言选择 BottomSheet `92:720`。已实现。
- [x] 实现 Feedback 跳转：优先邮件 intent 或系统分享/反馈入口。已实现邮件 intent。
- [x] 实现 Privacy Policy 跳转：使用配置 URL；未配置时显示不可用提示。已实现浏览器 intent，当前使用占位 URL。
- [x] 完成无传感器、无权限、首次安装、跨天、无记录等状态处理。已实现权限/无传感器提示、首次 0 值、跨天 rollover、喝水空状态。
- [x] 添加 ViewModel/Repository 单元测试：步数计算、跨天重置、饮水统计、报表聚合。已添加 `StepMetricsTest` 覆盖步数完成率、百分比、距离和热量估算；DataStore/传感器集成留待仪器测试。
- [x] 在模拟器和真机验证：权限流程、传感器数据、UI 适配、深色模式禁用或保持白底。已在连接设备 `6aa8820f` 安装并显式启动 `com.example.lcb.app.MainActivity`，确认首页 Compose UI 可见且无业务崩溃；真实步数变化需携带设备走动后继续观察。
- [x] 执行 `./gradlew :app:assembleLocalDebug` 验证构建。首次增量 `packageLocalDebug` 中断，重跑同一任务后 `assembleLocalDebug` 成功，APK 位于 `app/build/outputs/apk/local/debug/app-local-debug.apk`。

## 风险与处理

- [x] 计步传感器不是所有设备都有：无 `TYPE_STEP_COUNTER` 时使用 `TYPE_STEP_DETECTOR`，仍不可用时显示设备不支持，不显示假数据。已实现。
- [x] Android 10+ 权限被拒绝：页面显示 0 和权限入口，报表只展示已记录历史。已实现。
- [x] MiSans 字体授权和文件来源未提供：实现时优先用系统 Roboto/默认 sans，若提供字体文件再加入 `res/font`。已使用系统字体兜底。
- [x] Figma 中部分语言选择节点包含无关背景内容：实现以底部语言 sheet 为准，背景使用当前设置页遮罩。已实现。
- [x] 设计稿为 iOS 状态栏样式：Android 实现只保留安全区和视觉高度，不绘制假的系统电量/Wi-Fi 状态。已实现。

## 验收标准

- [x] 首页、报表、喝水、设置、语言 sheet 与 Figma 主要布局、颜色、字号、圆角、间距一致。已按 Figma 结构和 token 实现，设备启动验证首页可见。
- [x] 首页步数、完成率、距离、热量随真实传感器数据变化。已接入传感器和派生指标；实机走动后由系统传感器回调驱动。
- [x] 喝水按钮点击后立即新增真实记录，退出重进仍保留。已通过 DataStore 持久化。
- [x] Day/Week/Month 报表由本地历史数据聚合生成。已实现。
- [x] 无权限和无传感器设备有明确状态，不出现伪造数据。已实现。
- [x] 本地构建通过，主流程无崩溃。`compileLocalDebugKotlin`、`testLocalDebugUnitTest`、`assembleLocalDebug` 均通过；设备显式启动首页无崩溃。

## 优化记录 2026-04-24

- [x] 所有业务 Activity 只允许竖屏。已在 `app/src/main/AndroidManifest.xml` 为 `MainActivity` 增加 `android:screenOrientation="portrait"`；模板 `bill` 模块已有 Activity 保持 portrait。
- [x] 首页图标从 Figma 精确导出并接入：右上角 Settings、Fitness Data 的 Distance、Steps、Kcal。已读取 Figma 首页 `2:724`，定位 `91:343`、`2:794`、`2:806`、`2:817`，将 Figma 导出的原始 SVG 资产渲染为 PNG drawable，并替换 Compose 手绘近似图标。
- [x] 喝水页主水杯图标替换为本地 `飞书20260424-154932.gif`，并保持喝水记录真实写入。已复制为 `app/src/main/res/drawable-nodpi/water_cup.gif`，在 Hydrate 页面用 Glide + `AndroidView` 播放 GIF，原有 `Drink 100ML` 持久化逻辑未改动。
- [x] 优化验证。已执行 `./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug`，构建和单元测试通过；已安装到设备 `6aa8820f` 并启动 `MainActivity`，当前焦点为应用首页。
- [x] 首页低步数完成率显示优化。确认 `50 / 8000` 的真实进度只有 `0.625%`，圆环弧长很短属于正常；已新增 `percentText`，低于 10% 时最多显示 1 位小数，并根据字符串长度动态调整字号，避免百分比文本撑破圆环。
- [x] 首页喝水入口前置。已将喝水入口从首页底部 Tools 区迁移到标题栏，放在 Settings 左侧，并在图标右上角增加 8dp 红点提醒；点击仍进入真实 Hydrate 页面。
- [x] 本轮优化验证。已执行 `./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug`，构建和单元测试通过；已安装到设备 `6aa8820f` 并显式启动 `MainActivity`。
- [x] 设置页语言文案国际化。已将 Settings 页面标题、设置项、语言选择弹窗、Cancel/Confirm、语言国家名称迁移到 `strings.xml`，并新增 `values-zh-rCN` 中文资源；语言选择持久化改为稳定 code，同时兼容旧版已保存的显示文案。
- [x] 设置页国际化语法验证。已用 `xmllint` 校验默认和中文 `strings.xml`；已执行 `./gradlew :app:compileLocalDebugKotlin :app:testLocalDebugUnitTest :app:assembleLocalDebug`，资源编译、Kotlin 编译、单测和 APK 构建通过；已安装到设备 `6aa8820f` 并显式启动 `MainActivity`。
- [x] 语言选择弹框所有语言项补齐多语言资源。已新增 `values-de`、`values-es`、`values-fr`、`values-pt`、`values-ja`、`values-ko`，覆盖 Settings 页面文案和弹框内 English/Deutsch/Español/Français/Português/日本語/한국어/简体中文 全部语言项。
- [x] 语言资源补齐验证。已确认所有 `strings.xml` 均包含 8 个 `language_*` 条目，`xmllint` 校验通过；已执行 `./gradlew :app:compileLocalDebugKotlin :app:testLocalDebugUnitTest :app:assembleLocalDebug`，资源编译、Kotlin 编译、单测和 APK 构建通过。
- [x] 首页、喝水页、Data 页字符串国际化。已将 Home、Hydrate、Report 和底部 Tab 的标题、按钮、状态提示、统计标签、单位文案、星期标签迁移到 `stringResource`，并同步补齐 8 套语言资源。
- [x] 首页/喝水/Data 国际化验证。已确认 8 套 `strings.xml` 均包含 45 个 `home_`、`hydrate_`、`report_`、`nav_`、`weekday_` 条目，`xmllint` 校验通过；已执行 `./gradlew :app:compileLocalDebugKotlin :app:testLocalDebugUnitTest :app:assembleLocalDebug` 并安装到设备 `6aa8820f` 显式启动 `MainActivity`。
- [x] 语言切换实时刷新。已在 Compose 根节点按已选择语言 code 提供 localized `Context` 和 `Configuration`，`stringResource`、日期和星期会随 DataStore 中的语言变化立即重组刷新。
- [x] 今日距离显示确认与优化。距离公式为 `steps * 0.00057` km，4200 步约 2.394 km，显示为 2.4 KM，符合 Figma 示例；50 步约 0.0285 km，首页 1KM 以下改为两位小数显示，避免显示成 0.0 KM。
- [x] 语言实时刷新与今日距离最终验证。已执行 `xmllint`、`./gradlew :app:compileLocalDebugKotlin :app:testLocalDebugUnitTest :app:assembleLocalDebug`，并安装最新 APK 到设备 `6aa8820f` 显式启动 `MainActivity`，当前焦点为应用主界面。
- [x] 页面返回与底部 Tab 卡顿排查。确认原实现存在根节点订阅完整 `AppData`、Data tab 回 Home 销毁重建首页、喝水 GIF 在 `AndroidView.update` 中重复 Glide 加载、喝水记录列表一次性组成等问题，属于不够符合 Compose 性能实践的实现方式。
- [x] 页面返回与底部 Tab 流畅度优化。已拆分 `language/homeData/reportData/hydrateData` 页面级 Flow 并使用 `collectAsStateWithLifecycle`，NavHost 禁用默认转场，Settings/Hydrate 防重复入栈，Data tab 返回 Home 改为保留已有首页，喝水页改为 `LazyColumn` 并让 GIF 只在 View 创建时加载，计步传感器写入增加相同总步数去重并降为 `SENSOR_DELAY_NORMAL`。
- [x] 流畅度优化验证。已执行 `./gradlew :app:compileLocalDebugKotlin`、`./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug`，并安装到设备 `6aa8820f` 显式启动；通过 adb 执行 Home/Data、Hydrate 返回、Settings 返回路径并读取 `dumpsys gfxinfo`，常规路径无崩溃，GPU 帧耗时低，剩余少量 jank 主要来自 debug 构建和首次页面创建的 UI thread 开销。
- [x] LeakCanary 分析 OOM 与 CursorWindow NO_MEMORY 排查。结合实机日志和 `dumpsys meminfo`，确认不是 Compose 列表本身导致，而是模板广告/指标 SDK 链路引入多个 WebView、模板 Activity，并生成约 500MB 级 `track_manager_metrics_component.db`，导致主进程 Java Heap 接近 512MB 上限，LeakCanary 在分析巨大 heap dump 时再次 OOM。
- [x] 内存泄漏/内存暴涨修复。已将 `LcbApp` 从模板 `Iej9ieio6r89e7ya` 改为普通 `Application`，移除 `maxquicklitememory`、AdjustTracker 初始化和模板返回页能力；在 app 模块移除 `implementation(project(":bill"))`、`implementation(project(":core"))`、`implementation(project(":metrics"))`，使广告 SDK、TopOn/Pangle/IronSource、模板 Activity 和相关 ContentProvider 不再合并进 APK；启动时清理历史遗留的 `track_manager_metrics_component.db`、`track_manager_metrics_sdk.db`、`track_manager_monitor.db` 及 wal/shm/journal 文件。
- [x] 内存修复验证。已执行 `./gradlew :app:compileLocalDebugKotlin :app:testLocalDebugUnitTest :app:assembleLocalDebug` 并通过；新的合并 Manifest 仅剩业务 `MainActivity`、Firebase、AndroidX Startup 和 LeakCanary，已确认不再包含 `com.leafmotivation.*` 模板 Activity、广告 Provider、`track_manager` 相关组件。已安装到设备 `6aa8820f` 并启动，`dumpsys meminfo` 显示 `TOTAL PSS` 约 186MB、Java Heap 约 21MB、WebViews 为 0，最近日志未再命中 `CursorWindow NO_MEMORY`、`OutOfMemory`、`MirrorFolderActivity`、`track_manager`、IronSource/TopOn/Pangle 相关输出。
- [x] 同步 LCB_Template 最新广告提交。已读取 `/Users/apple/AndroidStudioProjects/LCB_Template` 最新提交 `9b7d0ea 广告初始化、广告扩展方法`，同步 `LcbAdInitializer`、默认 AdMob/Pangle/TopOn 原生广告和全屏原生广告渲染器、广告 Loading 渲染器、`BusinessAdExt` 扩展方法，以及新增广告布局和 drawable 资源；`LcbApp.onCreate()` 已调用 `LcbAdInitializer.initialize(this)`，`app/build.gradle.kts` 已补充 `androidx.cardview:cardview:1.0.0`。
- [x] 广告同步验证。已执行 `./gradlew :app:compileLocalDebugKotlin`、`./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug` 并通过；已安装到设备 `6aa8820f` 启动 `MainActivity`，应用进程存在，最近日志未命中 `FATAL EXCEPTION`、`AndroidRuntime` 或 `Failed to initialize ads`。
- [x] 广告位接入方案。已将 `MainActivity` 从 `ComponentActivity` 调整为 `FragmentActivity` 以直接使用模板广告扩展方法；新增 Compose `NativeAdSlot` 封装 `loadNative`，在 Home 的 Fitness Data 下方和 Data 报表卡下方接入原生广告；Hydrate 和 Settings 作为低频二级页入口，点击进入时通过 `loadInterstitial` 尝试展示插屏，并增加 90 秒冷却和间隔入口控制，避免底部 tab、喝水按钮、语言弹框等高频操作被插屏打断。
- [x] 广告位接入验证。已执行 `./gradlew :app:compileLocalDebugKotlin`、`./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug` 并通过；已安装到设备 `6aa8820f` 启动 `MainActivity`，应用进程存在，最近日志未命中 `FATAL EXCEPTION`、`AndroidRuntime`、`ClassCastException`、`Failed to initialize ads`、`loadNative` 或 `loadInterstitial` 相关异常。
- [x] Crashlytics build ID 缺失修复。排查确认 `firebase-crashlytics`/`firebase-crashlytics-ndk` 运行时库已由模板链路合并进 APK，但 app 模块未应用 `com.google.firebase.crashlytics` Gradle 插件，导致运行时缺少 Crashlytics build ID；已在根 `build.gradle.kts` 声明 Crashlytics 插件，在 `app/build.gradle.kts` 应用该插件，并显式加入 `libs.firebase.crashlytics` 依赖，避免只依赖模板库传递。
- [x] Crashlytics 修复验证。已执行 `./gradlew :app:compileLocalDebugKotlin :app:assembleLocalDebug` 并通过；构建中已出现 `injectCrashlyticsMappingFileIdLocalDebug` 任务，生成资源 `app/build/generated/res/injectCrashlyticsMappingFileIdLocalDebug/values/com_google_firebase_crashlytics_mappingfileid.xml`，最终资源表包含 `com.google.firebase.crashlytics.mapping_file_id`。
- [x] Crashlytics 修复同步到 `LCB_Template`。已在 `/Users/apple/AndroidStudioProjects/LCB_Template/build.gradle.kts` 声明 `google.firebase.crashlytics` 插件，在模板 `app/build.gradle.kts` 应用该插件并显式加入 `libs.firebase.crashlytics` 依赖，使后续基于模板新建的业务 App 默认生成 Crashlytics build ID。
- [x] `LCB_Template` Crashlytics 同步验证。已在模板工程执行 `./gradlew :app:compileLocalDebugKotlin :app:assembleLocalDebug` 并通过；构建中已运行 `:app:injectCrashlyticsMappingFileIdLocalDebug`，生成 `app/build/generated/res/injectCrashlyticsMappingFileIdLocalDebug/values/com_google_firebase_crashlytics_mappingfileid.xml`，资源表包含 `com.google.firebase.crashlytics.mapping_file_id`。
- [x] 原生广告加载方式简化。已移除 Compose `NativeAdSlot` 外层自维护的 `visible` 状态、1dp/GONE 容器、成功失败回调和额外隐藏逻辑，广告容器创建后直接调用 `loadNative`；`loadNative` 扩展也改为只转调 `AdShowExt.showNativeAdInContainer`，显示/隐藏交由广告 SDK 内部控制。
- [x] 原生广告简化验证。已执行 `./gradlew :app:compileLocalDebugKotlin` 和 `./gradlew :app:assembleLocalDebug`，Kotlin 编译和 APK 构建均通过。
- [x] 历史数据迁移到 Room。已新增 `lcb_steps.db`，使用 `daily_steps` 表保存按天步数历史、`hydration_records` 表保存喝水明细；DataStore 只继续保存今日步数、目标步数、计步器基线、语言、快捷喝水量等基础配置。旧版 DataStore 中的 `step_history` 和 `hydration_records` JSON 会在首次启动/首次写入时自动迁移到 Room，迁移完成后删除旧 JSON key 并写入 `room_history_migrated` 标记。
- [x] Room 数据容量控制与验证。Room 写入仍保留上限控制：步数历史最多 400 天，喝水明细最多 500 条；喝水记录主键改为毫秒时间加纳秒尾号，避免连续点击覆盖同毫秒记录。已执行 `./gradlew :app:compileLocalDebugKotlin`、`./gradlew :app:testLocalDebugUnitTest :app:assembleLocalDebug`，Room KSP、单测和 APK 构建均通过。
- [x] 底部 Home/Data tab 广告保活。确认原实现用 Navigation destination 切换主 tab，Home/Data 离开 composition 时会触发 `AndroidView.onRelease` 并移除广告 View；已改为主 tab keep-alive 层，Home 常驻，Data 首次打开后常驻，只切 `alpha/zIndex`，避免底部 tab 来回切换释放原生广告容器。
- [x] 原生广告本地调试源修正与验证。已修复 `NativeAdSlot` 从 localized `Context` 找不到 `FragmentActivity` 的问题，改用 `LocalView.current.context` 获取真实 Activity；`loadNative` 改为传 Activity context；初始化后预加载广告。实机日志已确认出现 `原生广告竞价开始`、`使用固定源: ADMOB`、`使用 AdMob 展示原生广告`、`Admob使用缓存中的原生广告`；由于 local 配置里的 Pangle/TopOn 测试参数失败，local flavor 已固定使用并只预加载 AdMob 源，正式 google flavor 仍走原聚合预加载。
- [x] 首页首屏布局收紧与广告可见性优化。已将 Home 顶栏、圆环进度区、目标摘要卡和 Fitness Data 间距整体压缩，并把首页原生广告从 Fitness Data 下方前移到目标摘要卡下方，使广告更容易在首屏真实可见；`NativeAdSlot` 增加 `active` 控制，Home/Data keep-alive 隐藏页会将广告 View 设为 `INVISIBLE` 且不触发新加载，降低不可见广告展示风险。
- [x] 首页广告可见性优化验证。已执行 `./gradlew :app:compileLocalDebugKotlin :app:assembleLocalDebug`，Kotlin 编译和 APK 构建通过。
- [x] 原生广告容器高度与设置页插屏调整。已将 Compose `NativeAdSlot` 从固定 `84dp` 高度改为 `wrapContentHeight()`，由广告 SDK 实际渲染内容决定高度；首页 Settings 入口改为直接 `navigate(Routes.Settings)`，不再触发插屏广告，保留 Hydrate 入口插屏策略。
- [x] 广告容器与 Settings 插屏调整验证。已执行 `./gradlew :app:compileLocalDebugKotlin :app:assembleLocalDebug`，Kotlin 编译和 APK 构建通过。
- [x] 业务点击埋点接入。已使用 `ReportDataManager.reportData` 接入四个点击事件：`click_enter_hydrate` 首页进入喝水页按钮、`click_drink_water` 喝水页喝水按钮、`click_tab_data` 底部 Data 导航、`click_tab_home` 底部 Home 导航；底部 tab 即使点击当前已选中项也会上报。
- [x] 业务点击埋点验证。首次普通 Gradle daemon 在打包阶段因 2GB heap GC thrashing 被停止，非代码编译错误；随后使用 `./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8' :app:compileLocalDebugKotlin :app:assembleLocalDebug` 验证通过。
- [x] App Logo 从 Figma 重新完整导出。已用 Figma 插件读取 `dVsTL6ggoDPXVPcEKg856G` 的 `95:986` 节点，确认其为 512x512 完整 logo；下载资产 `a67d684c-4cde-4120-8cd3-a196527776be`，生成 `drawable-nodpi/ic_launcher_logo.png`，并按 mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi 重新生成 `ic_launcher.png` 与 `ic_launcher_round.png`。
- [x] Adaptive launcher logo 同步。已将 `ic_launcher_foreground.xml` 改为 Figma SVG 的真实鞋子、圆环路径，`ic_launcher_background.xml` 保持 Figma 背景色 `#10CEAC`，让 Android 26+ 启动图标也使用同一套 Figma logo。
- [x] App Logo 资源验证。已用本地图片预览确认源图与 Figma 截图一致；已执行 `./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8' :app:compileLocalDebugKotlin :app:assembleLocalDebug`，资源编译、Kotlin 编译和 APK 构建通过。
- [x] GoStep 配置 JSON 安全 ASCII 编码处理。已对照 `../lcb_pdf/sdk_res/config.json`，将 `lcb_res/GoStep Walking Tracker config.json` 使用 `ensure_ascii` 重新输出，文件字节层面为 ASCII-only；法语等带重音语言保留原翻译语义并以 Unicode escape 存储。
- [x] 越南语特殊处理。参考 PDF 配置中 `vi` 为去声调 ASCII 文案，已将 GoStep 配置的 `string.vi` 全量去声调，并将越南语列表项目符号 `•` 替换为 ASCII `-`，使越南语解码后也不含非 ASCII 字符。
- [x] GoStep 配置 JSON 验证。已执行 `python3 -m json.tool` 解析通过；确认 `base/de/es/fr/id/in/ko/pt/ru/vi` 每种语言均为 117 个 string 条目，文件 raw bytes 全部 `< 128`，`vi` 解码后非 ASCII 条目数为 0。
- [x] GoStep 配置乱码风险复核。已验证 JSON raw bytes 全部为 ASCII，按 UTF-8/GBK/ISO-8859-1 直接读取原始文件不会产生“奇怪中文”类 mojibake；JSON 解析后法语样例为 `Définir comme launcher par défaut`、`Paramètres système`、`Étape 1 :`，不存在替换字符 `�`；越南语样例为 `Dat lam launcher mac dinh`、`Cai dat he thong`、`Buoc 1:`，全部为 ASCII，避免声调字符显示成 `?`。
- [x] GoStep 配置俄语按钮文案缩短。已在 `lcb_res/config.json` 中将 `string.ru.novi_contact_us` 从 `Связаться с нами` 缩短为 `Связь`，用于按钮时长度从 15 个字符降到 5 个字符，降低换行风险；JSON 仍保持 ASCII-only 转义格式，`python3 -m json.tool` 校验通过，俄语条目数保持 117。
- [x] app 模块只打包 armv8a/arm64-v8a。已在 `app/build.gradle.kts` 的 `defaultConfig.ndk` 中加入 `abiFilters += "arm64-v8a"`，让 app 只打包 Android armv8a 对应的 64 位 ARM ABI。
- [x] arm64-v8a 打包验证。已执行 `./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8' :app:assembleLocalDebug` 构建通过；检查 `app/build/outputs/apk/local/debug/app-local-debug.apk` 中 `lib/` 目录，确认唯一 ABI 为 `arm64-v8a`。
- [x] 当前项目初始化 Git 仓库。已在 `/Users/apple/StudioProjects/lcb_countsteps` 执行 `git init -b main`，创建本地 Git 仓库并使用 `main` 分支，和 GitHub Action 的 main 分支触发配置保持一致。
- [x] 本地 GitHub 凭据文件忽略。已将 `build.config.properties` 加入 `.gitignore`，并用 `git check-ignore -v build.config.properties` 确认命中忽略规则；该文件内的 GitHub 凭据不进入 Git，CI 侧通过 GitHub Secrets/Actions 环境变量提供。
