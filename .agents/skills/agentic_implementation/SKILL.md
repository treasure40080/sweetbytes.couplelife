---
name: AgenticImplementation
description: |
  android-developer Agent 的實作完整規範。
  定義「讀取 SA → 確認 Task List → 步進式實作 → 產出對照表」的完整協議。
  適用對象：android-developer
---

# Agentic Implementation — 實作協議

> 適用對象：**android-developer Agent**
> 核心原則：SA 是施工藍圖，一切以 SA 為準；不在 SA 裡的不做，SA 不清楚的先問。

---

## 1. 實作啟動協議

收到 project-manager 派工後，**按順序執行**：

```
Step 1. 讀取指定 SA 文件（docs/{feature}/sa.md）
        → 確認本次要實作的 Task 範圍
        → 標記 SA 中尚未實作的部分

Step 2. 讀取 .agents/skills/android_skill_index/SKILL.md
        → 找到本次 Task 對應的 Skill（API/UI/DI/導覽等）

Step 3. 依 Task 類型讀取對應 Skill：
        → data_layer_mastery（API/Model/Repository）
        → dependency_injection_mastery（Hilt Module）
        → coding_style_conventions（ViewModel/UseCase）
        → ui_ux_enginerring（Compose UI）
        → navigation_patterns（NavGraph）

Step 4. 列出實作 Task List，回報給 project-manager：
        ─────────────────────────────────
        📋 實作計畫：
        T4a: 建立 {Feature}Response.kt
        T4b: 建立 {Feature}Service.kt
        T4c: 建立 {Feature}Repository 介面
        T4d: 建立 {Feature}RepositoryImpl.kt
        T4e: 建立 DI Module
        ...
        ─────────────────────────────────
        確認後開始實作。

Step 5. 步進式實作（一次一個子 Task）
Step 6. 完成後產出實作對照表
```

---

## 2. 實作順序（由下往上）

**嚴格按照此順序**，確保每一層依賴的下一層已完成：

```
1. Data Layer
   ├── {Feature}Response.kt（DTO）
   ├── {Feature}Service.kt（Retrofit 介面）
   ├── {Feature}Repository.kt（domain 層介面）
   └── {Feature}RepositoryImpl.kt（data 層實作）

2. DI Layer
   └── {Feature}Module.kt（Hilt bindings）

3. Domain Layer
   └── {Feature}UseCase.kt（業務邏輯）

4. UI Layer（ViewModel 先於 Screen）
   ├── {Feature}ViewModel.kt（狀態機）
   ├── {Feature}Vo.kt（UI 資料模型，若 SA 有定義）
   └── {Feature}Screen.kt（Composable）

5. Navigation
   ├── RootNavScreen.kt（新增路由物件）
   └── RootNavGraph.kt（新增 composable 區塊）

6. Resources
   └── strings.xml（新增字串資源）
```

---

## 3. 每個層的實作規範

### 3.1 Data Layer

```kotlin
// Service：只定義介面，不含業務邏輯
interface {Feature}Service {
    @POST("api/v1/{endpoint}")
    suspend fun {methodName}(
        @Body request: {Feature}Request
    ): Result<{Feature}Response>
}

// Response：每個 field 必須有 @SerializedName
data class {Feature}Response(
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("nullable_field") val nullableField: String? = null
)

// Repository Impl：使用 fold 處理 Result
class {Feature}RepositoryImpl @Inject constructor(
    private val service: {Feature}Service
) : {Feature}Repository {
    override suspend fun fetchData(): Result<{Feature}Response> =
        service.{methodName}(request).fold(
            onSuccess = { ApiUtil.parseECResult(it) },
            onFailure = { it.getError().parErrorECResult() }
        )
}
```

### 3.2 Domain Layer（UseCase）

```kotlin
class {Feature}UseCase @Inject constructor(
    private val repository: {Feature}Repository
) {
    suspend fun execute(param: String): Result<{Feature}Vo> =
        repository.fetchData().fold(
            onSuccess = { Result.success(it.toVo()) },
            onFailure = { Result.failure(it) }
        )
}

// VO 轉換（extension function）
fun {Feature}Response.toVo() = {Feature}Vo(
    displayText = this.fieldName,
    // ...
)
```

