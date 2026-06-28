# Dependency Injection Mastery

本文件定義 PXGo-Android 專案的 Hilt DI 規範，供 AI 輔助開發時遵循。

---

## 1. 技術棧

| 項目 | 版本 |
|------|------|
| Hilt | 2.51.1 |
| hilt-navigation-compose | 1.2.0 |
| Annotation Processor | KAPT（非 KSP） |

---

## 2. 整體依賴圖

```
@HiltAndroidApp ECApplication
        ↓
SingletonComponent（唯一使用的 Component）
  ├── NetworkModule       → 4 條 Service 鏈
  ├── RepositoryModule    → 4 個 Repository
  ├── DataStoreModule     → 3 個 DataStore Manager
  ├── CoroutinesModule    → @DefaultDispatcher
  ├── CommonAndroidModule → AppCoroutineScope
  ├── Managers（@Singleton @Inject）
  └── UseCases（@Singleton @Inject）
        ↓
  @HiltViewModel（23 個 ViewModel）
        ↓
  @AndroidEntryPoint（Activity / Fragment）
```

---

## 3. Hilt Module 一覽

### CommonAndroidModule

| 提供 | Scope | 說明 |
|------|-------|------|
| `AppCoroutineScope` | @Singleton | `SupervisorJob + @DefaultDispatcher` |

### CoroutinesModule

| 提供 | Scope | 說明 |
|------|-------|------|
| `CoroutineDispatcher` (@DefaultDispatcher) | @Singleton | `Dispatchers.Default` |

### NetworkModule

每條 Service 鏈各自獨立，互不共用 Retrofit / OkHttpClient：

| @Named | 說明 | 憑證策略 |
|--------|------|----------|
| `"PxGoService"` | 主 API | CertificatePinner（2 張，2025/01 + 2025/11） |
| `"PxPayService"` | 支付 API | 無額外憑證 |
| `"ECService"` | 電商 API | TrustManagerFactory（4 張 AWS Root CA，依 Flavor） |
| `"LogCollectorService"` | 日誌收集 | 不驗證憑證 |
| `"Download"` | 靜態資源下載 | 無額外憑證，15s timeout |

每條鏈的組成：
```
@Named("XxxService") BaseUrl String
        ↓
@Named("XxxService") OkHttpClient
        ↓
@Named("XxxService") ResultCallCreator
        ↓
@Named("XxxService") Retrofit
        ↓
XxxService (interface)
```

**OkHttp 共用設定：**
- Connection timeout：30s
- Read / Write timeout：5 分 6 秒
- Connection pool：idle 5min，0 max connections
- Protocol：HTTP/1.1 only
- Ping interval：3s

**額外提供：**
- `CertificateFactory`
- `Converter.Factory`（Gson + 自訂 TypeAdapter）
- `Gson` singleton

### RepositoryModule

```kotlin
@Provides @Singleton
fun provideECRepository(
    @Named("ECService") eCService: ECService
): ECRepository = ECRepository(eCService)
```

| Repository | 注入的 Service |
|------------|----------------|
| `PxGoRepository` | `@Named("PxGoService") PxGoService` |
| `PxPayRepository` | `@Named("PxPayService") PxPayService` |
| `ECRepository` | `@Named("ECService") ECService` |
| `LogCollectorRepository` | `@Named("LogCollectorService") LogCollectorService` |

### DataStoreModule

位於 `data/datastore/di/DataStoreModule.kt`

```kotlin
@Provides @Singleton
fun provideAuthDataStoreManager(
    @ApplicationContext context: Context
): AuthDataStoreManager = AuthDataStoreManager(context)
```

| 提供 | 用途 |
|------|------|
| `AuthDataStoreManager` | 登入 token、使用者 session |
| `SettingsDataStoreManager` | App 設定與偏好 |
| `SplashDataStore` | 動態啟動畫面設定 |

---

## 4. Scope 規範

**全專案只使用 `@Singleton`**，沒有 `@ActivityScoped`、`@FragmentScoped` 等自訂 Scope。

- 所有 Module 安裝在 `SingletonComponent`
- 所有 Service、Repository、Manager、UseCase 都是 Singleton
- ViewModel 透過 `@HiltViewModel` 自動由 Hilt 管理生命週期

---

## 5. 自訂 Qualifier

### @DefaultDispatcher

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
```

- 注入 `Dispatchers.Default`
- 用於 `CommonAndroidModule` 建立 `AppCoroutineScope`、以及 `PaymentUseCase`

### @Named 命名規則

| 名稱 | 用途 |
|------|------|
| `"PxGoService"` | PxGo API 整條鏈 |
| `"PxPayService"` | 支付 API 整條鏈 |
| `"ECService"` | 電商 API 整條鏈 |
| `"LogCollectorService"` | 日誌 API 整條鏈 |
| `"Download"` | 下載用 OkHttpClient |

**新增 Service 時，整條鏈的 BaseUrl、OkHttpClient、Retrofit 都必須加上同名的 `@Named`。**

---

## 6. 各層的注入寫法

### Application

```kotlin
@HiltAndroidApp
class ECApplication : BaseApplication(), ImageLoaderFactory {
    @Inject lateinit var mainUseCase: MainUseCase
    @Inject lateinit var appLifecycleObserver: AppLifecycleObserver
    @Inject lateinit var pendingLogManager: PendingLogManager
}
```

### Activity / Fragment

```kotlin
@AndroidEntryPoint
class MainActivity : BaseActivity()

