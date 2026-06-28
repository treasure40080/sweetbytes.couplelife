# SA — 我們的家頁面：滿版 Lottie 動畫

> 系統分析文件（System Analysis）
> 描述「怎麼做」，是 android-developer 實作的直接依據。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0001 |
| 功能名稱 | 我們的家頁面 — 滿版 Lottie 動畫 |
| 版本 | v1.0 |
| 作者 | Tim_Yen |
| 建立時間 | 2026-06-28 |
| 最後更新 | 2026-06-28 |

---

## 架構概覽

此功能為純 UI 展示，無網路呼叫、無 ViewModel、無 UseCase。

```
MainActivity
    ↓ NavHost startDestination = "home"
HomeScreen（Composable）
    ↓ LottieAnimation（滿版）
assets/home_animation.json
```

---

## 現有程式碼分析

| 檔案 | 現況 |
|------|------|
| `MainActivity.kt` | 已有 NavHost，startDestination = `Screen.Home.route`（"home"） |
| `Screen.kt` | 已有 `Screen.Home`，route = "home" |
| `MainScreens.kt` | `HomeScreen()` 已存在，目前只是 placeholder Text |
| `android/build.gradle.kts` | **尚未加入 Lottie 依賴** |

---

## 依賴變更

### `android/build.gradle.kts`

新增 Lottie Compose 依賴：

```kotlin
implementation("com.airbnb.android:lottie-compose:6.4.0")
```

---

## 資源檔案

| 路徑 | 說明 |
|------|------|
| `android/src/main/res/raw/home_animation.json` | Lottie 動畫 JSON（由使用者提供，放置於 res/raw/） |

---

## 元件設計

### HomeScreen 修改

**檔案**：`android/src/main/java/com/sweetbytes/couplelife/ui/screen/MainScreens.kt`

將原有 placeholder 替換為 Lottie 滿版動畫：

```kotlin
@Composable
fun HomeScreen() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.home_animation)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxSize()
    )
}
```

**說明**：
- `LottieCompositionSpec.Asset` — 從 assets/ 讀取 JSON
- `iterations = LottieConstants.IterateForever` — 無限循環播放
- `Modifier.fillMaxSize()` — 滿版，不留任何 padding

---

## 路由定義

無需修改，`Screen.Home` 與 NavHost startDestination 已存在。

---

## 影響範圍

### 修改檔案

| 檔案路徑 | 修改內容 |
|----------|----------|
| `android/build.gradle.kts` | 新增 lottie-compose 依賴 |
| `android/src/main/java/com/sweetbytes/couplelife/ui/screen/MainScreens.kt` | 替換 HomeScreen 內容為 LottieAnimation |

### 新增資源

| 路徑 | 說明 |
|------|------|
| `android/src/main/assets/home_animation.json` | Lottie 動畫檔（使用者提供） |

---

## TodoList（android-developer 實作順序）

- [x] T1：`android/build.gradle.kts` 新增 lottie-compose 依賴
- [x] T2：將 JSON 檔放入 `android/src/main/res/raw/`
- [x] T3：修改 `HomeScreen()`，替換 placeholder 為 LottieAnimation
- [x] T4：Sync Gradle，確認編譯無誤

---

## 驗收標準

- [x] APP 啟動後直接進入我們的家頁面
- [x] Lottie 動畫滿版顯示，無多餘留白
- [x] 動畫無限循環播放，不閃退
- [x] 編譯無警告（Lottie 依賴正確引入）