### 3.3 ViewModel

```kotlin
@HiltViewModel
class {Feature}ViewModel @Inject constructor(
    private val useCase: {Feature}UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<{Feature}UIState>({Feature}UIState.Loading)
    val uiState: StateFlow<{Feature}UIState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<{Feature}Event>()
    val eventFlow: SharedFlow<{Feature}Event> = _eventFlow.asSharedFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = {Feature}UIState.Loading
            useCase.execute().fold(
                onSuccess = { _uiState.value = {Feature}UIState.Success(it) },
                onFailure = { _uiState.value = {Feature}UIState.Failure(it.message ?: "") }
            )
        }
    }

    fun onRetry() = loadData()
}
```

### 3.4 Screen（Compose）

```kotlin
@Composable
fun {Feature}Screen(
    navController: NavController,
    viewModel: {Feature}ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    // 初始載入（只觸發一次）
    LaunchedEffect(isInitialized) {
        if (!isInitialized) {
            viewModel.loadData()
            isInitialized = true
        }
    }

    // 導覽事件
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is {Feature}Event.NavigateTo -> navController.navigate(event.route)
            }
        }
    }

    when (uiState) {
        is {Feature}UIState.Loading -> {Feature}ShimmerLoading()
        is {Feature}UIState.Success -> {Feature}Content(
            data = (uiState as {Feature}UIState.Success).data,
            onAction = { viewModel.onAction() }
        )
        is {Feature}UIState.Failure -> ErrorScreen(
            message = (uiState as {Feature}UIState.Failure).errorMessage,
            onRetry = { viewModel.onRetry() }
        )
    }
}
```

---

## 4. 需要暫停詢問的情況

遇到以下情況，**立刻停止實作，回報給 project-manager**：

| 情況 | 回報內容 |
|------|----------|
| SA 缺少某個 field 的型別 | 「SA 中 {field} 的型別未定義，請確認」 |
| SA 的 API endpoint 與現有 Service 有命名衝突 | 具體列出衝突點 |
| 不確定應使用哪條 Service 鏈（EC/PxGo/PxPay） | 詢問 project-manager |
| 發現已有同功能的舊實作 | 列出舊檔案，詢問是重構還是新增 |
| strings.xml 有重複的 key | 詢問是否複用或新建 |

---

## 5. 完成回報格式

實作完成後，回報給 project-manager 的格式：

```
── 實作完成報告 ─────────────────────────────
Task：T{X} ~ T{Y}
功能：{功能名稱}（{PXBOX-XXXXX}）

【新增檔案】
- data/remote/{Feature}Service.kt
- data/model/{Feature}Response.kt
- ...

【修改檔案】
- navigation/RootNavScreen.kt（新增 {Feature} 路由）
- di/{Feature}Module.kt（新增 bindings）
- res/values/strings.xml（新增 N 條字串）

【SA 對照確認】
- [ ] API endpoint：✅ 符合
- [ ] Request/Response model：✅ 符合
- [ ] UIState 定義：✅ 符合
- [ ] 路由定義：✅ 符合

【架構合規自查】
- [ ] UI 不直接呼叫 Repository：✅
- [ ] ViewModel 無 Context/NavController：✅
- [ ] 無自訂 Hilt Component：✅

【待確認事項】
- （無 / 列出需要確認的項目）

git commit 等待 project-manager 授權。
─────────────────────────────────────────────
```

---

## 6. 禁止事項

- **禁止在 SA 不完整時開始實作**（缺欄位、缺型別、缺路由定義 = 先問）
- **禁止自行修改 docs/ 文件**（SA/pjm.md 由 project-manager 維護）
- **禁止擅自執行 git commit**（等 project-manager 授權）
- **禁止在未獲確認前操作 app/src/ 以外的目錄**
- **禁止 hardcode 使用者可見的字串**（一律 strings.xml + stringResource）
- **禁止使用 XML UI**（除 shortvideo/ 舊模組，新功能全用 Compose）
