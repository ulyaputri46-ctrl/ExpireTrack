package com.example.expiretrack.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.expiretrack.R
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.data.model.ProductStatus
import com.example.expiretrack.databinding.ActivityDetailProductBinding
import com.example.expiretrack.ui.viewmodel.ProductViewModel

class DetailProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    private lateinit var binding: ActivityDetailProductBinding
    private val viewModel: ProductViewModel by viewModels()

    private var currentProduct: ProductEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }

        viewModel.getProductById(productId).observe(this) { product ->
            if (product != null) {
                currentProduct = product
                bindProductDetails(product)
            }
        }

        binding.btnDetailEdit.setOnClickListener {
            currentProduct?.let { product ->
                val intent = Intent(this, AddProductActivity::class.java).apply {
                    putExtra(AddProductActivity.EXTRA_PRODUCT, product)
                }
                startActivity(intent)
            }
        }

        binding.btnDetailDelete.setOnClickListener {
            currentProduct?.let { product ->
                confirmDeleteProduct(product)
            }
        }
    }

    private fun bindProductDetails(product: ProductEntity) {
        binding.tvDetailName.text = product.name

        val catBrand = if (!product.brand.isNullOrBlank()) {
            "${product.category} • ${product.brand}"
        } else {
            product.category
        }
        binding.tvDetailCategoryBrand.text = catBrand

        binding.tvDetailPurchaseDate.text = product.purchaseDate
        binding.tvDetailExpiryDate.text = product.expiryDate
        binding.tvDetailQuantity.text = "${product.quantity} Buah / Unit"
        binding.tvDetailNotes.text = product.notes ?: "-"

        val status = product.getStatus()
        val daysText = product.getDaysRemainingText()

        binding.tvDetailStatusLabel.text = "Status: ${status.label.uppercase()}"
        binding.tvDetailDaysRemaining.text = daysText

        when (status) {
            ProductStatus.SAFE -> {
                binding.cardDetailStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_safe_bg))
                binding.cardDetailStatus.strokeColor = ContextCompat.getColor(this, R.color.status_safe)
                binding.tvDetailStatusLabel.setTextColor(ContextCompat.getColor(this, R.color.status_safe))
                binding.tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_safe))
                binding.imgDetailStatusIcon.setImageResource(R.drawable.ic_check_circle)
                binding.imgDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_safe))
            }
            ProductStatus.WARNING -> {
                binding.cardDetailStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_warning_bg))
                binding.cardDetailStatus.strokeColor = ContextCompat.getColor(this, R.color.status_warning)
                binding.tvDetailStatusLabel.setTextColor(ContextCompat.getColor(this, R.color.status_warning))
                binding.tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_warning))
                binding.imgDetailStatusIcon.setImageResource(R.drawable.ic_warning_circle)
                binding.imgDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_warning))
            }
            ProductStatus.TODAY -> {
                binding.cardDetailStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_today_bg))
                binding.cardDetailStatus.strokeColor = ContextCompat.getColor(this, R.color.status_today)
                binding.tvDetailStatusLabel.setTextColor(ContextCompat.getColor(this, R.color.status_today))
                binding.tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_today))
                binding.imgDetailStatusIcon.setImageResource(R.drawable.ic_clock)
                binding.imgDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_today))
            }
            ProductStatus.EXPIRED -> {
                binding.cardDetailStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_expired_bg))
                binding.cardDetailStatus.strokeColor = ContextCompat.getColor(this, R.color.status_expired)
                binding.tvDetailStatusLabel.setTextColor(ContextCompat.getColor(this, R.color.status_expired))
                binding.tvDetailDaysRemaining.setTextColor(ContextCompat.getColor(this, R.color.status_expired))
                binding.imgDetailStatusIcon.setImageResource(R.drawable.ic_alert_circle)
                binding.imgDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_expired))
            }
        }

        if (!product.imageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(product.imageUri)
                contentResolver.openInputStream(uri)?.close()
                binding.imgDetailProduct.setImageURI(uri)
                binding.imgDetailProduct.visibility = View.VISIBLE
                binding.imgDetailPlaceholder.visibility = View.GONE
            } catch (e: Exception) {
                binding.imgDetailProduct.visibility = View.GONE
                binding.imgDetailPlaceholder.visibility = View.VISIBLE
            }
        } else {
            binding.imgDetailProduct.visibility = View.GONE
            binding.imgDetailPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun confirmDeleteProduct(product: ProductEntity) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteProduct(product) {
                    Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}