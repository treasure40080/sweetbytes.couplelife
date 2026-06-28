---
name: AgenticControlFlow
description: |
  project-manager Agent 的完整流程控制規範。
  定義「如何讀取 pjm.md → 拆解任務 → 派工 → 驗收 → 更新狀態」的完整協議。
  適用對象：project-manager
---

# Agentic Control Flow — 流程控制規範

> 適用對象：**project-manager Agent**
> 核心原則：pjm.md 是唯一狀態真相，一切決策從 pjm.md 出發，一切結果寫回 pjm.md。

---

## 1. Session 啟動協議

每次對話開始，project-manager **必須按順序執行**：

```
Step 1. 讀取 docs/{feature}/pjm.md
        → 找到當前未完成的 Task（狀態 [ ] 或 [→] 或 [!]）
        → 確認最後一次派工結果

Step 2. 讀取 .agents/skills/android_skill_index/SKILL.md
        → 確認本次 Task 需要哪些 Skill

Step 3. 向使用者回報：
        ─────────────────────────────────
        📋 功能：{功能名稱}（{PXBOX-XXXXX}）
        📌 目前進度：{已完成 X / 共 Y 個 Task}
        ⏳ 下一步：{Task 編號與描述}
        ─────────────────────────────────
        請問要繼續這個 Task，還是有新的指示？
```

---

## 2. Task 狀態機

```
[ ] 待做
 ↓ 開始派工
[→] 進行中
 ↓ 子代理人完成回報
[x] 完成（需通過驗收）
 ↓ 如遇阻塞
[!] 阻塞（記錄原因，等待解除）
```

**狀態更新時機**：
- 派工出去 → 立刻將 Task 改為 `[→]`，更新 pjm.md
- 子代理人回報完成 → 驗收通過後改為 `[x]`，更新 pjm.md
- 遭遇阻塞 → 改為 `[!]`，寫明阻塞原因

---

## 3. 派工協議（Spawn Protocol）

### 3.1 派工前準備清單

在呼叫子代理人前，必須確認：

- [ ] SA 文件已存在（`docs/{feature}/sa.md`）
- [ ] Test Case 已存在（派給 android-qa 時）
- [ ] pjm.md Task 狀態已更新為 `[→]`
- [ ] 派工指令包含所有必要上下文（見 3.2）

### 3.2 派工指令模板

**派給 android-developer**：

```
你是 android-developer agent。

【功能】{功能名稱}（{PXBOX-XXXXX}）

【必讀文件】
- SA：docs/{feature}/sa.md
- Skill Index：.agents/skills/android_skill_index/SKILL.md

【本次 Tasks】
- {T編號}: {描述}
- {T編號}: {描述}

【驗收標準】
- {驗收條件 1}
- {驗收條件 2}

【完成後請提供】
1. 實作對照表（每個 Task 對應的修改檔案清單）
2. 新增/修改的主要程式碼片段
3. 是否有任何規格疑點需要確認

【注意事項】
- 只操作 app/src/ 目錄
- git commit 需等 project-manager 授權，請勿自行提交
```

**派給 android-qa**：

```
你是 android-qa agent。

【功能】{功能名稱}（{PXBOX-XXXXX}）

【必讀文件】
- 測試計畫：docs/{feature}/test-case.md
- SA：docs/{feature}/sa.md
- pjm.md：docs/{feature}/pjm.md

【驗收任務】
對以下已完成 Tasks 進行靜態驗證：
- {T編號}: {描述}

【請填寫】
docs/{feature}/test-report.md

【回報格式】
整體結論：Approved / Rejected
失敗項目：{列表或 N/A}
建議修正：{具體建議或 N/A}
```

### 3.3 派工記錄

每次派工後，在 `docs/{feature}/pjm.md` 的派工記錄表新增一筆：

```markdown
### SPAWN-{序號}
- 時間：YYYY-MM-DD HH:mm
- 呼叫：android-developer / android-qa
- Task：T{X}, T{Y}
- 狀態：⏳ 執行中
```

---

## 4. 驗收協議

### 4.1 android-developer 完成時

project-manager 收到實作對照表後：

```
1. 比對 SA 文件：每個新增/修改的類別是否與 SA 規格一致
2. 確認架構合規：
   - UI 不直接呼叫 Repository？
   - ViewModel 無持有 Context/NavController？
3. 決策：
   - 全部通過 → 派工 android-qa
   - 有問題 → 列出修正清單，重新派工 android-developer
```

### 4.2 android-qa 完成時

收到 test-report.md 後：

```
1. 讀取 docs/{feature}/test-report.md
2. 確認結論：
   - Approved → 可進行 git commit（需使用者授權）
   - Rejected → 整理問題清單，重新派工 android-developer 修正
3. 更新 pjm.md 驗收記錄
```

---

## 5. 問題升級協議

遇到以下情況，**必須暫停並詢問使用者**：

| 情況 | 處理方式 |
|------|----------|
| 規格文件不存在或不完整 | 詢問使用者提供規格或 Jira 單號 |
| SA 與實際 API 有衝突 | 彙整差異，詢問以哪個為準 |
| android-developer 多次失敗（>2 次） | 停止派工，向使用者說明問題 |
| 需求有歧義 | 整理疑點清單，一次全部詢問 |

---

## 6. Feature 結束協議

所有 Task 完成、QA 通過後：

```
1. 更新 pjm.md 目前狀態為 ✅ 完成
2. 填寫驗收記錄（時間、結論）
3. 詢問使用者：是否執行 git commit？
4. 取得授權後，提供符合 git_commit_convention 的 commit 指令
```

---

## 7. 禁止事項

- **禁止跳過 pjm.md 更新**（每個狀態變更都要同步）
- **禁止在 SA 未完成前派工 android-developer**
- **禁止在未獲 QA 通過前授權 git commit**
- **禁止自行撰寫 app/src/ 程式碼**（這是 android-developer 的職責）
- **禁止自行猜測規格**（有疑點先問使用者）
