# PJM — APP初次開發：我們的家頁面

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0001 |
| 功能名稱 | 我們的家頁面 — 滿版 Lottie 動畫 |
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

- [x] 建立 SA 文件（`docs/COUPLELIFE-0001/sa.md`）
- [x] 確認 SA 文件（android-developer 自審，PM 核准後開工）
- [x] 新增 Lottie 依賴至 build.gradle
- [x] 準備 Lottie JSON 動畫檔案，放置於 res/raw/
- [x] 建立 HomeScreen（我們的家頁面）Compose UI
- [x] 接入 Navigation，確保頁面可正常進入

### Phase 3 — 測試驗收

- [x] 建立 test-case.md
- [x] QA 測試驗證
- [x] 修正 QA 回報問題（DOC-001：sa.md 文件同步修正）
- [ ] 最終 git commit

---

## 派工記錄（Spawn Log）

| 時間 | 派給 | Task | 狀態 |
|------|------|------|------|
| 2026-06-28 | project-manager | Phase 1 規格確認 | ✅ 完成 |
| 2026-06-28 | android-developer | Phase 2 實作 HomeScreen + Lottie | ✅ 完成 |
| 2026-06-28 | android-qa | Phase 3 靜態驗證 + 人工測試驗收 | ✅ 完成 |

---

## 驗收記錄

| 時間 | 驗收人 | 結論 | 備註 |
|------|--------|------|------|
| 2026-06-28 | android-qa | ✅ Approved — 13/13 通過，DOC-001 已修正 | - |

---

## 阻塞紀錄

| 時間 | 阻塞原因 | 解除方式 | 解除時間 |
|------|----------|----------|----------|
| - | - | - | - |
