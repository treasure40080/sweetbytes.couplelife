# COUPLE-LIFE — AI Agent 操作手冊

## 📱 專案概覽

| 項目 | 內容 |
|------|------|
| 語言 | Kotlin |
| UI 框架 | Jetpack Compose (Material3) |
| 架構 | Clean Architecture + MVVM (Single Activity) |
| DI | Hilt 2.51.1（KAPT，全用 SingletonComponent） |
| 網路層 | Retrofit  |
| 狀態管理 | StateFlow（UI 狀態）+ SharedFlow（單次事件） |
| 導覽 | Compose Navigation — RootNavGraph → BottomNavGraphV2 / LoginNavGraph |
| Build Flavors | DEV / UAT / PROD |
| Min SDK | 26 \| Target SDK 35 \| Kotlin 2.0.0 \| Java 21 |

---

## 🗺️ 任務開始前：讀取 Skill Index

> **任何開發任務開始前，必須先讀取：`.agents/skills/android_skill_index/SKILL.md`**
> 依照任務類型找到對應 Skill，依規範執行。

| 涉及類型 | 必讀 Skill 路徑 |
|----------|----------------|
| 新增 API / 資料模型 / Token | `.agents/skills/data_layer_mastery/SKILL.md` |
| DI 注入 / Module 設定 | `.agents/skills/dependency_injection_mastery/SKILL.md` |
| 頁面導覽 / 路由定義 | `.agents/skills/navigation_patterns/SKILL.md` |
| Compose UI / 架構風格 | `.agents/skills/coding_style_conventions/SKILL.md` |
| UI/UX 元件 / 主題顏色 | `.agents/skills/ui_ux_enginerring/SKILL.md` |
| Git Commit 訊息 | `.agents/skills/git_commit_convention/SKILL.md` |
| SA 文件撰寫 | `.agents/skills/sa_document_convention/SKILL.md` |
| 開發流程全覽（SSOT） | `.agents/skills/development_workflow/SKILL.md` |
| AI 踩坑紀錄 | `.agents/skills/ai_coding_rules/AI_CODING_RULES.md` |

---

## 🔄 標準開發流程（10 步驟）

> 完整規範詳見 `.agents/skills/development_workflow/SKILL.md`

1. **確認 單號** — 與當前 git branch 前綴一致（如 `COUPLELIFE-26837`）
2. **讀取規格** — 查閱 `specs/requirements/` 對應文件
3. **列出 Task List** — 在對話中呈現精確清單並標註進度
4. **步進式執行** — 一次一個子任務，完成後貼出實作對照表請求 Review
5. **階段性提交** — 每個邏輯段落確認無誤後執行 `git commit`
6. **建立 / 更新 SA** — 在 `specs/sa/` 維護系統分析文件
7. **實作前審計** — 對齊對照表，包含「舊程式碼邏輯分析」
8. **程式碼實作** — 依 SA 與 Skill 規範撰寫
9. **驗證報告** — 產出 `walkthrough.md`（含測試情境與結果）
10. **Git 提交** — 依 `git_commit_convention` 格式，需使用者明確授權

---

## 🚫 核心禁止事項

### 架構層
- 禁止 UI 層（Screen / Activity）直接呼叫 Repository，一律透過 UseCase
- 禁止 ViewModel 持有 `Context` 或任何 View 引用
- 禁止 ViewModel 持有或直接呼叫 `NavController`（導覽透過 SharedFlow 事件）
- 禁止在 Repository 做業務邏輯判斷（if/else 根據 code 決定流程）
- 禁止在 UseCase 以外直接操作 DataStore 寫入（Token 刷新由 `TokenManager` 負責）
- 禁止多個 Service 共用同一個 OkHttpClient 或 Retrofit 實例
- 禁止建立自訂 Hilt Component，維持全 `SingletonComponent`

### 流程層
- 禁止在未更新 SA 的情況下直接修改程式碼
- 禁止未獲使用者明確授權就執行 `git commit`
- 禁止跳過 Task List 中的任何項目
- 禁止自行猜測規格（有疑點必須彙整後詢問使用者）

