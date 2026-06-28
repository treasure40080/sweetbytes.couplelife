---
name: AgenticWriteSA
description: |
  SA（系統分析）文件撰寫完整規範，適用於 Agentic 開發流程。
  定義 SA 結構、內容品質標準、與 android-developer 實作之間的銜接協議。
  適用對象：project-manager（建立）、android-developer（讀取執行）
---

# Agentic Write SA — 系統分析文件規範

> 適用對象：**project-manager**（建立/維護）、**android-developer**（讀取執行）
> 核心原則：SA 是 android-developer 的直接施工藍圖，**SA 不完整 = 不得開工**。

---

## 1. SA 文件觸發時機

以下情況必須建立或更新 SA：

| 情況 | 動作 |
|------|------|
| 新功能開發 | 建立 `docs/{feature}/sa.md` |
| Bug 修復（影響多個層） | 建立 `docs/{feature}/sa-bugfix.md` |
| 既有 SA 規格有變更 | 在 SA 末尾追加「變更記錄」章節 |
| 小幅調整（單一方法） | 在 pjm.md 的備註欄簡述即可，不需完整 SA |

---

## 2. SA 品質門檻（完成標準）

SA 必須達到以下標準，才能派工 android-developer：

### 必填章節（缺一不可）

- [ ] **API 規格**：endpoint、HTTP method、Service 鏈（EC/PxGo/PxPay/LogCollector）
- [ ] **Request/Response 資料模型**：完整的 field 定義（含型別與 `@SerializedName`）
- [ ] **VO（UI 層資料模型）**：從 Response → VO 的轉換邏輯
- [ ] **ViewModel 設計**：UIState sealed class、Event sealed class、方法清單
- [ ] **畫面結構**：Screen 層次（CommonActionBar + 內容 + Loading）
- [ ] **路由定義**：新增的 NavScreen 名稱與 NavGraph 掛載位置
- [ ] **影響範圍**：新增檔案清單 + 修改檔案清單
- [ ] **驗收標準**：明確的可驗證條件（至少 3 條）

### 選填章節（有需要才填）

- **時序圖**：有多個 API 並發或有複雜狀態機時
- **Hilt Module 設計**：有新增 binding 時
- **錯誤碼處理**：API 有多種 error code 需要個別處理時

---

## 3. SA 標準格式

```markdown
# SA — {功能名稱}（{PXBOX-XXXXX}）

## 基本資訊
| 項目 | 內容 |
| 建立時間 | YYYY-MM-DD |
| 版本 | v1.0 |

## 架構概覽
（用文字描述層次：Screen → ViewModel → UseCase → Repository → Service）

## API 規格
### {API 名稱}
- Endpoint：`POST /api/v1/xxx`
- Service：PxGoService
- Request：{ 完整 JSON }
- Response：{ 完整 JSON + 欄位說明 }
- 錯誤碼：{ code → 說明 表格 }

## 資料模型
### DTO（網路層）
```kotlin
data class XxxResponse(...)
```
### VO（UI 層）
```kotlin
data class XxxVo(...)
```

## 元件設計
### ViewModel
```kotlin
sealed class XxxUIState { ... }
sealed class XxxEvent { ... }
```
### Screen 結構
（文字描述 Composable 樹狀結構）

## 路由定義
```kotlin
object Xxx : RootNavScreen("xxx")
```

## 影響範圍
| 類型 | 檔案路徑 | 說明 |

## 驗收標準
- [ ] ...
```

---

## 4. 資料模型撰寫規範

### DTO 規則

```kotlin
// 正確：每個 field 加 @SerializedName
data class ProductResponse(
    @SerializedName("product_id") val productId: String,
    @SerializedName("price") val price: Double,
    @SerializedName("is_available") val isAvailable: Boolean
)

// 禁止：缺少 @SerializedName
data class ProductResponse(
    val productId: String,   // ❌ 會導致 JSON 解析失敗
)
```

### VO 規則

```kotlin
// VO 專注展示邏輯，不含原始 API 欄位
data class ProductVo(
    val displayPrice: String,     // "NT$ 299"（格式化後）
    val isAvailable: Boolean,
    val stockStatus: String       // "有庫存" / "售罄"
)
```

### 轉換函式（在 Repository 或 UseCase 中定義）

```kotlin
fun ProductResponse.toVo() = ProductVo(
    displayPrice = "NT$ ${price.toInt()}",
    isAvailable = isAvailable,
    stockStatus = if (isAvailable) "有庫存" else "售罄"
)
```

---

## 5. UIState 設計規範

每個 Screen 對應一個獨立的 UIState：

```kotlin
sealed class {Feature}UIState {
    object Loading : {Feature}UIState()           // 首次進入載入
    object Retrying : {Feature}UIState()          // 錯誤後重試
    object SwipeRefreshing : {Feature}UIState()   // 下拉刷新（如有）
    data class Success(val data: {Feature}Vo) : {Feature}UIState()
    data class Failure(val errorMessage: String) : {Feature}UIState()
    data class SwipeRefreshFailure(              // 下拉刷新失敗（保留舊資料）
        val errorMessage: String
    ) : {Feature}UIState()
}
```

### 必備的 Event

```kotlin
sealed class {Feature}Event {
    // 導覽事件（不使用 NavController 直接呼叫）
    data class NavigateTo(val route: String) : {Feature}Event()
    // 錯誤提示事件
    data class ShowError(val message: String) : {Feature}Event()
}
```

---

## 6. SA 更新協議

功能開發中如果發現 SA 與實際需求有差異：

```
1. android-developer 停止實作，回報給 project-manager
2. project-manager 確認差異點，更新 sa.md（在末尾追加「v1.1 變更記錄」）
3. 更新 pjm.md 備註欄：「SA v1.1 更新：{原因}」
4. 重新派工 android-developer 繼續實作
```

**禁止**：android-developer 自行修改 SA，或在 SA 不完整時繼續開工。

---

## 7. SA 與現有規範的整合

本 SA 格式與以下現有 Skill 對齊：

- `sa_document_convention/SKILL.md` — 通用 SA 結構規範
- `data_layer_mastery/SKILL.md` — API / Model / Repository 實作細節
- `coding_style_conventions/SKILL.md` — MVVM 架構與 fold 處理模式
