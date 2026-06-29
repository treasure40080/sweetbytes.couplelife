package com.sweetbytes.couplelife.ui.screen.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sweetbytes.couplelife.data.local.entity.EntryType

private val PRESET_CATEGORIES = listOf("吃飯", "玩樂", "薪水", "租屋費", "其他")

@Composable
fun AddEntryDialog(
    onConfirm: (amount: Int, type: EntryType, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTypeIndex by remember { mutableStateOf(1) } // 0=收入, 1=支出
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PRESET_CATEGORIES[0]) }
    var customName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val entryType = if (selectedTypeIndex == 0) EntryType.INCOME else EntryType.EXPENSE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增記帳") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 收入 / 支出 切換
                TabRow(selectedTabIndex = selectedTypeIndex) {
                    Tab(
                        selected = selectedTypeIndex == 0,
                        onClick = { selectedTypeIndex = 0 },
                        text = { Text("收入") }
                    )
                    Tab(
                        selected = selectedTypeIndex == 1,
                        onClick = { selectedTypeIndex = 1 },
                        text = { Text("支出") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 金額輸入
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("金額") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && amountText.isBlank(),
                    supportingText = {
                        if (showError && amountText.isBlank()) Text("金額不能為空")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 項目選取
                Text("選取項目")
                Column(modifier = Modifier.selectableGroup()) {
                    PRESET_CATEGORIES.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = null
                            )
                            Text(category)
                        }
                    }
                }

                // 自定義名稱（僅選「其他」時顯示）
                if (selectedCategory == "其他") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("自定義名稱") },
                        isError = showError && customName.isBlank(),
                        supportingText = {
                            if (showError && customName.isBlank()) Text("名稱不能為空")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValid = amountText.isNotBlank()
                    val categoryValid = selectedCategory != "其他" || customName.isNotBlank()
                    if (!amountValid || !categoryValid) {
                        showError = true
                        return@TextButton
                    }
                    val finalCategory = if (selectedCategory == "其他") customName else selectedCategory
                    onConfirm(amountText.toInt(), entryType, finalCategory)
                }
            ) {
                Text("確認")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
