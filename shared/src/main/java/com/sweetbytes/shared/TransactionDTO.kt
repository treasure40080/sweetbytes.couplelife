package com.sweetbytes.shared

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDTO(
    val id: Int? = null,
    val amount: Double,
    val category: String,      // 飲食、娛樂等
    val description: String,
    val paidByUserId: Int,     // 誰先付的錢
    val splitType: String,     // 分攤模式：AA、MALE_FULL、FEMALE_FULL
    val date: String           // "2026-06-28"
)