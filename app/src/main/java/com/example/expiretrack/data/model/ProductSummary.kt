package com.example.expiretrack.data.model

data class ProductSummary(
    val totalCount: Int = 0,
    val safeCount: Int = 0,
    val warningCount: Int = 0,
    val expiredCount: Int = 0
)