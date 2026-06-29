# SA — 統計頁面：圓餅圖（收支比例 / 分類佔比）

> 系統分析文件（System Analysis）
> 描述「怎麼做」，是 android-developer 實作的直接依據。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0003 |
| 功能名稱 | 統計頁面 — 圓餅圖（收支比例 / 分類佔比） |
| 版本 | v1.0 |
| 作者 | Tim_Yen |
| 建立時間 | 2026-06-28 |
| 最後更新 | 2026-06-28 |

---

## 架構概覽

此功能為純 UI 展示，無新增 API 或資料寫入，資料來源複用既有 Repository。

```
ChartScreen（Compose + AndroidView）
    ↓ collectAsStateWithLifecycle
ChartViewModel
    ↓ GetEntriesByMonthUseCase（複用）
EntryRepository → EntryDao → Room
```

---

## 依賴變更

### `android/build.gradle.kts` 新增

```kotlin
// MPAndroidChart（透過 JitPack）
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

### 根目錄 `settings.gradle.kts` 新增 JitPack Repository

```kotlin
dependencyResolutionManagement {
    repositories {
        // 現有之後加入：
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## 資料模型（ViewModel 內部計算，不寫入 Room）

### ChartUiState

```kotlin
data class ChartUiState(
    val selectedYear: Int = 當前年,
    val selectedMonth: Int = 當前月,
    val incomeExpenseData: List<PieSlice> = emptyList(),  // 收入 vs 支出
    val categoryData: List<PieSlice> = emptyList(),       // 各分類佔比
    val isEmpty: Boolean = true
)

data class PieSlice(
    val label: String,
    val amount: Int,
    val percentage: Float,
    val color: Int  // Android Color Int
)
```

---

## 顏色定義

**收支圖（2 色）：**
| 項目 | 顏色 |
|------|------|
| 收入 | `#4CAF50`（綠） |
| 支出 | `#F44336`（紅） |

**分類圖（最多 N 色，循環使用）：**
| 分類 | 顏色 |
|------|------|
| 吃飯 | `#FF9800`（橘） |
| 玩樂 | `#9C27B0`（紫） |
| 薪水 | `#2196F3`（藍） |
| 租屋費 | `#009688`（青） |
| 其他/自定義 | `#607D8B`（灰藍），多個自定義各取色盤下一色 |

色盤（自定義超過 1 個時循環取用）：
```kotlin
val CATEGORY_COLORS = listOf(
    0xFFFF9800, 0xFF9C27B0, 0xFF2196F3, 0xFF009688,
    0xFF607D8B, 0xFFE91E63, 0xFF00BCD4, 0xFF8BC34A
)
```

---

## ChartViewModel

**檔案**：`ui/screen/chart/ChartViewModel.kt`

```kotlin
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getEntriesByMonthUseCase: GetEntriesByMonthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init { loadChart() }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadChart()
    }

    private fun loadChart() {
        viewModelScope.launch {
            val y = _uiState.value.selectedYear.toString()
            val m = _uiState.value.selectedMonth.toString().padStart(2, '0')
            getEntriesByMonthUseCase(y, m).collect { entries ->
                _uiState.update { it.copy(
                    incomeExpenseData = calcIncomeExpense(entries),
                    categoryData = calcCategory(entries),
                    isEmpty = entries.isEmpty()
                )}
            }
        }
    }

    private fun calcIncomeExpense(entries: List<EntryEntity>): List<PieSlice> { ... }
    private fun calcCategory(entries: List<EntryEntity>): List<PieSlice> { ... }
}
```

---

## 元件設計

### ChartScreen

**檔案**：`ui/screen/chart/ChartScreen.kt`

**結構**：
```
ChartScreen（Scaffold）
├── TopAppBar：年月選擇器按鈕（點擊跳出 MonthPickerDialog）
└── Column（可捲動）
    ├── 無資料：EmptyState 提示文字
    └── 有資料：
        ├── Section「收入 vs 支出」
        │   ├── PieChartView（MPAndroidChart AndroidView）
        │   └── LegendRow × 2（收入 / 支出）
        └── Section「各分類佔比」
            ├── PieChartView（MPAndroidChart AndroidView）
            └── LegendRow × N（各分類）
```

### PieChartView（AndroidView 包裝）

**檔案**：`ui/screen/chart/PieChartView.kt`

```kotlin
@Composable
fun PieChartView(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 40f
                setUsePercentValues(true)
                legend.isEnabled = false  // 自製圖例
                setEntryLabelColor(Color.WHITE)
            }
        },
        update = { chart ->
            val entries = slices.map { PieEntry(it.percentage, it.label) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = slices.map { it.color }
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}
```

### LegendRow（自製圖例）

```kotlin
@Composable
fun LegendRow(slice: PieSlice) {
    Row（顏色方塊 + 分類名稱 + 百分比 + 金額）
}
```

### MonthPickerDialog

複用 `ui/screen/entry/MonthPickerDialog.kt`，不需重新建立。

---

## 影響範圍

### 新增檔案

| 檔案路徑 | 說明 |
|----------|------|
| `ui/screen/chart/ChartViewModel.kt` | ViewModel + 圖表資料計算 |
| `ui/screen/chart/ChartScreen.kt` | 主畫面 |
| `ui/screen/chart/PieChartView.kt` | MPAndroidChart AndroidView 包裝 |

### 修改檔案

| 檔案路徑 | 修改內容 |
|----------|----------|
| `android/build.gradle.kts` | 新增 MPAndroidChart 依賴 |
| `settings.gradle.kts` | 新增 JitPack repository |
| `ui/screen/MainScreens.kt` | 將 ChartScreen() placeholder 替換為新實作 |

---

## TodoList（android-developer 實作順序）

- [x] T1：`settings.gradle.kts` 新增 JitPack repository
- [x] T2：`android/build.gradle.kts` 新增 MPAndroidChart 依賴
- [x] T3：建立 `ChartViewModel.kt`（含 ChartUiState / PieSlice / 計算邏輯）
- [x] T4：建立 `PieChartView.kt`（AndroidView 包裝 MPAndroidChart PieChart）
- [x] T5：建立 `ChartScreen.kt`（TopAppBar + 兩個圓餅圖 + 圖例 + 空狀態）
- [x] T6：更新 `MainScreens.kt`，替換 ChartScreen placeholder
- [x] T7：Sync Gradle，確認編譯無誤
- [x] T8：ChartUiState 拆分 expenseCategoryData / incomeCategoryData / categoryFilter
- [x] T9：ChartScreen 各分類佔比加入支出 / 收入 TabRow

---

## 驗收標準

- [x] 進入頁面預設顯示當月圓餅圖
- [x] 可切換年/月，圖表隨之更新
- [x] 收入 vs 支出圓餅圖顏色正確（綠/紅）
- [x] 各分類圓餅圖每項顏色不同，支出 / 收入 TabRow 切換
- [x] 圖例顯示顏色 + 名稱 + 百分比
- [x] 無資料時顯示空狀態提示，不顯示圖表
- [x] 架構合規：UI 不直接呼叫 Repository，ViewModel 不持有 Context
