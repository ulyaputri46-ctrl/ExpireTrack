package com.example.expiretrack.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.expiretrack.data.model.ProductEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _allProducts = MutableLiveData<List<ProductEntity>>()
    val allProducts: LiveData<List<ProductEntity>> = _allProducts

    init {
        startListeningToProducts()
    }

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun startListeningToProducts() {
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId).collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }
                
                val products = mutableListOf<ProductEntity>()
                for (doc in snapshot.documents) {
                    val product = doc.toObject(ProductEntity::class.java)
                    if (product != null) {
                        product.id = doc.id
                        products.add(product)
                    }
                }
                _allProducts.postValue(products)
            }
    }

    suspend fun insertProduct(product: ProductEntity): String {
        val userId = getUserId() ?: throw Exception("User not logged in")
        val docRef = firestore.collection("users").document(userId).collection("products").document()
        product.id = docRef.id
        docRef.set(product).await()
        return docRef.id
    }

    suspend fun updateProduct(product: ProductEntity) {
        val userId = getUserId() ?: throw Exception("User not logged in")
        firestore.collection("users").document(userId).collection("products")
            .document(product.id)
            .set(product).await()
    }

    suspend fun deleteProduct(product: ProductEntity) {
        val userId = getUserId() ?: throw Exception("User not logged in")
        firestore.collection("users").document(userId).collection("products")
            .document(product.id)
            .delete().await()
    }

    suspend fun deleteAllProducts() {
        val userId = getUserId() ?: throw Exception("User not logged in")
        val productsRef = firestore.collection("users").document(userId).collection("products")
        val snapshot = productsRef.get().await()
        for (doc in snapshot.documents) {
            productsRef.document(doc.id).delete().await()
        }
    }
}