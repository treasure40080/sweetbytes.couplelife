# Data Layer Mastery

本文件定義 PXGo-Android 專案的 Data Layer 架構規範，供 AI 輔助開發時遵循。

---

## 1. 整體架構層次

```
Remote Layer
  ├── ECService          (Retrofit Interface)
  ├── PxGoService        (Retrofit Interface)
  ├── PxPayService       (Retrofit Interface)
  └── LogCollectorService (Retrofit Interface)
         ↓
Data Layer
  ├── ECRepository       / PxGoRepository / PxPayRepository / LogCollectorRepository
  ├── AuthDataStoreManager / SettingsDataStoreManager / SplashDataStore
  └── SplashCacheManager (本機檔案快取)
         ↓
Domain Layer
  ├── TokenManager       (Token 生命週期)
  ├── BiometricManager   (生物辨識狀態)
  ├── VersionUpdateManager
  └── TimeManager
         ↓
UseCase Layer
  ├── LoginUseCase / MemberUseCase
  ├── HomePageUseCase
  ├── PaymentUseCase
  ├── MainUseCase
  ├── SettingsUseCase
  └── DeleteAccountUseCase
```

---

## 2. Response 包裝型別

專案有三種 API 回應 Envelope，對應不同的 Service：

### Result\<T\>（PxGoService）

```kotlin
data class Result<T : Any?>(
    val code: String?,
    val data: T?,
    val message: String,
)
```

- Helper：`getStatusCode()`、`getStatusMessage()`
- 建立錯誤物件：`defaultDataNullResult()`、`parErrorResult()`

### ECResult\<T\>（ECService）

```kotlin
data class ECResult<T>(
    val code: String?,
    val srv: String? = "",
    val data: T?,
    val message: String,
    val responseCode: Int = 0   // HTTP 狀態碼，用於偵測 401
)
```

- `responseCode == 101`（`EC_REFRESH_ERROR`）代表 Token 過期，需 refresh
- Helper：`getResultCode()`、`getResultMessage()`
- 建立錯誤物件：`defaultDataNullECResult()`、`parErrorECResult()`

### PxResult\<T\>（PxPayService）

```kotlin
data class PxResult<T>(
    val status: Status? = Status(),
    val data: T?,
    val success: Boolean
) {
    data class Status(val code: Int? = 0, val message: String? = "")
}
```

- 成功判斷以 `success == true` 為主
- Helper：`getStatusCode()`、`getStatusMessage()`

---

## 3. Retrofit Service 端點總覽

### ECService（電商平台）

| 分類 | Method | Path | 說明 |
|------|--------|------|------|
| 認證 | POST | `/api/auth/login` | 帳號登入 |
| 認證 | POST | `/api/auth/sign-up` | 帳號註冊 |
| 認證 | POST | `/api/auth/authorize` | PxPay 授權 |
| 認證 | POST | `/api/auth/refresh-token` | Token 刷新 |
| 認證 | GET | `/api/auth/pxgo-token` | 取得 PxGo Token |
| 認證 | GET | `/api/auth/dsf-token` | 取得 DSF Token |
| 裝置 | POST | `/api/device/check-mobile` | 手機號碼檢查 |
| 裝置 | POST | `/api/device/check-device` | 裝置認證 |
| 裝置 | POST | `/api/device/touch-token` | 生物辨識 Token |
| 密碼 | POST | `/api/password/check-old` | 舊密碼驗證 |
| 密碼 | POST | `/api/password/forget` | 忘記密碼 |
| 驗證 | POST | `/api/sms/send-code` | 發送 OTP |
| 驗證 | POST | `/api/sms/validate` | 驗證 OTP |
| 支付 | POST | `/api/payment/check-url` | 取得支付網址 |
| 支付 | POST | `/api/payment/token` | 取得支付 Token |
| 使用者 | POST | `/api/user/logout` | 登出 |
| 使用者 | POST | `/api/user/remove-account` | 刪除帳號 |
| 使用者 | GET | `/api/user/member-info` | 會員資訊 |
| 設定 | POST | `/api/settings/push-token` | 更新推播 Token |
| 系統 | GET | `/api/system/properties` | 隱私政策 / 條款 |
| 系統 | GET | `/api/version` | 版本資訊 |
| 系統 | GET | `/api/splash-config` | Splash 設定 |
| 系統 | GET | `/api/timestamp` | Server 時間 |

### PxGoService（購物平台）

| 分類 | Method | Path | 說明 |
|------|--------|------|------|
| 首頁 | POST | `/api/home/current-shop` | 取得目前商店 |
| 首頁 | POST | `/api/home/index-page` | 首頁資料 |
| 首頁 | POST | `/api/home/flow` | 瀑布流商品（分頁） |
| 首頁 | POST | `/api/home/navigation-bars` | 導覽列 |
| 會員 | GET | `/api/member/home-page` | 會員首頁 |
| 會員 | GET | `/api/member/info` | 會員資訊 |
| 會員 | POST | `/api/member/login` | 會員登入 |
| 購物車 | POST | `/api/cart/count` | 購物車數量 |
| 支付 | POST | `/api/payment/check-url` | 支付網址確認 |
| 生物辨識 | POST | `/api/biometric/touch-token` | Touch Token |
| 生物辨識 | POST | `/api/biometric/touch-check` | Touch 驗證 |

