# Test Report — 記帳流水頁面

> 測試報告，由 android-qa Agent 填寫，回報給 project-manager 驗收。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0002 |
| 功能名稱 | 記帳流水頁面 — 新增 / 編輯 / 刪除記帳 / 月份篩選 / Room 持久化 |
| 測試日期 | 2026-06-28 |
| 測試執行者 | android-qa Agent + 使用者人工測試 |
| 整體結論 | ✅ Approved |

---

## 測試結果摘要

| 類別 | 總數 | Pass | Fail | Skip |
|------|------|------|------|------|
| 功能測試 | 11 | 11 | 0 | 0 |
| 架構合規 | 6 | 6 | 0 | 0 |
| SA 對照 | 7 | 7 | 0 | 0 |
| **合計** | **24** | **24** | **0** | **0** |

---

## 功能測試結果

### TC-001：進入頁面預設顯示當月記帳清單
- **結論**：✅ Pass
- **實際結果**：TopAppBar 顯示當前年月，清單正確顯示當月資料，無資料時顯示空狀態提示

### TC-002：月份切換，清單隨之更新
- **結論**：✅ Pass
- **實際結果**：MonthPickerDialog 正常開啟，選擇後 TopAppBar 更新，清單同步切換

### TC-003：新增記帳（收入）
- **結論**：✅ Pass
- **實際結果**：新增後 Dialog 關閉，清單最上方出現正確資料，金額顯示為綠色 +5000

### TC-004：新增記帳（支出，自定義項目）
- **結論**：✅ Pass
- **實際結果**：自定義名稱「咖啡」正確儲存並顯示，金額紅色 -200

### TC-005：新增記帳驗證（金額空白）
- **結論**：✅ Pass
- **實際結果**：Dialog 不關閉，顯示「金額不能為空」錯誤提示

### TC-006：新增記帳驗證（選「其他」但名稱空白）
- **結論**：✅ Pass
- **實際結果**：Dialog 不關閉，顯示「名稱不能為空」錯誤提示

### TC-007：編輯記帳項目
- **結論**：✅ Pass
- **實際結果**：EditEntryDialog 預填原始資料，修改後清單即時更新

### TC-008：刪除記帳項目
- **結論**：✅ Pass
- **實際結果**：DeleteConfirmDialog 正常顯示，確定後資料從清單移除

### TC-009：取消刪除
- **結論**：✅ Pass
- **實際結果**：點取消後 Dialog 關閉，資料保留

### TC-010：清單可捲動，不被 TopBar / BottomBar 遮住
- **結論**：✅ Pass
- **實際結果**：contentPadding 正確設定，第一筆與最後一筆均可完整顯示

### TC-011：重開 APP 資料仍存在
- **結論**：✅ Pass
- **實際結果**：Room 持久化正常，重啟後資料完整保留

---

## 架構合規結果

| 檢查項目 | 結論 | 備註 |
|----------|------|------|
| UI 未直接呼叫 Repository | ✅ | 透過 UseCase 呼叫 |
| ViewModel 未持有 Context | ✅ | Calendar.getInstance() 僅取值，無 Context 儲存 |
| ViewModel 未持有 NavController | ✅ | 導覽透過 SharedFlow 事件 |
| 未建立自訂 Hilt Component | ✅ | 全用 SingletonComponent |
| 單次事件透過 SharedFlow 回傳 | ✅ | EntryEvent sealed class |
| Room DAO 查詢回傳 Flow | ✅ | getEntriesByMonth 回傳 Flow |

---

## SA 對照結果

| SA 規格項目 | 實作結果 | 備註 |
|-------------|----------|------|
| EntryEntity（id / amount / type / category / createdAt） | ✅ 符合 | |
| EntryDao @Update / @Delete | ✅ 符合 | |
| Repository 介面含 updateEntry / deleteEntry | ✅ 符合 | |
| UpdateEntryUseCase / DeleteEntryUseCase | ✅ 符合 | |
| EntryViewModel 注入 4 個 UseCase | ✅ 符合 | |
| LazyColumn 使用 contentPadding | ✅ 符合 | |
| 垃圾桶 icon = Icons.Outlined.Delete，tint = onSurfaceVariant | ✅ 符合 | |

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
