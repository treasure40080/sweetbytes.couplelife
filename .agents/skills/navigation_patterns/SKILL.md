# Navigation Patterns

本文件定義 PXGo-Android 專案的 Navigation 架構規範，供 AI 輔助開發時遵循。

---

## 1. 整體架構總覽

```
LauncherActivity（啟動 + Splash + Config 預載）
        ↓ startActivity
MainActivity（RootNavGraph）
  ├── Splash
  ├── Network（無網路錯誤）
  ├── Main（MainScreen + BottomNavGraph）
  │     ├── Home（HomeECWebView）
  │     ├── Web1（小時達）
  │     ├── Web2（分批取）
  │     ├── Find（尋寶）
  │     └── Eat（好好吃）
  ├── CommonWebView
  ├── ActivitysWebView
  ├── Pay（支付驗證）
  ├── Settings
  ├── DeleteAccountFlowHost
  │     ├── DeleteAccountTerms
  │     ├── DeleteAccountIdentification
  │     └── DeleteAccountCustomerService
  └── NotFound
        ↕ ActivityResultLauncher
LoginActivity（LoginNavGraph）
  ├── LoginRouter → LoginMain
  ├── LoginPassword → ForgotPassword → SmsVerification → ResetPassword
  ├── UserIdentification → CustomerService
  ├── Registration → RegisterForm → SetPaymentPassword
  └── TermsAndPolicy
```

---

## 2. Route 定義規範

所有 Route 定義在 `navigation/Screen.kt`，使用 `sealed class` 繼承 `NavigationDestination` 介面。

### 常數定義

```kotlin
const val ARG_URL      = "url"
const val ARG_TAG      = "tag"
const val ARG_FROM     = "from"
const val ARG_PASSWORD = "password"

// ARG_TAG 可用值
const val TAG_HOME     = "home"
const val TAG_MINE     = "mine"
const val TAG_FIND     = "find"
const val TAG_TERMS    = "terms"
const val TAG_POLICY   = "policy"

// ARG_FROM 可用值
const val ARG_FROM_REGISTER        = "register"
const val ARG_FROM_BIND_DEVICE     = "bindDevice"
const val ARG_FROM_FORGOT_PASSWORD = "forgotPassword"
```

### RootNavScreen（全域路由）

| Route Object | Route Pattern | 說明 |
|---|---|---|
| `Splash` | `"splash"` | 啟動畫面 |
| `Network` | `"network"` | 無網路錯誤 |
| `Main` | `"main/{url}"` | 主畫面（含 Bottom Nav） |
| `CommonWebView` | `"commonWebView/{url}"` | 通用 WebView |
| `ActivitysWebView` | `"ActivitysWebView/{url}"` | 活動專用 WebView |
| `Pay` | `"pay/{tag}"` | 支付驗證 |
| `Settings` | `"settings"` | 設定 |
| `NotFound` | `"notfound"` | 404 |

### BottomNavScreenV2（底部 Tab）

| Route Object | Route Pattern | 說明 |
|---|---|---|
| `Home` | `"home"` | 首頁 |
| `Web1` | `"web1/{url}"` | 小時達 WebView |
| `Web2` | `"web2/{url}"` | 分批取 WebView |
| `Find` | `"find"` | 尋寶 |
| `Eat` | `"eat"` | 好好吃 |
| `NotFound` | `"NotFound"` | 找不到對應頁面 |

### LoginNavScreen（登入流程）

| Route Object | Route Pattern | 說明 |
|---|---|---|
| `LoginRouter` | `"loginRouter"` | 登入流程進入點 |
| `LoginMain` | `"loginMain/?url={url}"` | 登入頁（可帶 URL） |
| `LoginPassword` | `"loginPassword"` | 密碼輸入 |
| `ForgotPassword` | `"forgotPassword/{from}"` | 忘記密碼 |
| `SmsVerification` | `"smsVerification/{from}"` | 簡訊驗證 |
| `ResetPassword` | `"resetPassword"` | 重設密碼 |
| `UserIdentification` | `"userIdentification"` | 身份驗證 |
| `Registration` | `"registration"` | 註冊 |
| `RegisterForm` | `"registerForm"` | 註冊表單 |
| `SetPaymentPassword` | `"setPaymentPassword/{from}?password={password}"` | 設定支付密碼 |
| `TermsAndPolicy` | `"terms/{tag}"` | 條款 / 隱私政策 |

