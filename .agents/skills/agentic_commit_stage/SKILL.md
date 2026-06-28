---
name: AgenticCommitStage
description: |
  Agentic 多代理人流程中的 git commit 分段策略與授權協議。
  定義什麼時候可以 commit、每個 commit 要包含什麼、如何取得使用者授權。
  適用對象：project-manager（授權判斷）、android-developer（準備 commit）
---

# Agentic Commit Stage — 分段提交策略

> 適用對象：**project-manager**（授權）、**android-developer**（執行）
> 核心原則：每個 commit 必須代表一個獨立完整的邏輯單元，且必須通過 QA 驗收或 project-manager 明確授權。

---

## 1. Commit 觸發條件

**以下條件全部滿足，才能進行 git commit**：

- [ ] android-qa 驗收結論為 **Approved**（test-report.md 有明確記錄）
- [ ] pjm.md 中本次 commit 範圍的所有 Task 狀態為 `[x]`
- [ ] 使用者（Tim）明確授權（口頭確認或明確指令）
- [ ] commit message 符合 `git_commit_convention/SKILL.md` 格式

---

## 2. 分段 Commit 策略

不要一次 commit 整個功能，按以下邏輯分段：

### Stage 1 — Data Layer Commit

**包含**：
- `{Feature}Response.kt`（DTO）
- `{Feature}Service.kt`（Retrofit 介面）
- `{Feature}Repository.kt`（domain 介面）
- `{Feature}RepositoryImpl.kt`（data 實作）
- `{Feature}Module.kt`（Hilt DI）

**Commit Message 範例**：
```
[feature] PXBOX-XXXXX 建立 {功能名} data layer
```

**觸發時機**：data layer 完成且 QA 確認無 Critical 問題後

---

### Stage 2 — Domain + ViewModel Commit

**包含**：
- `{Feature}UseCase.kt`
- `{Feature}Vo.kt`（若有）
- `{Feature}ViewModel.kt`

**Commit Message 範例**：
```
[feature] PXBOX-XXXXX 建立 {功能名} UseCase 與 ViewModel
```

**觸發時機**：UseCase + ViewModel 完成且 QA 確認後

---

### Stage 3 — UI + Navigation Commit

**包含**：
- `{Feature}Screen.kt`
- `{Feature}ShimmerLoading.kt`（若有）
- `RootNavScreen.kt`（路由新增）
- `RootNavGraph.kt`（composable 掛載）
- `strings.xml`（新增字串）

**Commit Message 範例**：
```
[feature] PXBOX-XXXXX 建立 {功能名} Screen 與路由整合
```

**觸發時機**：UI 完成、路由可跑通、QA 全部驗收 Approved 後

---

### Stage 4 — Docs / SA Commit（可選）

**包含**：
- `docs/{feature}/sa.md`
- `docs/{feature}/pjm.md`
- `docs/{feature}/test-case.md`
- `docs/{feature}/test-report.md`

**Commit Message 範例**：
```
[docs] PXBOX-XXXXX 建立 {功能名} SA 與測試文件
```

**觸發時機**：所有文件整理完成後一次提交

---

## 3. 授權請求格式

project-manager 向使用者請求 commit 授權時，固定格式：

```
── Commit 授權請求 ──────────────────────────
功能：{功能名稱}（{PXBOX-XXXXX}）
QA 結論：✅ Approved（test-report.md 已填寫）

Stage：{Stage 1 / 2 / 3}
包含檔案：
  + {新增檔案 1}
  + {新增檔案 2}
  ~ {修改檔案}

Commit Message 預覽：
  [feature] PXBOX-XXXXX {功能說明}

  - {條列異動 1}
  - {條列異動 2}

是否授權執行此 commit？（是 / 否）
────────────────────────────────────────────
```

---

## 4. Commit Message 完整格式

遵循 `git_commit_convention/SKILL.md`：

```
[{type}] {PXBOX-XXXXX} {主旨}

- {異動項目 1}
- {異動項目 2}
- {異動項目 3}

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

**允許的 type**：
`feature` | `fix` | `refactor` | `docs` | `test` | `chore` | `hotfix` | `release`

**主旨規範**：
- 動詞開頭：「建立」「新增」「修正」「重構」
- 不超過 50 字
- 描述「做了什麼」，不寫「改了哪個檔案」

---

## 5. git add 安全規則

執行 `git add` 時，**禁止使用** `git add .` 或 `git add -A`，改用精確路徑：

```bash
# 正確：指定檔案或目錄
git add app/src/main/java/com/gc/pxgo/compose/application/data/remote/{Feature}Service.kt
git add app/src/main/java/com/gc/pxgo/compose/application/data/model/{Feature}Response.kt

# 禁止
git add .             # 可能含入 .env、local.properties 等敏感檔案
git add -A            # 同上
```

**每次 add 前先執行 `git status`** 確認清單無敏感檔案。

---

## 6. 禁止事項

- **禁止在沒有 QA Approved 的情況下 commit**（緊急修正除外，需使用者明確說明）
- **禁止一次 commit 整個功能**（按 Stage 分段）
- **禁止使用 `git add .`**
- **禁止使用 `--no-verify`**（不跳過 validate-commit.py hook）
- **禁止修改 commit message 中的 Jira 單號格式**（必須與 branch 前綴一致）
- **禁止在 pjm.md 未更新完成前執行 commit**

---

## 7. Commit 完成後動作

每次 commit 成功後，project-manager 必須：

```
1. 更新 pjm.md — 在對應 Task 記錄 commit hash
2. 確認下一個 Task 或宣布功能完成
```