@AndroidEntryPoint
class LoginActivity : BaseActivity()
```

`BaseActivity` 本身不加 `@AndroidEntryPoint`，由子類加。

### ViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homePageUseCase: HomePageUseCase,
    private val pxGoRepository: PxGoRepository,
    private val authDataStoreManager: AuthDataStoreManager,
) : ViewModel()
```

- 一律用建構子注入（constructor injection）
- 在 Screen 中透過 `hiltViewModel()` 取得

### UseCase

```kotlin
@Singleton
class PaymentUseCase @Inject constructor(
    private val eCRepository: ECRepository,
    private val authDataStoreManager: AuthDataStoreManager,
    private val tokenManager: Provider<TokenManager>,  // 見下方說明
    private val memberUseCase: MemberUseCase,
    private val logCollectorManager: LogCollectorManager,
)
```

UseCase 無需在 Module 中手動 `@Provides`，直接用 `@Singleton @Inject constructor` 讓 Hilt 自動處理。

### Manager

```kotlin
@Singleton
class PendingLogManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logCollectorRepository: LogCollectorRepository,
    private val gson: Gson,
)
```

Manager 同 UseCase，直接用 `@Singleton @Inject constructor` 即可。

---

## 7. 特殊模式

### Provider\<T\>：解決循環依賴

`PaymentUseCase` 與 `TokenManager` 有循環依賴，用 `Provider<T>` 延遲取得：

```kotlin
// 注入時不立即解析，呼叫時才取得實例
private val tokenManager: Provider<TokenManager>

fun someAction() {
    tokenManager.get().refreshToken()
}
```

遇到循環依賴時優先考慮此方式，不要打破 Clean Architecture 的層次。

### @ApplicationContext

需要 Context 時一律使用 `@ApplicationContext`，不注入 Activity Context：

```kotlin
class SomeManager @Inject constructor(
    @ApplicationContext private val context: Context,
)
```

### 動態憑證選擇

ECService 的憑證根據 `BuildConfig.FLAVOR` 在 NetworkModule 中於 runtime 決定：

```kotlin
val certificates = when (BuildConfig.FLAVOR) {
    "sit", "uat" -> listOf(sitCert)
    else          -> listOf(prodCert1, prodCert2)
}
```

新增環境時，記得在 NetworkModule 補上對應的憑證邏輯。

---

## 8. 自訂 CallAdapter（ResultCall）

每條 Service 鏈有各自的 `ResultCallCreator`，解析不同的 API 回應格式：

| ResultCall | 成功條件 |
|------------|----------|
| `PxGoResultCall` | `code == SUCCESS` |
| `PxPayResultCall` | `success == true` |
| `ECResultCall` | `code == EC_CODE_SUCCESS` |
| `LogCollectorResultCall` | 沿用 ECResultCall |

錯誤統一包裝為 `ApiError(httpCode, code, message)`，由 CallAdapter 處理，上層不需個別處理 HTTP 例外。

---

## 9. 新增功能的 DI 標準流程

### 新增一個功能模組（Feature）

1. **UseCase**：加 `@Singleton @Inject constructor`，注入所需 Repository / Manager
2. **ViewModel**：加 `@HiltViewModel @Inject constructor`，注入 UseCase
3. **Activity**（如需要）：加 `@AndroidEntryPoint`
4. 不需動任何 Module 檔案

### 新增一個 API Service

1. 在 `NetworkModule` 新增一組 `@Named("XxxService")` 的四件組：BaseUrl、OkHttpClient、Retrofit、Service
2. 實作對應的 `XxxResultCallCreator`
3. 在 `RepositoryModule` 新增 `@Provides @Singleton fun provideXxxRepository`
4. Repository 類別本身用建構子接收 Service 即可

---

## 10. 禁止事項

- 禁止在 ViewModel / UseCase 中直接 `new` 依賴物件，一律透過注入取得
- 禁止注入 `Activity` / `Fragment` Context 到 Singleton 層，應使用 `@ApplicationContext`
- 禁止建立自訂 Component 或 Subcomponent，維持全 `SingletonComponent`
- 禁止多個 Service 共用同一個 OkHttpClient 或 Retrofit 實例（憑證與解析邏輯需各自隔離）
- 不使用 `@IntoSet`、`@IntoMap`、`@Multibinds` 等多重綁定（目前專案無此需求）
