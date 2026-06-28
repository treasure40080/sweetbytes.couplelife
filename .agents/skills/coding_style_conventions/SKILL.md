# Coding Style Conventions

本文件定義 PXGo-Android 專案的 Coding Style 規範，供 AI 輔助開發時遵循。

---

## 1. 專案架構

採用 **Clean Architecture + MVVM**，分為三層：

```
Presentation Layer（UI）
    ├── Compose Screens (@Composable)
    ├── ViewModels (@HiltViewModel)
    └── Theme / Widgets
         ↓
Domain Layer
    ├── Use Cases
    ├── Repository Interfaces
    └── Managers
         ↓
Data Layer
    ├── Remote（Retrofit Services）
    ├── DataStore Managers
    ├── Data Models / DTOs
    └── Repository Implementations
```

- **Single Activity** 架構，主入口為 `MainActivity`，登入入口為 `LoginActivity`
- Compose Navigation 做畫面切換

---

## 2. 目錄結構

```
com/gc/pxgo/
├── compose/application/
│   ├── base/           # BaseActivity、BaseApplication、共用 ViewModel
│   ├── ui/
│   │   ├── screen/     # 各功能的 Compose Screen
│   │   ├── widget/     # 可重用 Compose 元件
│   │   └── theme/      # 主題、顏色定義
│   ├── domain/
│   │   ├── usecase/    # Use Cases
│   │   ├── repository/ # Repository 介面
│   │   └── manager/    # Domain Managers
│   ├── data/
│   │   ├── remote/     # Retrofit Service 介面
│   │   ├── model/      # 資料模型、DTOs
│   │   ├── datastore/  # DataStore 管理
│   │   └── manager/    # Data Managers
│   ├── di/             # Hilt DI Modules
│   ├── navigation/     # 路由定義
│   └── common/
│       ├── components/ # 通用 Compose 元件
│       ├── extensions/ # Kotlin Extension Functions
│       ├── interceptor/# 網路攔截器
│       └── worker/     # Background Workers
```

---

## 3. 命名規範

| 類型 | 規則 | 範例 |
|------|------|------|
| Class / Object | PascalCase | `LoginViewModel`, `ECRepository` |
| Function / Property | camelCase | `checkMobile()`, `showLoading` |
| Constant | SCREAMING_SNAKE_CASE | `DEFAULT_DEVICE_PLATFORM` |
| ViewModel 檔案 | `{Feature}ViewModel.kt` | `LoginViewModel.kt` |
| Repository 介面 | `{Source}Repository.kt` | `ECRepository.kt` |
| Use Case 檔案 | `{Feature}UseCase.kt` | `LoginUseCase.kt` |
| Screen 檔案 | `{Feature}Screen.kt` | `HomeScreen.kt` |
| 事件 Sealed Class | `{Feature}Event` | `LoginEvent` |
| 狀態 Sealed Class | `{Feature}UIState` | `HomeUIState` |

---

## 4. ViewModel 寫法

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginUseCase: LoginUseCase,
    private val authDataStoreManager: AuthDataStoreManager,
) : ViewModel() {

    // 私有可變狀態
    private val _showLoading = MutableStateFlow(false)
    val showLoading = _showLoading.asStateFlow()

    // 事件流使用 SharedFlow
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun checkMobile(phoneNumber: String) {
        viewModelScope.launch {
            _showLoading.value = true
            val result = loginUseCase.checkMobile(phoneNumber)
            _showLoading.value = false
            if (result.data != null) {
                emitEvent(LoginEvent.CheckMobileSuccess(result.data))
            } else {
                emitEvent(LoginEvent.CheckMobileFailed(result.getResultCode(), result.getResultMessage()))
            }
        }
    }

    private fun emitEvent(event: LoginEvent) {
        viewModelScope.launch { _eventFlow.emit(event) }
    }
}
```

---

## 5. Repository 寫法

```kotlin
class ECRepository(private val eCService: ECService) {

