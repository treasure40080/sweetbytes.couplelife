# Test Report — 統計圖表頁面

> 測試報告，由 android-qa Agent 填寫，回報給 project-manager 驗收。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0003 |
| 功能名稱 | 統計頁面 — 圓餅圖（收支比例 / 分類佔比） |
| 測試日期 | 2026-06-28 |
| 測試執行者 | 使用者人工測試 |
| 整體結論 | ✅ Approved |

---

## 測試結果摘要

| 類別 | 總數 | Pass | Fail | Skip |
|------|------|------|------|------|
| 功能測試 | 10 | 10 | 0 | 0 |
| 架構合規 | 5 | 5 | 0 | 0 |
| SA 對照 | 7 | 7 | 0 | 0 |
| **合計** | **22** | **22** | **0** | **0** |

---

## 功能測試結果

### TC-001：進入頁面預設顯示當月資料
- **結論**：✅ Pass
- **實際結果**：TopAppBar 顯示當前年月，兩個圓餅圖正確顯示，各分類佔比預設選「支出」Tab

### TC-002：無資料時顯示空狀態
- **結論**：✅ Pass
- **實際結果**：無資料月份顯示「本月尚無記帳資料」，不顯示圖表

### TC-003：月份切換，圖表隨之更新
- **結論**：✅ Pass
- **實際結果**：MonthPickerDialog 正常開啟，切換後兩個圖表同步更新

### TC-004：收入 vs 支出圓餅圖顏色正確
- **結論**：✅ Pass
- **實際結果**：收入綠色、支出紅色，圖例顯示正確

### TC-005：僅有收入或僅有支出時圓餅圖顯示 100%
- **結論**：✅ Pass
- **實際結果**：單一扇形正確顯示 100%

### TC-006：各分類佔比 — 預設顯示支出分類
- **結論**：✅ Pass
- **實際結果**：TabRow 預設選中「支出」，各分類顏色不同

### TC-007：各分類佔比 — 切換至收入分類
- **結論**：✅ Pass
- **實際結果**：點擊「收入」Tab 後圖表立即切換為收入分類資料

### TC-008：該類型無資料時顯示提示
- **結論**：✅ Pass
- **實際結果**：顯示「本月無收入記帳」提示，不顯示空圓餅圖

### TC-009：自定義分類顯示正確
- **結論**：✅ Pass
- **實際結果**：自定義名稱正確顯示於圖例，顏色與預設分類區分

### TC-010：頁面可上下捲動
- **結論**：✅ Pass
- **實際結果**：verticalScroll 正常，不被 TopAppBar / BottomBar 遮住

---

## 架構合規結果

| 檢查項目 | 結論 | 備註 |
|----------|------|------|
| UI 未直接呼叫 Repository | ✅ | 透過 GetEntriesByMonthUseCase |
| ViewModel 未持有 Context | ✅ | Calendar.getInstance() 僅取值 |
| ViewModel 未持有 NavController | ✅ | |
| 複用既有 GetEntriesByMonthUseCase | ✅ | 無重複建立 UseCase |
| 未建立自訂 Hilt Component | ✅ | SingletonComponent |

---

## SA 對照結果

| SA 規格項目 | 實作結果 | 備註 |
|-------------|----------|------|
| ChartUiState 含 expenseCategoryData / incomeCategoryData / categoryFilter | ✅ 符合 | 需求追加後同步更新 |
| calcIncomeExpense 回傳收入綠 / 支出紅 | ✅ 符合 | |
| calcCategory 依分類分組計算百分比 | ✅ 符合 | |
| PRESET_CATEGORY_COLORS 對應固定分類 | ✅ 符合 | |
| 各分類佔比 TabRow 切換支出 / 收入 | ✅ 符合 | |
| PieChartView 使用 AndroidView 包裝 MPAndroidChart | ✅ 符合 | |
| MonthPickerDialog 複用既有元件 | ✅ 符合 | |

---

## 發現問題清單

| 編號 | 嚴重度 | 描述 | 狀態 |
|------|--------|------|------|
| - | - | 無發現問題 | - |

---

## 驗收結論

### ✅ Approved（驗收通過）

**允許 commit 的條件確認**：
- [x] 0 個 Critical Bug
- [x] 0 個 Major Bug
- [x] 架構合規全數通過
- [x] SA 對照全數符合
