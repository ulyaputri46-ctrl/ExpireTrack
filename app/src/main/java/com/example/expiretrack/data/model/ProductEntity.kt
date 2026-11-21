package com.example.expiretrack.data.model

import java.io.Serializable

data class ProductEntity(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    val brand: String = "",
    val purchaseDate: String = "",
    val expiryDate: String = "",
    val quantity: Int = 1,
    val notes: String? = null,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {
    fun getStatus(): ProductStatus {
        return ProductStatus.getStatus(expiryDate)
    }

    fun getDaysRemaining(): Long {
        return ProductStatus.calculateDaysRemaining(expiryDate)
    }

    fun getDaysRemainingText(): String {
        return ProductStatus.getDaysRemainingText(getDaysRemaining())
    }
}