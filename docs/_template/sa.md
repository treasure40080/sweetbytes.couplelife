# SA — {功能名稱}

> 系統分析文件（System Analysis）
> 描述「怎麼做」，是 android-developer 實作的直接依據。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-XXXXX |
| 功能名稱 | {功能名稱} |
| 版本 | v1.0 |
| 作者 | {作者} |
| 建立時間 | YYYY-MM-DD |
| 最後更新 | YYYY-MM-DD |

---

## 架構概覽

```
UI Layer (Compose Screen)
    ↓ collectAsState / LaunchedEffect
ViewModel
    ↓ callUseCase()
UseCase
    ↓ repository.fetchXxx()
Repository（Interface in domain, Impl in data）
    ↓ service.apiCall()
Retrofit Service
    ↓ HTTP
後端 API
```

---

## API 規格

### {API 名稱}

**Request**

**錯誤碼**

## 資料模型

### DTO（網路層）

## 元件設計

### ViewModel

### Screen 結構

---

## 路由定義

---

## 影響範圍

### 新增檔案

| 檔案路徑 | 說明 |
|----------|------|
| `data/remote/{FeatureName}Service.kt` | Retrofit 介面 |
| `data/model/{FeatureName}Response.kt` | DTO |
| `data/repository/{FeatureName}RepositoryImpl.kt` | Repository 實作 |
| `domain/repository/{FeatureName}Repository.kt` | Repository 介面 |
| `domain/usecase/{FeatureName}UseCase.kt` | UseCase |
| `ui/screen/{featureName}/{FeatureName}Screen.kt` | 主畫面 |
| `ui/screen/{featureName}/{FeatureName}ViewModel.kt` | ViewModel |

### 修改檔案

| 檔案路徑 | 修改內容 |
|----------|----------|
| `di/{FeatureName}Module.kt` | 新增 Hilt binding |
| `navigation/RootNavScreen.kt` | 新增路由 |
| `navigation/RootNavGraph.kt` | 新增 composable |
| `res/values/strings.xml` | 新增字串資源 |

---

## 驗收標準

- [ ] {驗收條件 1}
- [ ] {驗收條件 2}
- [ ] 無架構違規（UI 不直接呼叫 Repository）
- [ ] 所有使用者可見文字使用 stringResource
