---
name: SkillIndex
description: |
  PXGo-Android 專案所有開發規範的總索引。
  AI Agent 在開始任何任務前必須先查閱此索引，找到對應 Skill 後依規範執行。
---

# Skill Index — PXGo-Android

> 任何開發任務開始前，**必須先查閱此索引**，找到對應的 Skill 後依規範執行。

---

## 🎯 任務決策矩陣

### 一般開發（單 Agent 模式）

| 任務類型 | 關鍵字 | Skill 路徑 | 強制程度 |
|----------|--------|-----------|----------|
| 新增 API / DTO / Repository | Service, Response, Repository, Retrofit | `.agents/skills/data_layer_mastery/SKILL.md` | **必須** |
| Hilt DI 注入 / Module | @Inject, @Module, @Binds, @Provides | `.agents/skills/dependency_injection_mastery/SKILL.md` | **必須** |
| 頁面導覽 / 路由定義 | NavGraph, NavScreen, navigate, popBackStack | `.agents/skills/navigation_patterns/SKILL.md` | **必須** |
| Compose UI / MVVM 架構 | Screen, ViewModel, UIState, StateFlow | `.agents/skills/coding_style_conventions/SKILL.md` | **必須** |
| UI 元件 / 主題 / 顏色 | Widget, Theme, Color, ActionBar, Dialog | `.agents/skills/ui_ux_enginerring/SKILL.md` | **必須** |
| Git Commit 訊息格式 | commit, PXBOX, message | `.agents/skills/git_commit_convention/SKILL.md` | **必須** |
| SA 文件撰寫結構 | SA, 系統分析, specs/sa | `.agents/skills/sa_document_convention/SKILL.md` | **必須** |
| 開發流程全覽（SSOT） | 開發流程, Task List, walkthrough | `.agents/skills/development_workflow/SKILL.md` | **必須** |
| AI 踩坑防範紀錄 | 禁止事項, 常見錯誤, AI rules | `.agents/skills/ai_coding_rules/AI_CODING_RULES.md` | **建議** |
| 常數 / 魔法數字管理 | Constants, KEY, TIMEOUT, 常數 | `.agents/skills/android_constants_convention/SKILL.md` | **建議** |

---

### 🤖 Agentic 開發（多 Agent 模式）

> 由 project-manager 統籌協調，派工給 android-developer 與 android-qa 時適用。

| 任務類型 | 適用 Agent | Skill 路徑 | 強制程度 |
|----------|-----------|-----------|----------|
| 功能流程控制 / pjm.md 維護 / 派工協議 | project-manager | `.agents/skills/agentic_control_flow/SKILL.md` | **必須** |
| SA 文件建立與品質門檻 | project-manager, android-developer | `.agents/skills/agentic_write_sa/SKILL.md` | **必須** |
| 靜態驗證 / 架構合規審計 | android-qa | `.agents/skills/agentic_audit_implementation/SKILL.md` | **必須** |
| 程式碼實作協議 / 完成回報格式 | android-developer | `.agents/skills/agentic_implementation/SKILL.md` | **必須** |
| 測試案例撰寫標準 | project-manager, android-qa | `.agents/skills/agentic_write_test_case/SKILL.md` | **必須** |
| Git Commit 分段策略 / 授權協議 | project-manager, android-developer | `.agents/skills/agentic_commit_stage/SKILL.md` | **必須** |

---

## 📂 全部 Skills 清單

```
.agents/skills/
│
├── android_skill_index/          ← 本文件（總索引）
│
├── ── 一般開發 Skills ──
│
├── data_layer_mastery/           API / DTO / Repository / Retrofit
├── dependency_injection_mastery/ Hilt DI / Module / Scope
├── navigation_patterns/          Compose Navigation / NavGraph / 路由
├── coding_style_conventions/     MVVM / Compose Screen / ViewModel
├── ui_ux_enginerring/            UI 元件 / 主題 / 顏色 / 間距
├── git_commit_convention/        Commit message 格式規範
├── sa_document_convention/       SA 文件章節結構規範
├── development_workflow/         10 步驟開發流程（SSOT）
├── ai_coding_rules/              AI 常見錯誤防範
├── android_constants_convention/ 常數命名與管理
│
└── ── Agentic 專用 Skills ──
│
├── agentic_control_flow/         project-manager 流程控制協議
├── agentic_write_sa/             SA 文件品質門檻與建立規範
├── agentic_audit_implementation/ android-qa 靜態驗證規範
├── agentic_implementation/       android-developer 實作協議
├── agentic_write_test_case/      測試案例撰寫標準
└── agentic_commit_stage/         分段 Commit 策略與授權協議
```

---

## 🚀 Agent 啟動宣言

> 身為 AI Agent，我承諾在執行任何寫檔動作前，會先查閱此索引，
> 確保本次改動符合 PXGo-Android 專案的既定規範。

---

*Skill Index 最後更新：2026-05-28*