    suspend fun checkMobile(phoneNumber: String): Result<ECResult<Boolean>> =
        eCService.checkMobile(CheckMobileReq(phoneNumber))
}
```

- Repository 只負責資料取得，不包含業務邏輯
- 回傳型別統一使用 `Result<T>` 或 `ECResult<T>` 包裝

---

## 6. Sealed Class 寫法（State / Event）

```kotlin
// Event
sealed class LoginEvent {
    data class CheckMobileSuccess(val isRegistered: Boolean) : LoginEvent()
    data class CheckMobileFailed(val errorCode: String, val errorMessage: String) : LoginEvent()
    object NavigateToHome : LoginEvent()
}

// UIState
sealed class HomeUIState {
    object Loading : HomeUIState()
    data class Success(val data: HomePageDataVo) : HomeUIState()
    data class Failure(val errorMessage: String) : HomeUIState()
}
```

- 用 `sealed class` 表達有限且互斥的狀態或事件
- 有資料的用 `data class`，無資料的用 `object`

---

## 7. Compose Screen 寫法

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val showLoading by viewModel.showLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is LoginEvent.CheckMobileSuccess -> { /* 導頁 */ }
                is LoginEvent.CheckMobileFailed -> { /* 顯示錯誤 */ }
            }
        }
    }

    // UI 實作
}
```

- Screen 只負責 UI 呈現，邏輯交給 ViewModel
- 用 `collectAsState()` 訂閱 StateFlow
- 用 `LaunchedEffect` 訂閱一次性事件（SharedFlow）

---

## 8. Dependency Injection（Hilt）

```kotlin
// Activity / Fragment
@AndroidEntryPoint
class LoginActivity : BaseActivity()

// ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(...) : ViewModel()

// Module
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideECRepository(service: ECService): ECRepository = ECRepository(service)
}
```

- 多個同型別實作時用 `@Named` 區分
- 全域單例用 `@Singleton`

---

## 9. Coroutines & Flow 使用原則

- ViewModel 內一律用 `viewModelScope.launch { }` 啟動協程
- 狀態用 `StateFlow`（有初始值、可重複讀取）
- 一次性事件用 `SharedFlow`（不保留狀態）
- 避免在 Repository / UseCase 層直接 launch，回傳 suspend fun 讓 ViewModel 控制

---

## 10. Extension Function 寫法

- 放在 `common/extensions/` 底下，按型別分檔（e.g., `StringExtensions.kt`）
- 命名清楚表達用途：`String.isNotNullOrEmpty()`, `Int.formatRoundingWithK()`
- 不要在 Extension 裡塞業務邏輯

---

## 11. 類別內部排列順序

1. 建構子注入屬性
2. 私有可變狀態（`MutableStateFlow` / `MutableSharedFlow`）
3. 公開暴露狀態（`asStateFlow()` / `asSharedFlow()`）
4. `init` 區塊（如有）
5. 公開函式
6. 私有輔助函式
7. `companion object`（如有）
8. Sealed class / Enum（定義在檔案末尾或同檔）

---

## 12. 注釋規範

- 只在 **非顯而易見的 WHY** 才加注釋（隱藏限制、特殊繞道原因）
- 不要注釋說明 WHAT（程式碼本身就說明了）
- 中英文混用可接受，但同一段注釋保持一致
- TODO 格式：`// TODO 說明待辦原因`

---

## 13. Build & 環境設定

| 項目 | 值 |
|------|-----|
| Kotlin | 2.0.0 |
| Java Source/Target | 21 |
| Min SDK | 26 |
| Target / Compile SDK | 35 |
| Kotlin code style | `official` |

**Product Flavors：** DEV / SIT / UAT / PROD，各自有對應的 API 端點與 BuildConfig 欄位。

---

## 14. 技術債提醒

- `shortvideo/` 模組為舊版 XML UI，**新功能不使用此模式**
- 存在 `HomeV2.kt` 等版本命名，代表逐步遷移中，新開發請直接使用最新版本
- `MainViewModel` 責任過大，新功能應盡量拆分到各自的 ViewModel
