package com.example.expiretrack.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.databinding.FragmentNotificationBinding
import com.example.expiretrack.notification.NotificationHelper
import com.example.expiretrack.ui.activity.DetailProductActivity
import com.example.expiretrack.ui.adapter.NotificationAdapter
import com.example.expiretrack.ui.viewmodel.ProductViewModel

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.btnTestNotif.setOnClickListener {
            NotificationHelper.sendTestNotification(requireContext())
            Toast.makeText(requireContext(), "Notifikasi uji coba berhasil dikirim!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(
            onItemClick = { product -> openDetailProduct(product) }
        )

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun observeData() {
        viewModel.urgentProducts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val size = list?.size ?: 0
            binding.tvNotifCountInfo.text = "Daftar Produk Memerlukan Perhatian ($size)"

            if (list.isNullOrEmpty()) {
                binding.layoutEmptyNotifications.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.layoutEmptyNotifications.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
            }
        }
    }

    private fun openDetailProduct(product: ProductEntity) {
        val intent = Intent(requireContext(), DetailProductActivity::class.java).apply {
            putExtra(DetailProductActivity.EXTRA_PRODUCT_ID, product.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}