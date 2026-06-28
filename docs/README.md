# COUPLE-LIFE — 專案文件索引

> 唯一真相來源（SSOT）。所有功能的規格、SA、測試報告都在這裡。

---

## 📂 目錄結構

```
docs/
├── README.md           ← 本文件（索引）
├── _template/          ← Agentic 文件模板（複製使用，不直接修改）
│   ├── pjm.md          # 任務書模板
│   ├── prd.md          # 產品需求模板
│   ├── sa.md           # 系統分析模板
│   ├── test-case.md    # 測試案例模板
│   ├── test-report.md  # 測試報告模板
│   └── pjm-spawn-log.md# Agent 派工日誌模板
│
├── {feature}/          ← 每個 Agentic 功能一個子目錄（COUPLELIFE-XXXXX 命名）
│   ├── pjm.md          # 任務書（唯一進度真相）
│   ├── sa.md           # 系統分析
│   ├── test-case.md    # 測試案例
│   ├── test-report.md  # 測試報告
│   └── pjm-spawn-log.md# Agent 派工紀錄
│

```

---

## 🚀 新功能開發：Agentic 流程

### 啟動新功能

```bash
# 1. 從模板複製
cp -r docs/_template docs/COUPLELIFE-XXXXX

# 2. 啟動 project-manager
# 在對話中輸入：
# 你是 project-manager agent，請讀取 docs/COUPLELIFE-XXXXX/pjm.md
# 確認目前進度，繼續下一個 Task。
```

### 功能目錄命名規則

- 使用命名：`docs/COUPLELIFE-XXXXX/`
- 若有描述性前綴：`docs/COUPLELIFE-XXXXX-feature-name/`

---

## 📋 Agentic 功能列表（進行中 / 已完成）

| 單號 | 功能名稱 | 狀態 | pjm.md |
|-----------|---------|------|--------|
| COUPLELIFE-XXXXX | （範例：待填入） | 🔵 進行中 | [pjm.md](./PXBOX-XXXXX/pjm.md) |

> 每次開啟新功能後，在此表格新增一行。

---

## 🤖 Harness Engineer 文件（docs/）

| 單號 | 功能名稱 | 文件連結 |
|-----------|---------|---------|

---

## 🔧 Harness 文件

| 說明 | 路徑 |
|------|------|
| Agent 定義 | `.claude/agents/` |
| Hooks | `.claude/hooks/` |
| 權限設定 | `.claude/settings.json` |
| Agent 活動統計 | `python3 .claude/hooks/stats-report.py` |