### DeleteAccountNavScreen（刪帳流程）

| Route Object | Route Pattern |
|---|---|
| `DeleteAccountFlowHost` | `"deleteAccountFlowHost"` |
| `DeleteAccountTerms` | `"deleteAccountTerms"` |
| `DeleteAccountIdentification` | `"deleteAccountIdentification"` |
| `DeleteAccountCustomerService` | `"deleteAccountCustomerService"` |

### Route 格式化工具

```kotlin
// 將 route pattern 的 placeholder 替換為實際值
fun NavigationDestination.format(pairs: List<Pair<String, Any>>): String
    = pairs.fold(route) { acc, (key, value) -> acc.replace("{$key}", "$value") }
```

---

## 3. NavGraph 結構

### RootNavGraph

- Start destination：`RootNavScreen.Splash`
- 接收 `rootNavController`、`mainViewModel`、`payViewModel`、`loginStateViewModel`
- Splash 使用 `WindowCompat` 處理全螢幕 WindowInsets
- 所有帶 URL 的路由用 `navArgument(ARG_URL) { nullable = true; defaultValue = null }`

### BottomNavGraphV2

- Start destination：`BottomNavItemV2.Home.route`
- 同時接收 `bottomNavController`（子）和 `rootNavController`（父）
- Tab 間導航使用 `rootNavController`，Tab 內部使用 `bottomNavController`

### LoginNavGraph

- 位於 `LoginActivity` 內，scope 為 Activity 層級
- 頂層共用元件：版本更新 Dialog、成功 Dialog、Loading、Toast
- 透過 `LaunchedEffect` 收集 `loginViewModel.eventFlow`，決定登入結果

---

## 4. 底部導覽列

```kotlin
// BottomNavItemV2 定義
enum class BottomNavItemV2(val route: String, val icon: Int, val label: String) {
    Home ("home", R.drawable.ic_home,     "首頁"),
    Web1 ("web1/{url}", R.drawable.ic_category, "小時達"),
    Web2 ("web2/{url}", R.drawable.ic_cart,     "分批取"),
    Find ("find", R.drawable.ic_find,     "尋寶"),
    Eat  ("eat",  R.drawable.ic_eat,      "好好吃"),
}
```

**選中狀態偵測：**

```kotlin
val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route
val isSelected = currentRoute == item.route
```

**Tab 切換選項（Home 範例）：**

```kotlin
rootNavController.navigateToHomeForBottom {
    popUpTo(BottomNavScreen.Home.route) {
        saveState = false
        inclusive = true
    }
    launchSingleTop = true
    restoreState = false
}
```

- `saveState = false`：Tab 切換時不保留舊 Tab 狀態
- `launchSingleTop = true`：避免同一 Tab 堆疊重複實例

---

## 5. 導航 Extension Functions

位於 `common/components/extensions/NavigationExt.kt`

```kotlin
// 基礎導航（防重複）
fun NavController.to(route: String) {
    navigate(route) { launchSingleTop = true }
}

// 帶 URL 導航（自動 URLEncode）
fun NavController.navigateWithUrl(
    dest: NavigationDestination,
    url: String?,
    builder: (NavOptionsBuilder.() -> Unit)? = null
)

// 帶 Tag 導航
fun NavController.navigateWithTag(
    dest: NavigationDestination,
    tag: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
)

// 泛用帶參數導航
fun NavController.navigateWithArgs(
    dest: NavigationDestination,
    vararg args: Pair<String, Any>,
    builder: (NavOptionsBuilder.() -> Unit)? = null
)

// 從 BackStackEntry 取得參數
fun NavBackStackEntry.getParameter(key: String, defaultValue: String? = null): String?

// 支援跨 Activity 的返回（pop 失敗則 finish Activity）
fun NavController.nestedPopBackStack(
    parent: Activity,
    resultBlock: (Activity.() -> Unit)? = null
)
```

**URL 必須 URLEncode：**

