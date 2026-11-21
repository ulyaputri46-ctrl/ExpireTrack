package com.example.expiretrack.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expiretrack.MainActivity
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.databinding.FragmentDashboardBinding
import com.example.expiretrack.ui.activity.AddProductActivity
import com.example.expiretrack.ui.activity.DetailProductActivity
import com.example.expiretrack.ui.adapter.ProductAdapter
import com.example.expiretrack.ui.viewmodel.ProductViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.btnSeeAll.setOnClickListener {
            (activity as? MainActivity)?.navigateToProductsTab()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product -> openDetailProduct(product) },
            onEditClick = { product -> openEditProduct(product) },
            onDeleteClick = { product -> confirmDeleteProduct(product) }
        )

        binding.rvUrgentProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUrgentProducts.adapter = adapter
    }

    private fun observeData() {
        viewModel.productSummary.observe(viewLifecycleOwner) { summary ->
            binding.tvStatTotalCount.text = summary.totalCount.toString()
            binding.tvStatSafeCount.text = summary.safeCount.toString()
            binding.tvStatWarningCount.text = summary.warningCount.toString()
            binding.tvStatExpiredCount.text = summary.expiredCount.toString()
        }

        viewModel.urgentProducts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNullOrEmpty()) {
                binding.layoutEmptyDashboard.visibility = View.VISIBLE
                binding.rvUrgentProducts.visibility = View.GONE
            } else {
                binding.layoutEmptyDashboard.visibility = View.GONE
                binding.rvUrgentProducts.visibility = View.VISIBLE
            }
        }
    }

    private fun openDetailProduct(product: ProductEntity) {
        val intent = Intent(requireContext(), DetailProductActivity::class.java).apply {
            putExtra(DetailProductActivity.EXTRA_PRODUCT_ID, product.id)
        }
        startActivity(intent)
    }

    private fun openEditProduct(product: ProductEntity) {
        val intent = Intent(requireContext(), AddProductActivity::class.java).apply {
            putExtra(AddProductActivity.EXTRA_PRODUCT, product)
        }
        startActivity(intent)
    }

    private fun confirmDeleteProduct(product: ProductEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}