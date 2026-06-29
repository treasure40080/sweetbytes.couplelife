# SA — 記帳流水頁面：新增記帳 / 月份篩選 / Room 持久化

> 系統分析文件（System Analysis）
> 描述「怎麼做」，是 android-developer 實作的直接依據。

---

## 基本資訊

| 項目 | 內容 |
|------|------|
| 單號 | COUPLELIFE-0002 |
| 功能名稱 | 記帳流水頁面 — 新增 / 編輯 / 刪除記帳 / 月份篩選 / Room 持久化 |
| 版本 | v1.1 |
| 作者 | Tim_Yen |
| 建立時間 | 2026-06-28 |
| 最後更新 | 2026-06-28 |

---

## 架構概覽

```
EntryScreen（Compose）
    ↓ collectAsStateWithLifecycle
EntryViewModel
    ↓ AddEntryUseCase / GetEntriesByMonthUseCase
EntryRepository（Interface）
    ↓ EntryRepositoryImpl
EntryDao（Room）
    ↓ Room Database（本地 SQLite）
```

---

## 依賴變更

### `gradle/libs.versions.toml` 新增版本

```toml
[versions]
hilt = "2.51.1"
room = "2.6.1"
datastorePrefs = "1.1.1"
lifecycleViewModel = "2.8.7"
ksp = "2.0.0-1.0.21"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePrefs" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewModel" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### `android/build.gradle.kts` 新增

```kotlin
plugins {
    // 現有 plugins 之後加入：
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

dependencies {
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
}
```

### 根目錄 `build.gradle.kts` 新增 plugin classpath

```kotlin
plugins {
    // 現有之後加入：
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

## 資料模型

### Entity

**檔案**：`data/local/entity/EntryEntity.kt`

```kotlin
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val type: EntryType,          // INCOME / EXPENSE
    val category: String,         // "吃飯" / "玩樂" / "薪水" / "租屋費" / 自定義
    val createdAt: Long           // System.currentTimeMillis()
)

enum class EntryType { INCOME, EXPENSE }
```

### TypeConverter

**檔案**：`data/local/converter/EntryTypeConverter.kt`

```kotlin
class EntryTypeConverter {
    @TypeConverter fun fromEntryType(type: EntryType): String = type.name
    @TypeConverter fun toEntryType(value: String): EntryType = EntryType.valueOf(value)
}
```

---

## DAO

**檔案**：`data/local/dao/EntryDao.kt`

```kotlin
@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("""
        SELECT * FROM entries
        WHERE strftime('%Y', createdAt / 1000, 'unixepoch') = :year
          AND strftime('%m', createdAt / 1000, 'unixepoch') = :month
        ORDER BY createdAt DESC
    """)
    fun getEntriesByMonth(year: String, month: String): Flow<List<EntryEntity>>
}
```

---

## Database

**檔案**：`data/local/AppDatabase.kt`

```kotlin
@Database(entities = [EntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(EntryTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}
```

---

## Repository

### 介面（domain 層）

**檔案**：`domain/repository/EntryRepository.kt`

```kotlin
interface EntryRepository {
    suspend fun addEntry(entry: EntryEntity)
    suspend fun updateEntry(entry: EntryEntity)
    suspend fun deleteEntry(entry: EntryEntity)
    fun getEntriesByMonth(year: String, month: String): Flow<List<EntryEntity>>
}
```

### 實作（data 層）

**檔案**：`data/repository/EntryRepositoryImpl.kt`

```kotlin
class EntryRepositoryImpl @Inject constructor(
    private val dao: EntryDao
) : EntryRepository {
    override suspend fun addEntry(entry: EntryEntity) = dao.insert(entry)
    override suspend fun updateEntry(entry: EntryEntity) = dao.update(entry)
    override suspend fun deleteEntry(entry: EntryEntity) = dao.delete(entry)
    override fun getEntriesByMonth(year: String, month: String) =
        dao.getEntriesByMonth(year, month)
}
```

---

## UseCase

### AddEntryUseCase

**檔案**：`domain/usecase/AddEntryUseCase.kt`

```kotlin
class AddEntryUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: EntryEntity) = repository.addEntry(entry)
}
```

### UpdateEntryUseCase（新增）

**檔案**：`domain/usecase/UpdateEntryUseCase.kt`

```kotlin
class UpdateEntryUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: EntryEntity) = repository.updateEntry(entry)
}
```

### DeleteEntryUseCase（新增）

**檔案**：`domain/usecase/DeleteEntryUseCase.kt`

```kotlin
class DeleteEntryUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: EntryEntity) = repository.deleteEntry(entry)
}
```

### GetEntriesByMonthUseCase

**檔案**：`domain/usecase/GetEntriesByMonthUseCase.kt`

```kotlin
class GetEntriesByMonthUseCase @Inject constructor(
    private val repository: EntryRepository
) {
    operator fun invoke(year: String, month: String) =
        repository.getEntriesByMonth(year, month)
}
```

---

## Hilt Module

**檔案**：`di/DatabaseModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "couplelife.db").build()

    @Provides
    fun provideEntryDao(db: AppDatabase): EntryDao = db.entryDao()

    @Provides @Singleton
    fun provideEntryRepository(dao: EntryDao): EntryRepository =
        EntryRepositoryImpl(dao)
}
```

---

## ViewModel

**檔案**：`ui/screen/entry/EntryViewModel.kt`

### UIState

```kotlin
data class EntryUiState(
    val entries: List<EntryEntity> = emptyList(),
    val selectedYear: Int = 當前年,
    val selectedMonth: Int = 當前月,
    val isLoading: Boolean = false
)
```

### Event（SharedFlow）

```kotlin
sealed class EntryEvent {
    object EntryAdded : EntryEvent()
    object EntryUpdated : EntryEvent()
    object EntryDeleted : EntryEvent()
    data class ShowError(val message: String) : EntryEvent()
}
```

### ViewModel 邏輯

```kotlin
@HiltViewModel
class EntryViewModel @Inject constructor(
    private val addEntryUseCase: AddEntryUseCase,
    private val getEntriesByMonthUseCase: GetEntriesByMonthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<EntryEvent>()
    val event: SharedFlow<EntryEvent> = _event.asSharedFlow()

    init { loadEntries() }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadEntries()
    }

    fun addEntry(amount: Int, type: EntryType, category: String) {
        viewModelScope.launch {
            addEntryUseCase(EntryEntity(amount = amount, type = type, category = category, createdAt = System.currentTimeMillis()))
            _event.emit(EntryEvent.EntryAdded)
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            val y = _uiState.value.selectedYear.toString()
            val m = _uiState.value.selectedMonth.toString().padStart(2, '0')
            getEntriesByMonthUseCase(y, m).collect { list ->
                _uiState.update { it.copy(entries = list) }
            }
        }
    }
}
```

---

## 元件設計

### EntryScreen

**檔案**：`ui/screen/entry/EntryScreen.kt`

**結構**：
```
EntryScreen（Scaffold）
├── TopAppBar：年月選擇器按鈕（點擊跳出 MonthPickerDialog）
├── LazyColumn（contentPadding = PaddingValues(top = innerPadding.top + 8.dp, bottom = innerPadding.bottom + 8.dp)）
│   ├── 有資料：EntryItem（點擊跳出 EditEntryDialog，右側垃圾桶 icon 觸發 DeleteConfirmDialog）
│   └── 無資料：EmptyState 提示文字
├── FAB（FloatingActionButton）：點擊顯示 AddEntryDialog
├── AddEntryDialog：新增記帳
├── EditEntryDialog：編輯記帳（帶入原始資料，無刪除按鈕）
└── DeleteConfirmDialog：刪除確認 Alert
```

**LazyColumn Scroll 規範**：
- 使用 `contentPadding` 而非 `Modifier.padding`，確保 item 可捲至 TopAppBar 下方與 BottomBar + FAB 上方
- `contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = innerPadding.calculateBottomPadding() + 80.dp, start = 16.dp, end = 16.dp)`
- `Modifier.fillMaxSize()` 不加額外 padding

### EntryItem 結構（v1.1）

```
Card（clickable → EditEntryDialog）
└── Row
    ├── Column（左側：分類名稱 + 時間）
    ├── Text（中間偏右：+/- 金額，收入綠/支出紅）
    └── IconButton（垃圾桶 icon，點擊 → DeleteConfirmDialog，不觸發 Card click）
```

### AddEntryDialog / EditEntryDialog

**AddEntryDialog 欄位**：
1. **收入 / 支出** 切換（TabRow）
2. **金額** 輸入框（KeyboardType.Number，不允許空白）
3. **項目** 選取（RadioButton 清單）：吃飯 / 玩樂 / 薪水 / 租屋費 / 其他
4. **自定義名稱** 輸入框（僅當選「其他」時顯示，必填）
5. **取消 / 確認** 按鈕

**EditEntryDialog 欄位**（帶入原始 EntryEntity 初始值，移除刪除按鈕）：
1. 同 AddEntryDialog 欄位（預填原始資料）
2. **取消 / 確認** 按鈕（無刪除按鈕）

### DeleteConfirmDialog

```
AlertDialog(
    title = "確認刪除",
    text = "確定要刪除這筆記帳嗎？",
    confirmButton = "確定"（紅色）→ viewModel.deleteEntry(entry),
    dismissButton = "取消" → 關閉 Dialog
)
```

### MonthPickerDialog

- 使用 `DatePickerDialog` 或自製 `AlertDialog` 包含 `year` / `month` 選擇器
- 確認後呼叫 `viewModel.onMonthSelected(year, month)`

---

## Application 入口

**檔案**：`CoupleLifeApplication.kt`（新增）

```kotlin
@HiltAndroidApp
class CoupleLifeApplication : Application()
```

**AndroidManifest.xml** 需加入：
```xml
android:name=".CoupleLifeApplication"
```

---

## 影響範圍

### 新增檔案

| 檔案路徑 | 說明 |
|----------|------|
| `CoupleLifeApplication.kt` | Hilt Application 入口 |
| `data/local/entity/EntryEntity.kt` | Room Entity + EntryType enum |
| `data/local/converter/EntryTypeConverter.kt` | TypeConverter |
| `data/local/dao/EntryDao.kt` | Room DAO |
| `data/local/AppDatabase.kt` | Room Database |
| `data/repository/EntryRepositoryImpl.kt` | Repository 實作 |
| `domain/repository/EntryRepository.kt` | Repository 介面 |
| `domain/usecase/AddEntryUseCase.kt` | 新增記帳 UseCase |
| `domain/usecase/GetEntriesByMonthUseCase.kt` | 查詢月份記帳 UseCase |
| `di/DatabaseModule.kt` | Hilt Module |
| `ui/screen/entry/EntryViewModel.kt` | ViewModel |
| `ui/screen/entry/EntryScreen.kt` | 主畫面 |
| `ui/screen/entry/AddEntryDialog.kt` | 新增記帳 Dialog |
| `ui/screen/entry/EditEntryDialog.kt` | 編輯記帳 Dialog（預填原始資料，含刪除入口） |
| `ui/screen/entry/MonthPickerDialog.kt` | 月份選擇 Dialog |

### 修改檔案

| 檔案路徑 | 修改內容 |
|----------|----------|
| `gradle/libs.versions.toml` | 新增 hilt / room / datastore / ksp 版本與 library |
| `android/build.gradle.kts` | 新增 hilt / ksp plugin + dependencies |
| 根目錄 `build.gradle.kts` | 新增 hilt / ksp plugin classpath |
| `AndroidManifest.xml` | 新增 `android:name=".CoupleLifeApplication"` |
| `ui/screen/MainScreens.kt` | 將 `EntryScreen()` 改為注入 ViewModel 的版本 |
| `MainActivity.kt` | 加入 `@AndroidEntryPoint` |

---

## TodoList（android-developer 實作順序）

- [x] T1：更新 `gradle/libs.versions.toml`（新增 hilt / room / datastore / ksp）
- [x] T2：更新根目錄 `build.gradle.kts`（plugin classpath）
- [x] T3：更新 `android/build.gradle.kts`（plugins + dependencies）
- [x] T4：建立 `CoupleLifeApplication.kt`，更新 `AndroidManifest.xml`
- [x] T5：加 `@AndroidEntryPoint` 到 `MainActivity.kt`
- [x] T6：建立 `EntryEntity.kt` + `EntryTypeConverter.kt`
- [x] T7：建立 `EntryDao.kt`（新增 @Update / @Delete）
- [x] T8：建立 `AppDatabase.kt`
- [x] T9：建立 `EntryRepositoryImpl.kt` + `EntryRepository.kt`（新增 update / delete）
- [x] T10：建立 `AddEntryUseCase.kt` + `GetEntriesByMonthUseCase.kt`
- [x] T11：建立 `DatabaseModule.kt`
- [x] T12：建立 `EntryViewModel.kt`
- [x] T13：建立 `EntryScreen.kt` + `AddEntryDialog.kt` + `MonthPickerDialog.kt`
- [x] T14：更新 `MainScreens.kt`，將 EntryScreen 接上 ViewModel
- [x] T15：Sync Gradle，確認編譯無誤
- [x] T16：EntryDao 補上 @Update / @Delete
- [x] T17：EntryRepository 介面 + 實作補上 updateEntry / deleteEntry
- [x] T18：建立 `UpdateEntryUseCase.kt` + `DeleteEntryUseCase.kt`
- [x] T19：EntryViewModel 補上 updateEntry / deleteEntry 方法 + EntryEvent
- [x] T20：建立 `EditEntryDialog.kt`（預填原始資料）
- [x] T21：EntryScreen 的 EntryItem 加上點擊 → 顯示 EditEntryDialog + DeleteConfirmDialog
- [x] T22：編譯確認無誤

---

## 驗收標準

- [x] 進入頁面預設顯示當月記帳清單
- [x] 點擊年月按鈕可選擇其他年/月，清單隨之更新
- [x] FAB 點擊跳出 AddEntryDialog
- [x] Dialog 可區分收入/支出，輸入金額，選取項目
- [x] 選「其他」時可輸入自定義名稱
- [x] 金額空白或其他名稱空白時不允許送出
- [x] 新增後資料立即出現在清單，依新增時間倒序
- [x] 重開 APP 後資料仍存在
- [x] 架構合規：UI 不直接呼叫 Repository，ViewModel 不持有 Context
