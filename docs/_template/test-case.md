# Test Case — {功能名稱}

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-XXXXX |
| 功能名稱 | {功能名稱} |
| SA 文件 | `docs/{feature}/sa.md` |
| 建立時間 | YYYY-MM-DD |
| 版本 | v1.0 |

---

## 功能測試案例

> 依需求列出，每個 TC 對應一個明確的使用情境。

### TC-001：{案例名稱}

**前置條件**：{描述前置狀態}

**步驟**：
1. {步驟 1}
2. {步驟 2}

**預期結果**：
- {預期結果 1}
- {預期結果 2}

---

## 單元測試案例

> 針對 UseCase / ViewModel / Repository 的邏輯驗證，使用 Mock Data。

### UT-001：{測試目標，例如：GetProductUseCase 正常回傳}

**測試對象**：`{ClassName}`

**Mock Data**：
```kotlin
// 範例
val mockResponse = {DataClass}(...)
```

**輸入**：{描述輸入參數}

**預期輸出**：
- {預期結果}

---

### UT-002：{測試目標，例如：API 錯誤時 UseCase 回傳 Failure}

**測試對象**：`{ClassName}`

**Mock Data**：
```kotlin
// 模擬 API 錯誤
val mockError = Exception("Network error")
```

**輸入**：{描述}

**預期輸出**：
- {預期結果}

---

## 架構合規驗證

| 檢查項目 | 預期 | 結果 |
|----------|------|------|
| UI 未直接呼叫 Repository | ✅ | 待驗 |
| ViewModel 未持有 Context | ✅ | 待驗 |
| ViewModel 未持有 NavController | ✅ | 待驗 |
| 未建立自訂 Hilt Component | ✅ | 待驗 |
| 字串使用 stringResource | ✅ | 待驗 |
| 點擊按鈕套用 singleClick() | ✅ | 待驗 |

---

## SA 對照驗證

| SA 規格項目 | 實作符合 |
|-------------|---------|
| {SA 規格 1} | 待驗 |
| {SA 規格 2} | 待驗 |

---

## 測試排除項目（Out of Scope）

- {列出不在此次範圍內的測試項目}
