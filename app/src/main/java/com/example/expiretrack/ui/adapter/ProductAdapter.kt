package com.example.expiretrack.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expiretrack.R
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.data.model.ProductStatus
import com.example.expiretrack.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (ProductEntity) -> Unit,
    private val onEditClick: (ProductEntity) -> Unit,
    private val onDeleteClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            val context = binding.root.context

            binding.tvProductName.text = product.name

            val categoryBrandText = if (!product.brand.isNullOrBlank()) {
                "${product.category} • ${product.brand}"
            } else {
                product.category
            }
            binding.tvCategoryBrand.text = categoryBrandText

            binding.tvExpiryDate.text = "Kedaluwarsa: ${product.expiryDate}"

            val status = product.getStatus()
            val daysText = product.getDaysRemainingText()
            binding.tvStatusBadge.text = "${status.label} ($daysText)"

            when (status) {
                ProductStatus.SAFE -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_safe)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_safe))
                }
                ProductStatus.WARNING -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_warning))
                }
                ProductStatus.TODAY -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_today)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_today))
                }
                ProductStatus.EXPIRED -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_expired)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_expired))
                }
            }

            loadProductImage(product.imageUri, product.category)

            binding.cardProduct.setOnClickListener { onItemClick(product) }
            binding.btnEditQuick.setOnClickListener { onEditClick(product) }
            binding.btnDeleteQuick.setOnClickListener { onDeleteClick(product) }
        }

        private fun loadProductImage(imageUriStr: String?, category: String) {
            if (!imageUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(imageUriStr)
                    binding.imgProduct.setImageURI(uri)
                    binding.imgProduct.setPadding(0, 0, 0, 0)
                    binding.imgProduct.clearColorFilter()
                    return
                } catch (e: Exception) {
                    // Safe fallback if content URI permission expired or invalid
                }
            }
            setDefaultCategoryIcon(category)
        }

        private fun setDefaultCategoryIcon(category: String) {
            val paddingInDp = (12 * binding.root.resources.displayMetrics.density).toInt()
            binding.imgProduct.setPadding(paddingInDp, paddingInDp, paddingInDp, paddingInDp)
            
            // Set the tint color for icons
            binding.imgProduct.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.sage_primary))
            
            val iconRes = when (category.lowercase()) {
                "makanan" -> R.drawable.ic_box
                "minuman" -> R.drawable.ic_box
                "obat & kesehatan" -> R.drawable.ic_warning_circle
                "skincare & kosmetik" -> R.drawable.ic_tag
                else -> R.drawable.ic_box
            }
            binding.imgProduct.setImageResource(iconRes)
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}