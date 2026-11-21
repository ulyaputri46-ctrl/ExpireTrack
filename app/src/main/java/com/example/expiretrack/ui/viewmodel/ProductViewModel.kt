package com.example.expiretrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.data.model.ProductStatus
import com.example.expiretrack.data.model.ProductSummary
import com.example.expiretrack.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository()

    val allProducts: LiveData<List<ProductEntity>> = repository.allProducts

    val searchQuery = MutableLiveData<String>("")
    val selectedCategory = MutableLiveData<String>("Semua Kategori")
    val selectedStatus = MutableLiveData<String>("Semua Status")

    val filteredProducts = MediatorLiveData<List<ProductEntity>>()
    val productSummary = MediatorLiveData<ProductSummary>()
    val urgentProducts = MediatorLiveData<List<ProductEntity>>()

    init {
        filteredProducts.addSource(allProducts) { applyFilters() }
        filteredProducts.addSource(searchQuery) { applyFilters() }
        filteredProducts.addSource(selectedCategory) { applyFilters() }
        filteredProducts.addSource(selectedStatus) { applyFilters() }

        productSummary.addSource(allProducts) { products ->
            productSummary.value = calculateSummary(products)
        }

        urgentProducts.addSource(allProducts) { products ->
            urgentProducts.value = getUrgentProductsList(products)
        }
    }

    private fun applyFilters() {
        val list = allProducts.value ?: emptyList()
        val query = searchQuery.value?.trim()?.lowercase() ?: ""
        val category = selectedCategory.value ?: "Semua Kategori"
        val statusFilter = selectedStatus.value ?: "Semua Status"

        val filtered = list.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.name.lowercase().contains(query) ||
                    product.brand.lowercase().contains(query)

            val matchesCategory = category == "Semua Kategori" ||
                    product.category.equals(category, ignoreCase = true)

            val status = product.getStatus()
            val matchesStatus = when (statusFilter) {
                "Aman" -> status == ProductStatus.SAFE
                "Segera Kedaluwarsa" -> status == ProductStatus.WARNING
                "Hari Ini" -> status == ProductStatus.TODAY
                "Kedaluwarsa" -> status == ProductStatus.EXPIRED
                else -> true
            }

            matchesSearch && matchesCategory && matchesStatus
        }

        filteredProducts.value = filtered
    }

    private fun calculateSummary(products: List<ProductEntity>?): ProductSummary {
        if (products.isNullOrEmpty()) {
            return ProductSummary(0, 0, 0, 0)
        }

        var safe = 0
        var warning = 0
        var expired = 0

        for (p in products) {
            when (p.getStatus()) {
                ProductStatus.SAFE -> safe++
                ProductStatus.WARNING -> warning++
                ProductStatus.TODAY, ProductStatus.EXPIRED -> expired++
            }
        }

        return ProductSummary(
            totalCount = products.size,
            safeCount = safe,
            warningCount = warning,
            expiredCount = expired
        )
    }

    private fun getUrgentProductsList(products: List<ProductEntity>?): List<ProductEntity> {
        if (products.isNullOrEmpty()) return emptyList()
        return products.filter {
            val status = it.getStatus()
            status == ProductStatus.WARNING || status == ProductStatus.TODAY || status == ProductStatus.EXPIRED
        }.sortedBy { it.getDaysRemaining() }
    }

    fun getProductById(id: String): LiveData<ProductEntity?> {
        val result = MutableLiveData<ProductEntity?>()
        allProducts.observeForever { products ->
            result.value = products.find { it.id == id }
        }
        return result
    }

    fun insertProduct(product: ProductEntity, onComplete: ((String?) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = repository.insertProduct(product)
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(newId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(null) }
                }
            }
        }
    }

    fun updateProduct(product: ProductEntity, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateProduct(product)
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(false) }
                }
            }
        }
    }

    fun deleteProduct(product: ProductEntity, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteProduct(product)
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(false) }
                }
            }
        }
    }

    fun deleteAllProducts(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAllProducts()
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete?.let { callback ->
                    viewModelScope.launch(Dispatchers.Main) { callback(false) }
                }
            }
        }
    }
}