# PJM — 統計圖表頁面開發

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0003 |
| 功能名稱 | 統計頁面 — 圓餅圖（收支比例 / 分類佔比） |
| 負責開發者 | Tim_Yen |
| 建立時間 | 2026-06-28 |
| 最後更新 | 2026-06-28 |
| 目前狀態 | ✅ 完成 |
| 需求文件 | [req.md](./req.md) |

---

## Task List

> 狀態：`[ ]` 待做 ｜ `[→]` 進行中 ｜ `[x]` 完成 ｜ `[!]` 阻塞

### Phase 1 — 規格確認

- [x] 讀取需求規格，產出問題清單
- [x] 與 PO 確認需求，req.md 確認完成

### Phase 2 — 開發實作

- [x] 建立 SA 文件（`docs/COUPLELIFE-0003/sa.md`）
- [x] 確認 SA 文件（android-developer 自審，PM 核准後開工）
- [x] 新增 GetEntriesByMonthUseCase 資料取得（複用既有 UseCase）
- [x] 建立 ChartViewModel（UIState / 圖表資料計算 / 支出收入分類切換）
- [x] 建立 PieChartView（MPAndroidChart AndroidView 包裝）
- [x] 建立 ChartScreen（月份切換 + 兩個圓餅圖 + 圖例 + TabRow）
- [x] 接入 Navigation，確保頁面可正常進入

### Phase 3 — 測試驗收

- [x] 建立 test-case.md
- [x] QA 測試驗證
- [x] 修正 QA 回報問題（無發現問題）
- [x] 最終 git commit

---

## 派工記錄（Spawn Log）

| 時間 | 派給 | Task | 狀態 |
|------|------|------|------|
| 2026-06-28 | project-manager | Phase 1 規格確認 | ✅ 完成 |
| 2026-06-28 | android-developer | Phase 2 全部實作（MPAndroidChart / ChartViewModel / ChartScreen） | ✅ 完成 |
| 2026-06-28 | android-qa | Phase 3 測試驗收 | ✅ 完成 |

---

## 驗收記錄

| 時間 | 驗收人 | 結論 | 備註 |
|------|--------|------|------|
| 2026-06-28 | android-qa | ✅ Approved — 22/22 通過，無發現問題 | - |

---

## 阻塞紀錄

| 時間 | 阻塞原因 | 解除方式 | 解除時間 |
|------|----------|----------|----------|
| - | - | - | - |