### 資源層
- 所有圖檔靜態資源必須管理於 `Assets.xcassets`，禁止散落在自訂目錄
- 增刪實體檔案後，必須提醒使用者手動更新專案結構

---

## ✅ Session 開始 Checklist

每次對話開始，自動確認以下事項：

- [ ] 當前 git branch 是什麼？（對應的 單號？）
- [ ] 已讀取 `android_skill_index/SKILL.md` 並找到對應 Skill？
- [ ] 任務是否有對應的規格文件（`specs/requirements/`）？
- [ ] 是否需要建立 Task List？

---

## 📂 關鍵目錄速查

```
com/sweetbytes/couplelife/
├── base/           # BaseActivity、共用 ViewModel
├── ui/screen/      # Compose Screens
├── ui/theme/      # Compose Themes

---

## 🤖 Agentic 開發模式

> 複雜功能開發時，由 **project-manager** 子代理人統籌協調，派工給 **android-developer** 與 **android-qa**。

### 三大支柱

| 支柱 | 路徑 | 說明 |
|------|------|------|
| 🔧 Harness | `.claude/` | hooks、settings、agent 定義 |
| 📄 Docs | `docs/` | 唯一真相來源（pjm.md / SA / 測試報告） |
| 💻 Output | `app/src/` | 程式碼輸出，不含決策邏輯 |

### Agent 角色

| Agent | 定義檔 | 職責 |
|-------|--------|------|
| project-manager | `.claude/agents/project-manager.md` | 讀取需求 → 拆解任務 → 維護 pjm.md → 派工 → 驗收 |
| android-developer | `.claude/agents/android-developer.md` | 依 SA 實作 Kotlin/Compose 程式碼，只寫 `app/src/` |
| android-qa | `.claude/agents/android-qa.md` | 依測試計畫驗證，產出 test-report.md |

### pjm.md — 唯一狀態真相

- 路徑：`docs/{feature}/pjm.md`
- 包含：需求摘要、Task List（含狀態）、派工記錄、驗收結果
- **對話記憶不算，永遠以 `pjm.md` 為準**
- 每次 session 開始，project-manager 必須先讀取 pjm.md 確認進度

### 啟動方式

```
你是 project-manager agent，請讀取 docs/{feature}/pjm.md
確認目前進度，繼續下一個 Task。
```

### 🔑 Keyword 啟動：Harness Engineer 開發流程

當使用者輸入以下格式的 keyword：

```
Harness Engineer開發COUPLELIFE-XXXXX
```

**必須按照以下流程執行**：

1. 從 keyword 解析 Jira 單號（例如 `PXBOX-27349`）
2. 執行 `git branch` 確認當前分支，核對分支前綴是否與 Jira 單號一致
3. 檢查 `docs/COUPLELIFE-XXXXX/` 是否存在：

   **不存在** → 建立資料夾，依序建立文件：
   ```
   docs/COUPLELIFE-XXXXX/
   ├── req.md    ← 第一步，填寫需求描述與相關連結，完成後詢問使用者確認
   ├── pjm.md    ← 第二步，req.md 確認後才建立
   └── sa.md     ← 第三步，pjm.md Phase 1 完成後才建立
   ```

   **已存在** → 依序讀取現有文件：
   ```
   1. 讀取 req.md（確認需求）
   2. 讀取 pjm.md（確認進度）
   3. 讀取 sa.md（確認規格，若存在）
   4. 回報目前狀態，詢問繼續哪個步驟
   ```

4. **req.md 是前置條件**：沒有 req.md 不能建立 pjm.md；沒有 pjm.md 不能建立 sa.md

### 模板位置

```
docs/_template/
├── req.md          # 需求文件模板
├── pjm.md          # 專案任務書模板
├── sa.md           # 系統分析文件模板
├── test-case.md    # 測試案例模板
└── test-report.md  # 測試報告模板
```

---

*Skills 最後更新：2026-06-28*
