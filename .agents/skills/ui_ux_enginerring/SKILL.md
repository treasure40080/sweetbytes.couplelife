# UI/UX Engineering Conventions

本文件定義 PXGo-Android 專案的 UI/UX 工程規範，供 AI 輔助開發時遵循。

---

## 1. 技術棧總覽

| 項目 | 技術 |
|------|------|
| UI 框架 | Jetpack Compose (Material3) |
| 導航 | Compose Navigation |
| 圖片載入 | Coil 3.0.2（支援 SVG / GIF） |
| 動畫 | Compose Animation + Lottie 6.4.0 |
| 載入骨架 | compose-shimmer 1.0.5 |
| 下拉刷新 | Accompanist SwipeRefresh |
| 字型 | Noto Sans TC（多字重）|

---

## 2. 主題系統（Theme）

### 顏色

- 所有顏色定義在 `ui/theme/Color.kt`
- 語意命名：`PxGoBlack`、`PxGoWhite`、`BottomSelect`、`NoticeBackground`
- 主色系：Primary Blue `#0047BA`，次要藍 `#2B62EC`
- 支援動態顏色解析：`Color.fromHex(hexString)`
- **注意：目前 DarkColorPalette 與 LightColorPalette 內容相同，尚未實作真正的深色模式**

### 字型

- 字體：Noto Sans TC，英文 fallback 為 Noto Sans
- 字重：Regular、Medium、Bold、Black、Light、Thin
- 文字基準行高：`LineHeightStyle.Alignment.Center` + `PlatformTextStyle(includeFontPadding = false)`

### Shape

- `small` / `medium`：`RoundedCornerShape(4.dp)`
- `large`：`RoundedCornerShape(0.dp)`
- 元件圓角以 `RoundedCornerShape` 直接設定為主

### Elevation

- 全專案幾乎不使用 elevation/shadow
- Button 統一使用 `ButtonDefaults.noneElevation()` 移除陰影
- 以邊框與背景色取代層次感

---

## 3. 間距規範

| 值 | 用途 |
|----|------|
| 4dp | 最小間距、圓角、文字欄位邊框 |
| 8dp | 元件內部間距 |
| 10dp | 文字欄位、卡片 Padding |
| 16dp | 區塊垂直間距 |
| 20dp | 畫面水平邊距 |
| 44dp | ActionBar 高度標準 |
| 48dp | BottomSheet Header 高度 |

---

## 4. 可重用元件（Widget）

元件位於：
- `ui/widget/` — 畫面專用的大型元件
- `common/components/` — 通用基礎元件

### 文字輸入框

```kotlin
// 基礎款
PxTextField(
    value = text,
    onValueChange = { text = it },
    placeholder = "請輸入",
    isError = false,
    errorContent = { Text("錯誤訊息") }
)

// 帶標題款
TitleTextField(title = "手機號碼", value = phone, onValueChange = { phone = it })
TitlePasswordTextField(title = "密碼", value = pwd, onValueChange = { pwd = it })
```

- 高度固定 42dp，上下 Padding 10dp
- 標題與欄位間距 4dp
- 錯誤訊息透過 `errorContent` lambda 傳入

### ActionBar

```kotlin
CommonActionBar(
    title = "頁面標題",
    onLeftClick = { navController.popBackStack() },
    backgroundColor = Color.White
)
```

- 高度 44dp，標題置中
- 點擊防抖 300ms（`singleClick()`）

### Alert / Dialog

```kotlin
// 單/雙按鈕 Alert
CommonAlert(
    title = "確認",
    content = "是否繼續？",
    confirmText = "確定",
    cancelText = "取消",
    onConfirm = { /* action */ },
    onDismiss = { /* dismiss */ }
)

// 全螢幕成功提示
SuccessDialog(message = "操作成功")

// 載入中
Loading(isVisible = showLoading)
```

- Dialog 背景白色、圓角 8dp
- Padding：上下 28dp、左右 20dp
- 關鍵流程設 `dismissOnBackPress = false`、`dismissOnClickOutside = false`

### Toast

```kotlin
CommonToast(
    isVisible = showToast,
    message = "提示訊息"
)
```

- 置中顯示，半透明黑底
- 自動 1000ms 後消失，淡入淡出 500ms

### BottomSheet / Picker

```kotlin
PxSingleWheelViewBottomSheet(
    isVisible = showPicker,
    items = enumList,
    onConfirm = { selected -> },
    onDismiss = { showPicker = false }
)
```

- Header 48dp，含「取消」與「確定」按鈕
- 內容可捲動

### 點擊防抖

```kotlin
// 300ms 內只觸發一次
Button(onClick = singleClick { doAction() }) { ... }

// 完全封鎖點擊
Button(onClick = blockedClick()) { ... }
```

---

## 5. 畫面結構模式

### 標準 Screen 結構

