---
name: AgenticAuditImplementation
description: |
  android-qa Agent 的靜態驗證完整規範。
  定義如何對照 SA 文件審計程式碼、填寫 test-report.md、判定驗收通過或失敗。
  適用對象：android-qa
---

# Agentic Audit Implementation — 靜態驗證規範

> 適用對象：**android-qa Agent**
> 核心原則：不執行程式，純靜態讀取 + 對照 SA 規格，以可重現的清單判定通過/失敗。

---

## 1. 驗收啟動流程

收到 project-manager 派工後，**按順序執行**：

```
Step 1. 讀取 docs/{feature}/test-case.md     → 取得測試案例清單
Step 2. 讀取 docs/{feature}/sa.md            → 取得規格基準
Step 3. 讀取 docs/{feature}/pjm.md           → 確認本次驗收的 Task 範圍
Step 4. 列出驗收計畫（哪些 Task 要驗哪些檔案）
Step 5. 逐項靜態驗證
Step 6. 填寫 docs/{feature}/test-report.md
Step 7. 回報 project-manager：Approved / Rejected + 問題清單
```

---

## 2. 靜態驗證清單

### 2.1 架構合規驗證（Architecture Compliance）

逐一讀取 app/src/ 程式碼，確認：

| 檢查項目 | 說明 | 嚴重度 |
|----------|------|--------|
| UI 不直接呼叫 Repository | Screen/Activity 只能呼叫 ViewModel | 🔴 Critical |
| ViewModel 不持有 Context | 傳入 Context 需透過 Application | 🔴 Critical |
| ViewModel 不持有 NavController | 導覽透過 SharedFlow Event | 🔴 Critical |
| 無自訂 Hilt Component | 全用 `@SingletonComponent` | 🔴 Critical |
| UseCase 不直接持有 View 引用 | UseCase 無任何 UI 相關 import | 🟡 Major |
| Repository 無業務邏輯判斷 | if/else 根據 code 決定流程必須在 UseCase | 🟡 Major |

**Critical 問題 = 直接 Rejected，不等其他驗收結果。**

---

### 2.2 SA 規格對照驗證

對照 sa.md 中的 API 規格，逐一確認：

#### API 層（Service / Repository）

```
檢查 {Feature}Service.kt：
- [ ] @GET/@POST/@PUT 與 SA 一致
- [ ] endpoint 路徑與 SA 一致
- [ ] @Query / @Body / @Path 參數與 SA 一致

檢查 {Feature}Response.kt：
- [ ] 所有 field 都有 @SerializedName
- [ ] field 名稱與 API 文件一致
- [ ] nullable 標記合理（非必填欄位應為 String?）

檢查 {Feature}RepositoryImpl.kt：
- [ ] 使用 .fold(onSuccess, onFailure) 處理結果
- [ ] onSuccess 呼叫 ApiUtil.parseECResult(it)
- [ ] onFailure 呼叫 it.getError().parErrorECResult()
```

#### Domain 層（UseCase）

```
檢查 {Feature}UseCase.kt：
- [ ] 業務邏輯封裝完整
- [ ] 無直接持有 DataStore 或 SharedPreference（透過 Manager）
- [ ] suspend fun 或 Flow 使用正確
```

#### UI 層（Screen / ViewModel）

```
檢查 {Feature}ViewModel.kt：
- [ ] UIState 定義包含 Loading / Success / Failure
- [ ] Event 透過 SharedFlow（_eventFlow）發出
- [ ] 無 navController.navigate() 直接呼叫

檢查 {Feature}Screen.kt：
- [ ] LaunchedEffect 收集 eventFlow
- [ ] when(uiState) 處理所有 sealed class 分支
- [ ] Loading 狀態有 Shimmer 或全螢幕 Loading 元件
- [ ] Failure 狀態有重試按鈕
- [ ] 點擊按鈕使用 singleClick { }
```

---

### 2.3 資源字串驗證

```
搜尋 app/src/ 中所有 Text(...) 與 contentDescription：
- [ ] 使用者可見的中文字串是否已移至 strings.xml？
- [ ] 使用 stringResource(R.string.xxx) 而非硬寫
- [ ] 例外：純程式用的 key（帶有 translatable="false"）
```

**快速檢查指令思路**（不執行，讀取分析）：
搜尋 `Text("` 或 `contentDescription = "` 的使用，確認無 hardcode 中文。

---

### 2.4 路由完整性驗證

```
- [ ] RootNavScreen（或對應 NavScreen）有新增路由物件
- [ ] NavGraph 有對應 composable { } 區塊
- [ ] 路由名稱與 SA 文件一致
- [ ] 帶參數路由使用 {ARG_NAME} 佔位符
```

---

### 2.5 Hilt 注入驗證

```
- [ ] 新增 Module 使用 @Module + @InstallIn(SingletonComponent::class)
- [ ] Repository binding 使用 @Binds（介面型）
- [ ] 有狀態的 class 使用 @Singleton
- [ ] ViewModel 使用 @HiltViewModel + @Inject constructor
```

---

## 3. 問題嚴重度分級

| 等級 | 符號 | 說明 | 對驗收的影響 |
|------|------|------|-------------|
| Critical | 🔴 | 架構違規、功能不可用 | 立刻 Rejected |
| Major | 🟡 | 功能有缺陷、規格不符 | Rejected（可帶修正清單）|
| Minor | 🟢 | 命名不規範、注釋缺失 | 不影響 Approved，列為建議 |

**Approved 條件**：0 Critical + 0 Major

---

## 4. test-report.md 填寫規範

按 `docs/_template/test-report.md` 格式填寫，必填欄位：

```
1. 基本資訊：Jira 單號、測試日期、整體結論
2. 測試結果摘要：Pass/Fail 數量統計表
3. 每個 TC 的結論與實際結果
4. 架構合規結果表
5. SA 對照結果表
6. 發現問題清單（如有）
7. 驗收結論（Approved / Rejected + 條件說明）
```

---

## 5. 回報格式

回報給 project-manager 時，固定格式：

```
── QA 驗收報告 ──────────────────────────────
功能：{功能名稱}（{PXBOX-XXXXX}）
驗收 Task：T{X}, T{Y}
整體結論：✅ Approved / ❌ Rejected

【Critical 問題】
- （無 / 列出）

【Major 問題】
- （無 / 列出）

【Minor 建議】
- （無 / 列出）

【完整報告】docs/{feature}/test-report.md
────────────────────────────────────────────
```

---

## 6. 禁止事項

- **禁止修改任何 app/src/ 程式碼**（QA 只讀，不改）
- **禁止自行決定「這個問題可以忽略」**（Critical/Major 一律回報）
- **禁止跳過 SA 對照**（每個 API/Model/UIState 都要對照）
- **禁止在沒有 test-case.md 的情況下開始驗收**