### PxPayService（第三方支付）

| 分類 | Method | Path | 說明 |
|------|--------|------|------|
| 認證 | POST | `/api/login` | 加密登入 |
| 認證 | POST | `/api/authorize` | PxPay 授權 |
| 認證 | POST | `/api/logout` | 登出 |
| 驗證 | POST | `/api/mobile/check` | 手機驗證（加密） |
| 驗證 | POST | `/api/sms/send` | 發送 OTP |
| 支付 | GET | `/api/payload` | 取得 Payload |

### LogCollectorService（日誌）

- `POST` → Lambda URL，傳送 `LogPayload`，回傳 `ResponseBody`

---

## 4. Repository 職責邊界

Repository **只做**：
- 呼叫 Service method
- 組裝 Authorization header（`"Bearer $token"`）
- 將個別參數包裝成 Request data class
- 回傳 Service 的原始結果

Repository **不做**：
- 業務邏輯判斷
- 資料轉換或 UI model 對應
- 直接操作 DataStore

### 標準寫法

```kotlin
// ECRepository
suspend fun loginEC(
    phoneNumber: String,
    password: String,
    pushToken: String,
): Result<ECResult<LoginECResponse>> =
    eCService.loginEC(LoginECReq(phoneNumber, password, pushToken))

// 需要 Token 時組裝 Bearer header
suspend fun getMemberInfo(
    token: String,
    actionType: String,
    deviceId: String,
): Result<ECResult<GetMemberInfoResponse>> =
    eCService.getMemberInfo("Bearer $token", actionType, deviceId)
```

### PxGoRepository 特殊模式

部分方法回傳自訂 Sealed Class，集中錯誤語意：

```kotlin
sealed class PaymentUrlResult {
    data class Success(val url: String) : PaymentUrlResult()
    data class Failure(val code: String, val message: String) : PaymentUrlResult()
}

fun payCheckPaymentUrl(...): PaymentUrlResult =
    result.fold(
        onSuccess = { PaymentUrlResult.Success(it.data?.url ?: "") },
        onFailure = { PaymentUrlResult.Failure(it.getError().code, it.getError().message) }
    )
```

---

## 5. DataStore Manager

### 總覽

| Manager | Store 名稱 | 用途 |
|---------|-----------|------|
| `AuthDataStoreManager` | `px_authentication` | Token、使用者身份、生物辨識 |
| `SettingsDataStoreManager` | `px_settings` | App 設定、刪帳狀態 |
| `SplashDataStore` | `ec_splash_cache` | Splash 圖片快取 Metadata |

### AuthDataStoreManager 常用 Key

| Key | Type | 說明 |
|-----|------|------|
| `access_token` | String | EC access token |
| `refresh_token` | String | Token 刷新憑證 |
| `phone_number` | String | 使用者手機號碼 |
| `member_id` | String | 會員 ID |
| `is_login` | Boolean | 登入狀態 |
| `dsf_token` | String | DSF Token |
| `dsf_token_expired` | String | DSF Token 到期時間 |
| `pxgo_token` | String | PxGo Token |
| `pxgo_token_expired` | String | PxGo Token 到期時間 |
| `biometric_datas` | String | JSON 序列化的 `List<BiometricData>` |
| `device_id` | String | 裝置唯一識別碼 |

### 讀寫模式

```kotlin
// 單次讀取（suspend）
val token = authDataStoreManager.getValue(ACCESS_TOKEN_KEY, "")

// 響應式訂閱（StateFlow）
val isLogin: StateFlow<Boolean> = authDataStoreManager.getIsLoginStateFlow()

// 批次寫入（atomic）
authDataStoreManager.saveAuthData(
    accessToken = token,
    refreshToken = refresh,
    phoneNumber = phone,
    memberId = memberId,
    // ...
)
```

- 響應式 key 有 StateFlow 工廠：`getPhoneNumberStateFlow()`、`getMemberIdStateFlow()`、`getIsLoginStateFlow()`
- 批次寫入用 `saveAuthData(...)` 保證原子性，不拆成多次單獨 `setValue`

---

## 6. Data Model / DTO 規範

### 目錄組織

```
data/model/
├── login/
│   ├── request/   # LoginECReq, CheckMobileReq, ...
│   └── response/  # LoginECResponse, AddressResponse, ...
├── home/
│   ├── req/       # HomeFlowGoodsReq, RequestParams, ...
│   └── resp/      # IndexPageVo, HomeFlowGoodVo, ...
├── payment*/
├── memberInfo/
└── ...
```

### 序列化規範

- 使用 **Gson** + `@SerializedName`
- API 欄位名稱（snake_case）vs Kotlin 屬性（camelCase）透過 `@SerializedName` 橋接
- 可選欄位加 `?` + 預設值 `= null`