```kotlin
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isInitialized) {
        if (!isInitialized) {
            viewModel.loadData()
            isInitialized = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> navController.navigate(...)
            }
        }
    }

    when (state) {
        is HomeUIState.Loading    -> HomeShimmerLoading()
        is HomeUIState.Success    -> HomeContent(data = (state as HomeUIState.Success).data)
        is HomeUIState.Failure    -> ErrorScreen(onRetry = { viewModel.loadData() })
    }
}
```

### 狀態定義

```kotlin
sealed class HomeUIState {
    object Loading : HomeUIState()
    object Retrying : HomeUIState()
    object SwipeRefreshing : HomeUIState()
    data class Success(val data: HomePageDataVo) : HomeUIState()
    data class Failure(val errorMessage: String) : HomeUIState()
    data class SwipeRefreshFailure(val errorMessage: String) : HomeUIState()
}
```

---

## 6. 載入（Loading）模式

### Shimmer 骨架

- 使用 `compose-shimmer`，動畫參數：1000ms tween、100ms delay、Restart 重播
- 建立獨立的 `{Feature}ShimmerLoading.kt`，外型需模擬真實內容的佔位

```kotlin
val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

Box(
    modifier = Modifier
        .shimmer(shimmer)
        .size(width = 200.dp, height = 20.dp)
        .background(Color.LightGray)
)
```

### 全螢幕 Loading

- 使用 `Loading(isVisible = showLoading)` 元件
- 顯示圓形進度條，阻擋使用者操作

---

## 7. 錯誤與空狀態處理

- 錯誤狀態一律透過 `UIState.Failure(errorMessage)` 傳遞
- 錯誤畫面須包含重試按鈕，觸發 `viewModel.loadData()`
- 下拉刷新失敗使用獨立的 `SwipeRefreshFailure` 狀態，避免蓋掉現有資料
- 空狀態需提供明確的文字提示，不留白

---

## 8. 下拉刷新

```kotlin
SwipeRefresh(
    state = rememberSwipeRefreshState(isRefreshing),
    onRefresh = { viewModel.swipeRefresh() }
) {
    LazyColumn { ... }
}
```

- 刷新中顯示 `SwipeRefreshing` 狀態，不清空現有資料
- 刷新失敗顯示 Toast，保留舊資料

---

## 9. 動畫規範

| 動畫 | 用途 | 時長 |
|------|------|------|
| `fadeIn() / fadeOut()` | Dialog、Toast 顯示隱藏 | 500ms |
| `slideInVertically() / slideOutVertically()` | Toast 進出 | 500ms |
| `animateContentSize()` | 展開/收合佈局 | 預設 |
| Lottie | 複雜動畫（成功、載入） | 依設計稿 |
| Shimmer | 骨架載入 | 1000ms loop |
| Splash overlay 淡出 | 啟動畫面 | 300ms tween |

---

## 10. 導航（Navigation）

### 路由定義

```kotlin
sealed class RootNavScreen(val route: String) : NavigationDestination {
    object Main : RootNavScreen("main")
    object Pay : RootNavScreen("pay/{${ARG_TAG}}")
    object Web : RootNavScreen("web/{${ARG_URL}}")
}
```

### 帶參數導航

```kotlin
// Helper 統一做參數替換
navController.navigate(
    RootNavScreen.Pay.format(listOf(ARG_TAG to tag))
)
```

### 常用導航模式

```kotlin
// 重置回到根路由（避免堆疊累積）
navController.navigate(route) {
    popUpTo(route) { inclusive = true }
    launchSingleTop = true
}

// 返回上一頁
navController.popBackStack()
```

### NavGraph 層次

- `RootNavGraph`：全域路由（Splash、Main、Pay、WebView）
- `BottomNavGraphV2`：底部導覽列對應路由
- `LoginNavGraph`：登入流程路由
- 各 NavGraph 分檔管理，不集中在單一檔案

---

## 11. 圖片載入（Coil）

```kotlin
// 網路圖片
AsyncImage(
    model = imageUrl,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.size(80.dp)
)

// 作為 Painter 使用
val painter = rememberAsyncImagePainter(model = imageUrl)
Image(painter = painter, contentDescription = null)
```

- crossfade 設為 `false`（即時顯示，無淡入效果）
- 支援 SVG（`SvgDecoder`）與 GIF（`GifDecoder`）
- 透過 `coil-network-okhttp` 共用 OkHttp Client

---

## 12. 字串資源管理

- 所有使用者看得到的文字都用 `stringResource(R.string.xxx)`
- 純程式用的 key / 固定值用 `translatable="false"`
- 命名規則：`{功能}_{描述}`，例如 `login_phone_hint`、`error_network_timeout`
- 中英文字串分開定義，不混在同一個 key

---

## 13. 注意事項與已知限制

- **深色模式未實作**：`DarkColorPalette` 與 `LightColorPalette` 目前相同，若要支援需另行實作
- **`shortvideo/` 模組**為舊版 XML UI，新功能禁止沿用此模式
- 部分畫面存在 `HomeV2.kt` 等版本命名，開發時以最新版為準
- `MainActivity.instant` 為靜態參考，應避免在新元件中依賴