```kotlin
// ✅ 正確
navigateWithUrl(RootNavScreen.CommonWebView, url)
// 內部會做 URLEncoder.encode(url, "UTF-8")

// ❌ 錯誤：直接帶入含特殊字元的 URL 會導致路由解析失敗
navigate("commonWebView/$url")
```

---

## 6. Back Stack 操作模式

### 重置回到指定路由

```kotlin
// 清除堆疊並重新導航（例如登出後回首頁）
navController.navigate(RootNavScreen.Main.route) {
    popUpTo(RootNavScreen.Splash.route) { inclusive = true }
    launchSingleTop = true
}
```

### 保留/恢復 Tab 狀態

```kotlin
navController.navigate(route) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

### 安全返回首頁（優先 popBackStack）

```kotlin
fun NavController.navigateToHomeForRoot(force: Boolean = false) {
    if (force) {
        navigateWithTag(RootNavScreen.Main, TAG_HOME)
    } else {
        // 若 back stack 中已有 Main，直接 pop 回去
        val targetEntry = backQueue.findLast {
            it.destination.route == RootNavScreen.Main.route
        }
        if (targetEntry != null) {
            previousBackStackEntry?.savedStateHandle?.set(ARG_TAG, TAG_HOME)
            popBackStack(RootNavScreen.Main.route, inclusive = false, saveState = true)
        } else {
            navigateWithTag(RootNavScreen.Main, TAG_HOME)
        }
    }
}
```

---

## 7. 跨畫面傳遞狀態（SavedStateHandle）

不需要路由參數，但需要跨畫面傳遞臨時狀態時使用：

```kotlin
// 發送端（設定值）
rootNavController.currentBackStackEntry
    ?.savedStateHandle?.set(ARG_TAG, TAG_HOME)

// 接收端（讀取值）
val tag = navController
    .getBackStackEntry(RootNavScreen.Main.route)
    .savedStateHandle.get<String>(ARG_TAG)

// 響應式讀取
val networkRestored by navController
    .getBackStackEntry(route)
    .savedStateHandle
    .getStateFlow<Boolean?>("network_restored", null)
    .collectAsState()
```

---

## 8. ViewModel → 導航事件模式

ViewModel **不持有** NavController，透過 SharedFlow / StateFlow 發出事件，由 Composable 收集後執行導航。

### 標準模式

```kotlin
// ViewModel 定義事件
sealed class HomeEvent {
    data class NavigateToWebView(val url: String) : HomeEvent()
    data class NavigateToPay(val tag: String) : HomeEvent()
    object NavigateToProfile : HomeEvent()
}

private val _eventFlow = MutableSharedFlow<HomeEvent>()
val eventFlow = _eventFlow.asSharedFlow()

// Screen 收集並導航
LaunchedEffect(Unit) {
    viewModel.eventFlow.collectLatest { event ->
        when (event) {
            is HomeEvent.NavigateToWebView ->
                rootNavController.navigateWithUrl(RootNavScreen.CommonWebView, event.url)
            is HomeEvent.NavigateToPay ->
                rootNavController.navigateWithTag(RootNavScreen.Pay, event.tag)
            is HomeEvent.NavigateToProfile ->
                rootNavController.navigateToProfile()
        }
    }
}
```

### 需要 Late Subscriber 的事件（加 replay buffer）

```kotlin
// 例如強制重新載入首頁，訂閱者可能比事件晚到
private val _forceReloadHomeEventFlow = MutableSharedFlow<Boolean>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
```

---

## 9. Activity 間導航

### 整體流程

```
LauncherActivity
  → 預載 splash config（3s timeout）
  → startActivity(MainActivity) + FLAG_ACTIVITY_CLEAR_TOP

MainActivity
  → 需要登入時：loginResultLauncher.launch(LoginActivity intent)
  → LoginActivity 完成後：onActivityResult 回呼 mainViewModel.loginProcessDone()

LoginActivity
  → 完成時：setResult(RESULT_OK, ...) → finish()
```

### 啟動登入流程

```kotlin
// MainActivity 啟動 LoginActivity
loginResultLauncher.launch(
    createLaunchLoginProcessIntent(
        context = this,
        from = ARG_FROM_HOME,
        to = LoginNavScreen.LoginMain.route,
        isWeb = false,
        password = null,
        pxpayLoginDeeplink = ""
    )
)

