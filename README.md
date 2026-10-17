# LiquidBrowser

液态玻璃 Android 浏览器，从零开发的独立新项目（Kotlin + Jetpack Compose + WebView + Material 3）。
**不是 Via 修改版，不含任何 Via 代码、资源或品牌元素。**

## 当前进度：阶段 1–2 已完成，阶段 3 部分落地

- Gradle 工程（Kotlin DSL）：AGP 9.4.1 / Kotlin 2.4.20 / Gradle 9.7.1 / compileSdk 37
- Material 3 主题：浅色 / 深色 / 跟随系统（真实可用，当前仅内存生效）
- 首页：搜索框 + 快捷链接，输入后进入浏览器界面
- 浏览器界面：真 WebView（平台 `android.webkit.WebView`），见下文“浏览器”一节
- 底部导航栏：KSU 风格悬浮胶囊底部导航栏 + 胶囊玻璃搜索框，见下文“底部导航栏”一节
- 书签 / 历史：空状态界面，阶段 4 接入数据层
- 设置：主题切换 + 玻璃强度滑杆（已接入 Backdrop 真实折射）+ 关于
- 设计令牌集中在 `ui/theme/GlassTokens.kt`，禁止各页面散落硬编码

## 浏览器

- 真 WebView：平台 `android.webkit.WebView`。
- 输入网址直载；输入关键词走 Bing 搜索（大陆无需 VPN 可用）。
- 支持后退 / 前进 / 刷新、进度条；系统返回键先回退网页历史。

## 底部导航栏

- 改编自 Kyant0 官方 catalog 里的 `LiquidBottomTabs` 示例（Apache 2.0），不是逐字抄 KSU 管理器代码（KSU 是 GPL-3.0）。
- 悬浮胶囊 + 弹簧指示 pill + 横向拖拽切 tab + 按压缩放。
- 胶囊玻璃搜索框 + 玻璃强度滑杆已接入 Backdrop 真实折射。

## 构建

```bash
chmod +x gradlew  # 首次克隆后执行一次：网页端上传的文件不带可执行权限
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

或推到 GitHub 后由 `.github/workflows/android.yml` 自动构建，产物在 Actions 的 Artifacts 里。

## 依赖与开发前检查（已完成）

- AndroidLiquidGlass（Kyant0/Backdrop）：**Apache 2.0**，可使用，需保留署名。
  已接入 Backdrop `2.0.1`，坐标 `io.github.kyant0:backdrop`。
  库本身 `minSdk = 21`，示例 App `minSdk = 23`；本项目 `minSdk = 28`，满足要求。
- API 已按仓库当前 kmp 分支源码核对：`rememberLayerBackdrop` /
  `Modifier.layerBackdrop` / `Modifier.drawBackdrop` / `effects { vibrancy() / blur() / lens() }`。
  `lens` 支持 `chromaticAberration` 开关；`blur` / `lens` 在不支持 RenderEffect / AGSL
  的设备上会静默不生效，属库内保护检查，不是 bug。
- WebView 背景采样已验证，结论见下文“已知限制”：Backdrop 只采样 Compose 图层，
  压在 WebView 上的玻璃栏折射不到网页内容，改用明确标注的视觉降级方案
  （半透明材质 + 渐变 + 高光 + 细边框），禁止用高开销循环截图强行实现。

## 阶段计划

| 阶段 | 内容 | 状态 |
|---|---|---|
| 1 | 项目骨架：Gradle + 主题 + 首页 + 基本导航 | ✅ 已完成 |
| 2 | 最小可用浏览器：地址栏、WebView 加载、后退/前进/刷新/进度 | ✅ 已完成 |
| 3 | 液态玻璃 UI：Backdrop 接入 + 可复用玻璃组件 + WebView 采样验证 | 🚧 部分落地：KSU 风格悬浮胶囊底部导航栏 + 胶囊玻璃搜索框 + 玻璃强度滑杆已接入 Backdrop 真实折射 |
| 4 | 标签、书签与历史 + 关键逻辑测试 | 待开始 |
| 5 | 下载、隐私与设置 | 待开始 |
| 6 | 手机与平板适配（窗口尺寸类别） | 待开始 |
| 7 | 性能与发布：Release 构建、APK、完整说明 | 待开始 |

## 已知限制（阶段 3 部分落地）

- 主题选择、玻璃强度仅内存生效，未持久化（阶段 4+ 接 DataStore）。
- Backdrop 只采样 Compose 图层，WebView 是原生视图不在 Compose 图层里，所以底部玻璃栏压在 WebView 上时折射不到网页内容，只能显示半透明色调 + 高光 + 描边；首页 / 设置页等 Compose 界面折射正常。
- 书签 / 历史无数据层，仅空状态界面。
- 已由 CI 验证可构建。本地构建见上文“构建”一节，注意 gradlew 可执行权限。
