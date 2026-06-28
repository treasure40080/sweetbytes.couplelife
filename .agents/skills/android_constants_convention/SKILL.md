# 常數定義與使用規範 (Keys & Magic Numbers)

當傳遞 Intent extras、定義 Bundle keys，或是設置超時時間、限制數量等魔法數字 (Magic Numbers) 時，**絕對不可使用寫死 (Hardcoded) 的值**。
請在未來的任何程式碼產出中，務必遵守以下規範：

## 1. 建立常數定義
所有的 Keys 都必須以 `const val` 的形式，統一宣告在該模組或相關的常數檔（例如 `SplashConstants.kt` 或 `Constants.kt`）中。

### ❌ 錯誤示範 (Anti-Pattern)
直接寫死字串容易在後續維護中因大小寫或錯字導致 NullPointerException 或取不到值的 Bug：
```kotlin
// 錯誤：直接寫死字串做為 Key
intent.putExtra("SPLASH_URL", url)
val data = intent.getStringExtra("SPLASH_url") // 拼字錯誤難以察覺
```

### ✅ 正確示範 (Best Practice)
```kotlin
// 1. 在 Object 或是 Companion Object 中集中宣告常數
object SplashConstants {
    const val EXTRA_SPLASH_URL = "EXTRA_SPLASH_URL"
    const val EXTRA_SPLASH_TYPE = "EXTRA_SPLASH_TYPE"
}

// 2. 嚴格呼叫該常數
intent.putExtra(SplashConstants.EXTRA_SPLASH_URL, url)
val data = intent.getStringExtra(SplashConstants.EXTRA_SPLASH_URL)
```

## 2. 命名約定
- **變數名稱**：應以 `EXTRA_` 作為前綴，並全大寫且以底線分隔單字 (Screaming Snake Case)。例如 `EXTRA_USER_ID`。
- **賦值內容**：字串的值應強烈建議與變數名稱保持完全一致，以確保在 Log 追蹤或除錯時具有極高的唯一性與易讀性。

## 3. 避免 Magic Numbers (魔法數字)
程式碼中出現的具體數字（如 Timeout 毫秒數、最大重試次數等），也必須收斂至常數區塊，以提升可讀性與後續維護彈性。

### ❌ 錯誤示範
```kotlin
// 錯誤一：沒有人知道 2000L 代表什麼業務意義
val result = withTimeoutOrNull(2000L) { repository.fetchSplashSettings() }

// 錯誤二：判定條件使用魔法數字 (200) 而非專案內定義的成功常數
if (result?.code == 200) { ... }
```

### ✅ 正確示範
```kotlin
object SplashConstants {
    // 定義清楚該數字的用途及單位
    const val API_TIMEOUT_MILLIS = 2000L
}

// 呼叫
val result = withTimeoutOrNull(SplashConstants.API_TIMEOUT_MILLIS) { repository.fetchSplashSettings() }
```

## 4. 避免使用全寫路徑 (Avoid Fully Qualified Names)
在宣告變數型別、函數回傳值或擴充功能時，**嚴禁**在程式碼段落中直接寫出全寫路徑 (Fully Qualified Names)。
請統一在檔案的最頂端使用 `import` 語句將類別匯入，保持程式碼的簡潔與可讀性。

### ❌ 錯誤示範（自訂類別）
```kotlin
// 錯誤：直接在宣告處寫出落落長的全寫路徑
suspend fun fetchSplashSettings(): Result<ECResult<List<com.gc.pxgo.compose.application.data.model.splash.SplashConfigResponse>>>
```

### ✅ 正確示範（自訂類別）
```kotlin
// 在頂部匯入
import com.gc.pxgo.compose.application.data.model.splash.SplashConfigResponse

// 在宣告處只需寫出類別名稱
suspend fun fetchSplashSettings(): Result<ECResult<List<SplashConfigResponse>>>
```

### ❌ 錯誤示範（標準函式庫也不例外）
此規則同樣適用於 Java / Kotlin 標準函式庫，例如 `java.time.*`：
```kotlin
// 錯誤：在 lambda 內直接使用全寫路徑
val startMillis = config.startTime?.let { java.time.ZonedDateTime.parse(it).toInstant().toEpochMilli() } ?: 0L
val endMillis   = config.endTime?.let   { java.time.ZonedDateTime.parse(it).toInstant().toEpochMilli() } ?: Long.MAX_VALUE
```

### ✅ 正確示範（標準函式庫）
```kotlin
// 在頂部匯入
import java.time.ZonedDateTime

// 在使用處只需寫出類別名稱
val startMillis = config.startTime?.let { ZonedDateTime.parse(it).toInstant().toEpochMilli() } ?: 0L
val endMillis   = config.endTime?.let   { ZonedDateTime.parse(it).toInstant().toEpochMilli() } ?: Long.MAX_VALUE
```