// LoginActivity 返回結果
fun Activity.finishLogin(
    loginResult: LoginProcessResultState,
    vararg extraBundlePairs: Pair<String, Any?>
) {
    setResult(RESULT_OK, Intent().apply {
        putExtras(bundleOf(
            Pair(KEY_LOGIN_RESULT, loginResult.ordinal),
            *extraBundlePairs
        ))
    })
    finish()
}
```

---

## 10. Deep Link 處理

### Intent Filter 設定（AndroidManifest.xml）

```xml
<!-- HTTPS Domain Deep Link -->
<intent-filter android:autoVerify="true">
    <data android:scheme="https" android:host="${ecHost}" />
</intent-filter>

<!-- PxPay SSO Callback -->
<intent-filter>
    <data android:scheme="${scheme}" android:host="px-auth"
          android:pathPrefix="/authorize" />
</intent-filter>
```

### 處理流程

```
外部 URL / px-auth:// URI
        ↓
LauncherActivity.getIntentData()
        ↓ Intent.data 傳給 MainActivity
MainActivity.getIntentData()
        ↓ 存入 ECApplication.tempIntentUri
MainViewModel.processIntentDeepLink()
        ↓
  ┌─ PxPay SSO → ssoEventFlow → launchLoginProcess()
  └─ 一般 URL → deeplinkUrlData StateFlow
        ↓
  ECApp 觀察 deeplinkUrlData
  ├─ 內部域名 → setECUrlDeeplink()（重載 MainScreen）
  └─ 外部 URL → setOtherUrlDeeplink()（導航至 CommonWebView）
```

---

## 11. 特殊導航情境

### 無網路攔截

```kotlin
// ECApp 全域監聽網路狀態
context.observeConnectivityAsFlow()
    .debounce(500)
    .collect { state ->
        if (state == ConnectionState.Unavailable) {
            rootNavController.navigate(RootNavScreen.Network.route) {
                launchSingleTop = true
            }
        }
    }
```

### WebView 內部返回鍵處理

```kotlin
BackHandler {
    when {
        webviewViewModel.isWebViewFullscreen.value -> {
            webviewViewModel.webView?.reload()
            webviewViewModel.isWebViewFullscreen.value = false
        }
        webviewViewModel.webView?.canGoBack() == true ->
            webviewViewModel.webView?.goBack()
        else ->
            rootNavController.popBackStack()
    }
}
```

### Splash → Main 同步等待

```kotlin
// Splash 畫面等待以下全部完成才導航
LaunchedEffect(lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        // 1. 資料遷移完成
        // 2. 版本檢查完成
        // 3. 最短 Splash 顯示時間到
        // → 觸發 shouldNavigateToMain
    }
}

// 導航後才處理 deep link（避免目標畫面尚未 ready）
coroutineScope.launch {
    viewModel.shouldNavigateToMain.first { it }
    viewModel.processIntentDeepLink(isFirstTime = true)
}
```

---

## 12. 新增頁面的標準流程

1. 在 `Screen.kt` 對應的 `sealed class` 新增 Route Object
2. 若有參數，在 Route pattern 中加 `/{ARG_XXX}`，並在 `ARG_*` 常數區補充常數
3. 在對應的 NavGraph 的 `composable(route)` 區塊新增目標畫面
4. 用 `navArgument()` 宣告參數型別與 nullable 設定
5. 在呼叫端使用 `navigateWithUrl()` / `navigateWithArgs()` 導航，**勿直接字串拼接**

---

## 13. 禁止事項

- 禁止 ViewModel 持有或直接呼叫 NavController，一律透過 EventFlow
- 禁止直接字串拼接帶 URL 的路由，必須使用 `navigateWithUrl()`（內含 URLEncode）
- 禁止在 NavGraph 之外定義 Route 字串，統一放在 `Screen.kt`
- 禁止在多個地方重複定義相同的 Route pattern
- 跨 Activity 傳遞登入結果禁止用靜態變數，一律透過 `ActivityResultLauncher`
