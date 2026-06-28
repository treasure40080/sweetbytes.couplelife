---
name: AgenticWriteTestCase
description: |
  測試案例（test-case.md）撰寫完整規範。
  定義如何從 SA 文件推導出完整的功能測試、架構合規與邊界測試案例。
  適用對象：project-manager（建立）、android-qa（執行）
---

# Agentic Write Test Case — 測試案例規範

> 適用對象：**project-manager**（建立）、**android-qa**（執行）
> 核心原則：每個 test-case 必須可獨立判定 Pass/Fail，不允許模糊描述。

---

## 1. 測試案例觸發時機

| 情況 | 動作 |
|------|------|
| 新功能 SA 完成後 | 立刻建立 `docs/{feature}/test-case.md` |
| SA 有重大變更 | 更新 test-case.md，標記「受 SA v1.x 影響」 |
| 發現 Bug | 新增對應的 regression test case |

---

## 2. 測試案例結構

每個 test-case 必須包含：

```markdown
### TC-{序號}：{測試名稱}

**測試類型**：功能 / 架構合規 / 邊界 / 錯誤處理

**前置條件**：
- {條件 1}（例：已登入、Token 有效、有網路）

**步驟**：
1. {操作步驟 1}
2. {操作步驟 2}

**預期結果**：
- {可量化的預期結果 1}
- {可量化的預期結果 2}

**SA 關聯**：{sa.md 的哪個章節}

**驗收方法**：靜態驗證（讀程式碼） / 行為驗證（執行 App）
```

---

## 3. 必備測試案例清單

以下類型的 test case **每個功能都必須涵蓋**：

### 3.1 核心功能流程

| TC 類型 | 描述 | 驗收方法 |
|---------|------|----------|
| 正常流程 | API 成功回傳，畫面正確顯示 | 靜態驗證 |
| API 失敗 | API 回傳錯誤，進入 Failure 狀態 | 靜態驗證 |
| Loading 狀態 | 進入畫面時顯示 Shimmer 骨架 | 靜態驗證 |
| 空狀態（若有列表） | API 回傳空列表，顯示空狀態提示 | 靜態驗證 |
| 下拉刷新（若有） | 刷新成功 / 失敗各一個 TC | 靜態驗證 |

### 3.2 架構合規

| TC 類型 | 描述 | 驗收方法 |
|---------|------|----------|
| 分層驗證 | UI 不直接呼叫 Repository | 靜態驗證 |
| ViewModel 純淨 | 無 Context / NavController | 靜態驗證 |
| 字串資源化 | 使用者可見文字不 hardcode | 靜態驗證 |
| 點擊防抖 | 按鈕使用 singleClick {} | 靜態驗證 |

### 3.3 邊界與錯誤

| TC 類型 | 描述 |
|---------|------|
| Token 過期 | 401 回應觸發 Token 刷新流程 |
| 網路中斷 | 顯示錯誤畫面 + 重試按鈕 |
| 必填欄位為空（表單） | 顯示 inline 錯誤訊息 |
| 超長文字 | 截斷或換行，不破版 |

---

## 4. 測試案例撰寫範例

### TC-001：正常流程 — 載入商品列表

```markdown
**測試類型**：功能

**前置條件**：
- 使用者已登入，Token 有效
- 後端 API 回傳 200 + 非空列表

**步驟**：
1. 進入商品列表頁（navigateTo ProductListScreen）

**預期結果**：
- ViewModel 的 uiState 從 Loading → Success
- Success 的 data 包含正確的商品 Vo 列表
- Screen 渲染 ProductListContent，顯示商品卡片

**SA 關聯**：sa.md § API 規格 / § 元件設計 UIState

**驗收方法**：靜態驗證
- 確認 ProductViewModel.loadData() 呼叫 ProductUseCase.execute()
- 確認 Success 分支觸發 _uiState.value = ProductUIState.Success(data)
- 確認 ProductScreen 的 when(uiState) 有處理 Success 分支
```

---

### TC-002：API 錯誤處理

```markdown
**測試類型**：錯誤處理

**前置條件**：
- 後端 API 回傳非 0000 錯誤碼（或 HTTP 錯誤）

**步驟**：
1. 進入商品列表頁
2. API 回傳失敗

**預期結果**：
- uiState 從 Loading → Failure(errorMessage)
- Screen 顯示 ErrorScreen 元件
- ErrorScreen 包含「重試」按鈕，點擊觸發 viewModel.onRetry()

**SA 關聯**：sa.md § 元件設計 UIState（Failure）

**驗收方法**：靜態驗證
- 確認 onFailure 分支設定 ProductUIState.Failure
- 確認 Failure 分支有 onRetry 回調
```

---

## 5. 測試案例品質門檻

test-case.md 完成後，確認：

- [ ] 涵蓋 SA 中所有 API 端點（每個 API 至少一個正常流程 + 一個錯誤流程）
- [ ] 涵蓋所有 UIState（Loading / Success / Failure，以及 SwipeRefreshing 若有）
- [ ] 架構合規 TC 完整（分層、ViewModel 純淨、字串資源化）
- [ ] 每個 TC 的「預期結果」是可量化的（不允許「顯示正確」這類模糊描述）
- [ ] 「驗收方法」明確標示靜態驗證或行為驗證

---

## 6. 與 test-report.md 的對應關係

test-case.md 中每個 TC 都要在 test-report.md 有對應結果：

```
test-case.md  TC-001  →  test-report.md  TC-001: ✅ Pass / ❌ Fail
test-case.md  TC-002  →  test-report.md  TC-002: ✅ Pass / ❌ Fail
...
```

android-qa 驗收時，**逐一對照**，不得跳過任何 TC。
