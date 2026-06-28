# Test Case — 我們的家頁面：滿版 Lottie 動畫

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0001 |
| 功能名稱 | 我們的家頁面 — 滿版 Lottie 動畫 |
| SA 文件 | `docs/COUPLELIFE-0001/sa.md` |
| 建立時間 | 2026-06-28 |
| 版本 | v1.0 |

---

## 功能測試案例

### TC-001：APP 啟動直接進入我們的家頁面

**前置條件**：APP 已安裝，冷啟動

**步驟**：
1. 點擊 APP icon 啟動

**預期結果**：
- 第一個看到的頁面是「我們的家」（Home）
- 底部導覽列預設選中 Home tab

---

### TC-002：Lottie 動畫滿版顯示

**前置條件**：已進入我們的家頁面

**步驟**：
1. 觀察畫面

**預期結果**：
- Lottie 動畫佔滿整個可用螢幕空間（Content Area，不含底部 NavigationBar）
- 動畫四邊無多餘留白或黑邊

---

### TC-003：Lottie 動畫無限循環播放

**前置條件**：已進入我們的家頁面

**步驟**：
1. 等待動畫播放完一輪
2. 觀察動畫是否繼續播放

**預期結果**：
- 動畫自動重播，不停止在最後一幀

---

### TC-004：切換頁籤後返回，動畫仍正常

**前置條件**：已進入我們的家頁面，動畫正在播放

**步驟**：
1. 點擊底部導覽列切換至其他頁籤
2. 再點擊 Home tab 返回

**預期結果**：
- 返回後 Lottie 動畫正常顯示並播放
- 無空白、無閃退

---

## 單元測試案例

> 此功能為純 UI 展示，無 ViewModel / UseCase / Repository，不需要單元測試。

---

## 架構合規驗證

| 檢查項目 | 預期 | 結果 |
|----------|------|------|
| UI 未直接呼叫 Repository | ✅ 無 Repository | ✅ Pass |
| ViewModel 未持有 Context | ✅ 無 ViewModel | ✅ Pass |
| ViewModel 未持有 NavController | ✅ 無 ViewModel | ✅ Pass |
| 未建立自訂 Hilt Component | ✅ 無 DI | ✅ Pass |
| Lottie 檔案放置於 res/raw/ | ✅ | ✅ Pass |

---

## SA 對照驗證

| SA 規格項目 | 實作符合 |
|-------------|---------|
| `LottieCompositionSpec.RawRes(R.raw.home_animation)` | ✅ Pass |
| `iterations = LottieConstants.IterateForever` | ✅ Pass |
| `Modifier.fillMaxSize()` | ✅ Pass |
| startDestination = Screen.Home.route | ✅ Pass |

---

## 測試排除項目（Out of Scope）

- 動畫播放完後的互動行為
- 使用者登入 / 配對邏輯
- 其他頁面功能
