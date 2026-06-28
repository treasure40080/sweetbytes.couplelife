## 核心開發原則 (Core Development Principles)

在規劃實作內容時，必須嚴格遵守專案的核心架構規範：

1.  **嚴格遵守 MVVM 架構**：UI 層 (Activity/Fragment/Compose) 絕對禁止直接呼叫 Repository。所有資料處理與業務邏輯必須經由 ViewModel 與 UseCase。
2.  **標準化 API 處理**：對於 API 的回傳 `Result`，必須考量專案通用的處理方式。**Repository/UseCase 互動必須使用 `.fold()` 搭配 `ApiUtil.parseECResult(it)` 與 `it.getError().parErrorECResult()`**，並於實作說明中清楚定義。
    - **UseCase 範例**：
      ```kotlin
      suspend fun fetchData() = repository.apiCall().fold(
          onSuccess = { ApiUtil.parseECResult(it) },
          onFailure = { it.getError().parErrorECResult() }
      )
      ```
3.  **避免魔法數字與全寫路徑**：所有的 Key、Timeout、狀態碼必須定義為常數，且在程式碼範例中禁止使用全寫路徑 (Fully Qualified Names)。

---

## 🎯 第一部分：需求與架構設計

第一部分主要聚焦在產品需求理解以及系統的高階架構規劃。必須包含以下章節：

1. **需求背景 (Background)**：描述為什麼需要這個功能，以及解決了營運或業務上的什麼問題。
2. **目標 (Goals)**：列出本次開發預期達成的具體目標與限制。
3. **規格 (Specifications)**：詳細定義支援的規格、參數、限制前提（例如：檔案格式、尺寸、特定狀態下的行為）。
4. **系統架構 (System Architecture)**：高階的模組關聯與架構設計說明。
5. **流程圖 / 時序圖 (Flowchart & Sequence Diagram)**：
   - 必須使用純文字流程圖或 Mermaid 語法來繪製。
   - 清楚標示從使用者觸發到系統各層級 API 互動的先後時序與分支判斷。
6. **異常與極端異常處理 (Exception Handling / Edge Cases)**：表列出可能的例外狀況（如斷網、API 逾時、檔案損毀）以及對應的系統後備（Fallback）處理機制。

---

## 💻 第二部分：工程實作指引 (Implementation Details)

第二部分為實際交接給工程師開發的技術底層實作草案。**所有調整與新增的項目都必須附上具體的範例程式碼 (Code Snippets)**。必須包含以下章節：

1. **預置依賴套件確認 (Dependencies)**：如果有需要新增的 Gradle / 第三方套件（如 Coil, Lottie 等），必須在此列出相關宣告。
2. **Model & API 層實作**：
   - 定義 Request / Response 的 Data Class 或 Enum。
   - 定義 Repository 或 API Service 內的新增函數介面。
3. **UI Layer 與各種 Class 調整**：
   - 說明 `Activity`, `Fragment` 或 `Compose Screen` 畫面的轉場及生命週期調整。
   - 解釋如 Manager、UseCase 或是 Cache 等等純邏輯層的實作方式。
   - **⚠️ 架構鐵則 (Architectural Constraint)**：實作指引必須嚴格遵守 MVVM 架構，**嚴禁 UI 層直接與 Repository 建立溝通與呼叫**。所有資料請求、時間過濾與商業邏輯都必須封裝於 `ViewModel` 或 `UseCase` 中處理。
4. **ViewModel 各種調整**：說明狀態管理 (StateFlow/LiveData) 的變更以及承接 API 資料後的處理邏輯。
5. **程式碼範例 (Code Examples)**：上述的「每一個點」只要有牽涉到新增的 Class 或是修改的區塊，都必須給出對應的 Kotlin / Compose 範例程式碼。

---

## ✅ 交付追蹤 (Tasks & QA)

在文件最尾端，必須加入以下兩個列表以供收尾與驗收追蹤：

### TodoList (開發任務拆解清單)
- 規劃工程師實作這個功能時，應該按部就班完成的任務子項目檢查表。

### CheckList (測試與邊界檢查單)
- 針對第一部分的「異常處理」以及主要功能，列出 QA 或是工程師在開發完畢後，必須手動測試的極端狀況與對應的預期結果。
