# AI Coding Rules & Lessons Learned (AI 專屬規範與錯題本)

## 🎨 1. Coding Style 規範

### 1.1 Android 開發架構規範 (MVVM + Compose)
1.  技術棧與層次結構
  - 語言: Kotlin
  - UI: Jetpack Compose
  - 架構: MVVM (Model-View-ViewModel)
  - 導覽: Compose Navigation
  - 依賴注入: Hilt / Koin (DI)
  - 網路層: Retrofit
  - 資料流: StateFlow (狀態管理), SharedFlow (單次事件)

2.  核心規則 (Coding Style)
  - ViewModel:
      - 必須使用 StateFlow 暴露 UI 狀態 (UI State)。
      - 使用 SharedFlow 處理單次事件 (如：Toast、導覽跳轉)。
      - 禁止在 ViewModel 中持有 Context 或 View 引用。
  - Compose:
      - 遵循 Stateless 原則，將狀態提升 (State Hoisting)。
      - 所有 Composable 函數命名必須為大寫開頭的動詞/名詞。
  - DI:
      - 所有的 Repository 與 API Service 必須透過 DI 注入。
  - Retrofit:
      - 網路請求必須在 ioDispatcher 中執行，並處理 Exception。

### 1.2 日誌與追蹤 (Logging & Tracking)
- **架構要求**：所有交易相關的追蹤 (如打 API 的 REQ/RES、網頁 JS 橋接)，都必須統一透過 `LogCollectorManager` 與 `PaymentUseCase.createTransactionLogPayload` 來建構 Log 內容。
- **欄位規範**：需準確帶入 `action`、`transaction_id`、`flow_type` 等關鍵欄位；若是網頁載入應區分 `status` (start/finish/error)。

### 1.3架構與狀態管理 (MVVM + Compose)
- **StateFlow / SharedFlow**：在 ViewModel 裡，UI 事件請使用 `MutableSharedFlow<Event>` 來發送；狀態流則使用 `StateFlow` 或 Compose 的 `State`。
- **依賴注入 (DI)**：嚴格依照現有的 Hilt 注入方式 (`@Inject constructor`) 來傳遞依賴。若是給 JavaScript Bridge 使用的物件，請由 ViewModel 暴露 `val` 屬性，再於 Compose 的 WebView Factory 階段向下傳遞，避免在 Bridge 內自己再次 Inject 而造成實體不一致。

### 1.4 日誌常值與類型管理 (Log Constants & Type Safety)
為避免硬編碼字串（Hardcoded Strings）導致的維護困難與拼寫錯誤，所有日誌相關的 action 與 status 必須進行封裝管理。

A. 規範要求
- 禁止魔法字串 (No Magic Strings)：嚴禁在 ViewModel 或 Repository 層級直接使用 "web_page_load" 或 "js_call" 等字串。
- 枚舉化 Action：所有日誌動作必須定義在 LogAction 列舉中。
- 常數化 Status：通用的狀態描述（如 start, success, fail）必須統一使用 WebViewPageLoadStatus 物件常數。

B. 核心結構定義
Kotlin
/**
 * 集中管理日誌 Action 類型
 */
enum class LogAction(val value: String) {
    WEB_PAGE_LOAD("web_page_load"),
    JS_CALL("js_call"),
    WEB_USER_ACTION("web_user_action"),
    BRIDGE_INVOKE("bridge_invoke")
}

/**
 * 集中管理通用狀態值
 */
object WebViewPageLoadStatus {
    const val START = "start"
    const val FINISH = "finish"
    const val ERROR = "error"
}

C. 實作準則與範例
依照 2.1 規範，日誌建構應結合 WebLogEvent (Sealed Class) 與 WebLogFactory：
- 工廠介面規範：WebLogFactory.create 方法應接收 LogAction 型別而非 String。
- 代碼範例：
Kotlin
// 在 ViewModel 中呼叫
val event = WebLogFactory.create(
    url = url,
    action = LogAction.WEB_PAGE_LOAD, // 使用 Enum 確保正確性
    status = WebViewPageLoadStatus.START,         // 使用統一常數
    startTime = pageLoadStartTime
)

// 更新狀態 (符合 2.2 狀態管理規範)
if (event is WebLogEvent.Start) {
    pageLoadStartTime = System.currentTimeMillis()
}

// 發送日誌 (符合 2.1 統一建構規範)
viewModelScope.launch {
    logCollectorManager.info(event.toBaseMap(paymentUseCase))
}

D. 優化效益
- 強型別檢查：透過編譯器確保 action 欄位不脫軌，減少 Runtime 錯誤。
- 重構友善：若需修改日誌欄位名稱，僅需更動 LogAction 一處，全專案自動同步。
- 語意清晰：LogAction.JS_CALL 比起 "js_call" 更具備代碼自解釋性，且易於在 IDE 中追蹤所有引用位置。


---

*(歡迎繼續將各種踩過的坑與風格規範補充於此🙌)*


這個檔案用於記錄本專案的 Coding Style，以及過去 AI 曾犯過的錯誤與修正經驗。
**【重要指引】AI 在進行任何新功能實作或 Bug 修復前，請務必了解或回顧本檔案的規範，以避免重複犯錯。**

---

## 📌 2. 過去的錯誤與教訓 (Lessons Learned)

### 2.1 WebView 網頁載入時間追蹤 (`duration_ms` 精準度)
- **❌ 錯誤做法**：過去在 `onPageFinished` 時才去比對 URL 拿取時間，或是將 `url` 當作 Map 的 key 來記錄 `pageLoadStartTime`。當網頁發生重定向 (Redirect) 或 Hash 改變時，URL 會與 `start` 時不同，導致算出的 `duration_ms` 為 `0`。
- **✅ 正確做法**：直接使用一個全域或 ViewModel 層級的 `pageLoadStartTime` 變數。在 `onPageStarted` 賦予起始時間 `System.currentTimeMillis()`，並統一於 `onPageFinished` 與 `onReceivedError` 計算耗時。

### 2.2 WebView 渲染與 Context 問題 (白畫面 Bug)
- **❌ 錯誤做法**：在 Compose 原生與 WebView 混合的架構中，若 WebView 的建立 (Factory) 使用了 `ApplicationContext`，會導致網頁內的 Dialog 或某些 UI 元素無法正常依附，進而造成嚴重的「白畫面」渲染異常。
- **✅ 正確做法**：必須確保 WebView 是使用 `Activity Context` 來進行初始化與 Parent 視圖的替換 (Re-parenting)。

### 2.3 類別路徑與 Import 引用 (編譯失敗)
- **❌ 錯誤做法**：曾經在 `CustomWebViewViewModel` 裡憑空假設 `ServiceMode` 是包在 `PaymentUseCase` 裡面的 (例如寫成 `PaymentUseCase.ServiceMode`)，導致編譯失敗 (Unresolved reference)。
- **✅ 正確做法**：務必確認外部 enum 或 data class 的確切 package 來源。若無法確定，請先使用工具 (`grep_search` 或 `view_file`) 檢查 import 宣告，不應憑空猜測類別層級。


### 2.4 

---