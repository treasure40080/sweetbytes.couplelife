# SA — 設定頁面：伴侶邀請碼 / 版本號碼

> 系統分析文件（System Analysis）
> 描述「怎麼做」，是 android-developer 實作的直接依據。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0004 |
| 功能名稱 | 設定頁面 — 伴侶邀請碼 / 版本號碼 |
| 版本 | v1.0 |
| 作者 | Tim_Yen |
| 建立時間 | 2026-06-28 |
| 最後更新 | 2026-06-28 |

---

## 架構概覽

純 UI 頁面，無 ViewModel / UseCase / Repository / Room。

```
SettingsScreen（Composable）
├── 邀請碼區塊：Text + IconButton（複製 → ClipboardManager → Toast）
└── 版本區塊：Text（BuildConfig.VERSION_NAME）
```

---

## 依賴變更

無新增依賴。`BuildConfig` 由 Android Gradle Plugin 自動生成，直接使用。

---

## 元件設計

### SettingsScreen

**檔案**：`ui/screen/settings/SettingsScreen.kt`

**結構**：
```
SettingsScreen
└── Column（fillMaxSize, padding 16.dp）
    ├── SettingsSectionTitle("伴侶連結")
    ├── InviteCodeCard（邀請碼 + 複製按鈕）
    ├── Spacer（weight(1f)）
    └── VersionText（"v{BuildConfig.VERSION_NAME}"）
```

### InviteCodeCard

```kotlin
Card（fillMaxWidth）
└── Row（SpaceBetween, padding 16.dp）
    ├── Column
    │   ├── Text("邀請碼", style = labelSmall, color = onSurfaceVariant)
    │   └── Text(inviteCode, style = titleMedium, fontWeight = Bold)
    └── IconButton（Icons.Outlined.ContentCopy）
        → 點擊：ClipboardManager.setPrimaryClip + Toast "已複製邀請碼"
```

### 邀請碼來源

此階段 hardcode 靜態字串：
```kotlin
const val INVITE_CODE = "COUPLE-2026"
```

### 版本號碼

```kotlin
Text(
    text = "v${BuildConfig.VERSION_NAME}",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.fillMaxWidth(),
    textAlign = TextAlign.Center
)
```

### 複製邏輯

```kotlin
val clipboardManager = LocalClipboardManager.current
val context = LocalContext.current

// 點擊複製
clipboardManager.setText(AnnotatedString(inviteCode))
Toast.makeText(context, "已複製邀請碼", Toast.LENGTH_SHORT).show()
```

---

## 影響範圍

### 新增檔案

| 檔案路徑 | 說明 |
|----------|------|
| `ui/screen/settings/SettingsScreen.kt` | 設定主畫面 |

### 修改檔案

| 檔案路徑 | 修改內容 |
|----------|----------|
| `ui/screen/MainScreens.kt` | 移除 SettingsScreen placeholder，改 import 新實作 |

---

## TodoList（android-developer 實作順序）

- [x] T1：建立 `ui/screen/settings/SettingsScreen.kt`（邀請碼 Card + 複製 + 版本號）
- [x] T2：更新 `ui/screen/MainScreens.kt`，替換 SettingsScreen placeholder
- [x] T3：確認編譯無誤

---

## 驗收標準

- [x] 進入頁面顯示邀請碼「COUPLE-2026」
- [x] 點擊複製 icon → Toast「已複製邀請碼」
- [x] 頁面底部顯示 `v1.0`（BuildConfig.VERSION_NAME 動態取得）
- [x] 架構合規：純 UI，無 ViewModel / Repository