```kotlin
data class LoginECReq(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("password")     val password: String? = "",
    @SerializedName("touch_token")  val touchToken: String? = "",
    @SerializedName("push_token")   val pushToken: String,
)
```

### 命名慣例

| 用途 | 後綴 | 範例 |
|------|------|------|
| 請求體 | `Req` / `Request` | `LoginECReq`, `CheckMobileRequest` |
| 回應體 | `Response` / `Resp` | `LoginECResponse`, `IndexPageVo` |
| UI 對應物件 | `Vo` | `HomePageDataVo`, `HomeBatchVo` |

---

## 7. Network Interceptor 鏈

| Interceptor | 作用 |
|-------------|------|
| `ECUserAgentInterceptor` | 加入標準 Header（Accept、Cache-Control、App-Name、App-Version、Platform 等） |
| `ECLoggingInterceptor` | 印出所有 Request / Response（含 JSON pretty-print） |
| `PxResponseInterceptor` | Peek response body，透過 `LogApiDataUtil` 寫入本機 log 檔 |

**ECUserAgentInterceptor 加入的 Header：**
```
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json; charset=UTF-8
App-Name: pxec
App-Version: {BuildConfig.VERSION_NAME}
AOS-Version: Android-{Build.VERSION.RELEASE}
Platform: Android
```

---

## 8. TokenManager：Token 生命週期

### Token 快取策略

- PxGo Token / DSF Token：3 分鐘有效期，到期重新取得
- 到期檢查：`System.currentTimeMillis() - storedExpiry > 3_min_ms`

### 401 自動刷新機制

```kotlin
// 所有需要 Token 的 EC API 呼叫統一走此包裝
suspend fun <T> safeApiCallWithRefresh(apiCall: suspend () -> T): T {
    val result = apiCall()
    if (result.responseCode == EC_REFRESH_ERROR) {
        refreshTokenIfNeeded()   // 加 Mutex 防止並發刷新
        return apiCall()         // 刷新後重試一次
    }
    return result
}
```

### Mutex 防並發

- 使用單一 `Mutex` 鎖定，確保同時只有一個 refresh 在執行
- 若 refresh 在 10 秒內已成功，跳過重複刷新（debounce）
- 最多重試 3 次；超過上限執行 `memberUseCase.resetLocalUserDataAsGuest()`

---

## 9. UseCase 職責

UseCase 負責**業務邏輯協調**，是唯一可以：
- 同時呼叫多個 Repository
- 轉換 API Model → UI Model（Vo）
- 做資料驗證（手機號碼格式、密碼規則、身分證號碼等）
- 操作 DataStore（讀取 / 寫入 Token、使用者資料）
- 透過 `TokenManager.safeApiCallWithRefresh()` 呼叫需要 Token 的 API

### 並發資料載入模式

```kotlin
// HomePageUseCase：並行取得多份資料
coroutineScope {
    val indexPage  = async { pxGoRepository.getIndexPage(...) }
    val flowGoods  = async { pxGoRepository.getFlowGoods(...) }
    HomePageDataVo(indexPage.await(), flowGoods.await())
}
```

### 錯誤處理模式

```kotlin
result.fold(
    onSuccess = { data ->
        if (data.data != null) UseCase.Result.Success(data.data)
        else UseCase.Result.Failure(data.getResultCode(), data.getResultMessage())
    },
    onFailure = { throwable ->
        val error = throwable.getError()
        UseCase.Result.Failure(error.code, error.message)
    }
)
```

---

## 10. SplashCacheManager：本機快取

- 下載路徑：`context.filesDir/splash_cache/{md5}.{ext}`
- Metadata 存於 `SplashDataStore`（含 id、url、localPath、startTime、endTime）
- 下載完成後比對 API 清單，刪除已過期的本機檔案
- 使用 `@Named("Download")` OkHttpClient（15s timeout）

---

## 11. 新增 API 的標準流程

1. **定義 DTO**：在 `data/model/{feature}/request/` 與 `response/` 建立 data class，加 `@SerializedName`
2. **擴充 Service**：在對應的 Retrofit interface 加上 suspend function
3. **擴充 Repository**：新增 suspend fun，組裝 Request 物件後呼叫 Service
4. **實作 UseCase 邏輯**：在對應 UseCase 呼叫 Repository，處理 `.fold()`、資料轉換
5. **ViewModel 呼叫**：透過 UseCase 取得結果，emit 到 StateFlow / SharedFlow

---

## 12. 禁止事項

- 禁止在 Repository 做業務邏輯判斷（if/else 根據 code 決定流程）
- 禁止在 ViewModel 直接呼叫 Repository，一律透過 UseCase
- 禁止在 UseCase 之外直接操作 DataStore 寫入（Token 刷新由 TokenManager 負責）
- 禁止多個 Service 共用同一個 OkHttpClient 或 Retrofit 實例
- 禁止在 Repository 建立 CoroutineScope，所有協程由 ViewModel 的 `viewModelScope` 控制
- 新增需要 Token 的 EC API 呼叫，必須透過 `TokenManager.safeApiCallWithRefresh()` 包裝
