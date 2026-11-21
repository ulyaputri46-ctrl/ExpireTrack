package com.example.expiretrack.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expiretrack.R
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.data.model.ProductStatus
import com.example.expiretrack.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val onItemClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            val context = binding.root.context
            val status = product.getStatus()
            val daysText = product.getDaysRemainingText()

            binding.tvNotifTitle.text = product.name
            binding.tvNotifMessage.text = "Status: ${status.label} • $daysText (Kedaluwarsa: ${product.expiryDate})"
            binding.tvNotifTime.text = "Kategori: ${product.category}"

            when (status) {
                ProductStatus.WARNING -> {
                    binding.imgNotifIcon.setImageResource(R.drawable.ic_warning_circle)
                    binding.imgNotifIcon.setBackgroundResource(R.drawable.bg_badge_warning)
                    binding.imgNotifIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_warning))
                }
                ProductStatus.TODAY -> {
                    binding.imgNotifIcon.setImageResource(R.drawable.ic_clock)
                    binding.imgNotifIcon.setBackgroundResource(R.drawable.bg_badge_today)
                    binding.imgNotifIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_today))
                }
                ProductStatus.EXPIRED -> {
                    binding.imgNotifIcon.setImageResource(R.drawable.ic_alert_circle)
                    binding.imgNotifIcon.setBackgroundResource(R.drawable.bg_badge_expired)
                    binding.imgNotifIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_expired))
                }
                else -> {
                    binding.imgNotifIcon.setImageResource(R.drawable.ic_check_circle)
                    binding.imgNotifIcon.setBackgroundResource(R.drawable.bg_badge_safe)
                    binding.imgNotifIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_safe))
                }
            }

            binding.cardNotification.setOnClickListener { onItemClick(product) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}