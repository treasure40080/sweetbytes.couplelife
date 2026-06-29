# Test Case — 記帳流水頁面

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0002 |
| 功能名稱 | 記帳流水頁面 — 新增 / 編輯 / 刪除記帳 / 月份篩選 / Room 持久化 |
| SA 文件 | `docs/COUPLELIFE-0002/sa.md` |
| 建立時間 | 2026-06-28 |
| 版本 | v1.0 |

---

## 功能測試案例

### TC-001：進入頁面預設顯示當月記帳清單

**前置條件**：APP 已安裝，進入記帳頁籤

**步驟**：
1. 點擊底部 Navigation Bar「記帳」頁籤

**預期結果**：
- TopAppBar 顯示當前年/月（如 `2026 / 06`）
- 若有資料則顯示當月清單，依新增時間倒序排列
- 若無資料則顯示空狀態提示文字

---

### TC-002：月份切換，清單隨之更新

**前置條件**：已進入記帳頁面

**步驟**：
1. 點擊 TopAppBar 的年月按鈕
2. 在 MonthPickerDialog 選擇其他年/月
3. 點擊確認

**預期結果**：
- TopAppBar 年月更新為選擇的月份
- 清單更新為該月份的記帳資料

---

### TC-003：新增記帳（收入）

**前置條件**：已進入記帳頁面

**步驟**：
1. 點擊 FAB（+）
2. 選擇「收入」
3. 輸入金額 `5000`
4. 選取項目「薪水」
5. 點擊確認

**預期結果**：
- Dialog 關閉
- 清單最上方出現新增的記帳項目，顯示 `+5000`（綠色）、分類「薪水」

---

### TC-004：新增記帳（支出，自定義項目）

**前置條件**：已進入記帳頁面

**步驟**：
1. 點擊 FAB（+）
2. 選擇「支出」
3. 輸入金額 `200`
4. 選取項目「其他」，輸入自定義名稱「咖啡」
5. 點擊確認

**預期結果**：
- 清單出現分類「咖啡」、`-200`（紅色）的項目

---

### TC-005：新增記帳驗證（金額空白）

**前置條件**：AddEntryDialog 已開啟

**步驟**：
1. 不輸入金額，直接點擊確認

**預期結果**：
- Dialog 不關閉
- 金額欄位顯示錯誤提示「金額不能為空」

---

### TC-006：新增記帳驗證（選「其他」但名稱空白）

**前置條件**：AddEntryDialog 已開啟，已選「其他」

**步驟**：
1. 輸入金額 `100`
2. 選取「其他」，不填自定義名稱
3. 點擊確認

**預期結果**：
- Dialog 不關閉
- 自定義名稱欄位顯示錯誤提示「名稱不能為空」

---

### TC-007：編輯記帳項目

**前置條件**：清單中有至少一筆資料

**步驟**：
1. 點擊某筆記帳 Card
2. 在 EditEntryDialog 修改金額為 `999`
3. 點擊確認

**預期結果**：
- Dialog 關閉
- 清單中該筆金額更新為 `999`

---

### TC-008：刪除記帳項目

**前置條件**：清單中有至少一筆資料

**步驟**：
1. 點擊某筆記帳右側垃圾桶 icon
2. 確認 AlertDialog 跳出
3. 點擊「確定」

**預期結果**：
- Dialog 關閉
- 該筆資料從清單消失

---

### TC-009：取消刪除

**前置條件**：DeleteConfirmDialog 已開啟

**步驟**：
1. 點擊「取消」

**預期結果**：
- Dialog 關閉
- 資料保留，清單不變

---

### TC-010：清單可捲動，不被 TopBar / BottomBar 遮住

**前置條件**：當月記帳資料超過一頁

**步驟**：
1. 向上/向下捲動清單

**預期結果**：
- 第一筆資料不被 TopAppBar 遮住
- 最後一筆資料可捲至 FAB 和 BottomBar 上方
- 捲動順暢無卡頓

---

### TC-011：重開 APP 資料仍存在

**前置條件**：已新增至少一筆記帳資料

**步驟**：
1. 完全關閉 APP
2. 重新開啟

**預期結果**：
- 記帳清單資料與關閉前一致

---

## 架構合規驗證

| 檢查項目 | 預期 | 結果 |
|----------|------|------|
| UI 未直接呼叫 Repository | ✅ 透過 UseCase | ✅ Pass |
| ViewModel 未持有 Context | ✅ | ✅ Pass |
| ViewModel 未持有 NavController | ✅ | ✅ Pass |
| 未建立自訂 Hilt Component | ✅ 全用 SingletonComponent | ✅ Pass |
| 單次事件透過 SharedFlow 回傳 | ✅ EntryEvent | ✅ Pass |
| Room DAO 查詢回傳 Flow | ✅ | ✅ Pass |

---

## SA 對照驗證

| SA 規格項目 | 實作符合 |
|-------------|---------|
| EntryEntity（id / amount / type / category / createdAt） | ✅ Pass |
| EntryDao @Update / @Delete | ✅ Pass |
| Repository 介面含 updateEntry / deleteEntry | ✅ Pass |
| UpdateEntryUseCase / DeleteEntryUseCase | ✅ Pass |
| EntryViewModel 注入 4 個 UseCase | ✅ Pass |
| LazyColumn 使用 contentPadding | ✅ Pass |
| 垃圾桶 icon = Icons.Outlined.Delete，tint = onSurfaceVariant | ✅ Pass |

---

## 測試排除項目（Out of Scope）

- 雲端同步 / 多裝置資料共享
- 統計圖表分析
- 使用者登入 / 配對邏輯
