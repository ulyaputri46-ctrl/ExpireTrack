package com.example.expiretrack.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.databinding.FragmentProductBinding
import com.example.expiretrack.ui.activity.AddProductActivity
import com.example.expiretrack.ui.activity.DetailProductActivity
import com.example.expiretrack.ui.adapter.ProductAdapter
import com.example.expiretrack.ui.viewmodel.ProductViewModel

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilterSpinners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product -> openDetailProduct(product) },
            onEditClick = { product -> openEditProduct(product) },
            onDeleteClick = { product -> confirmDeleteProduct(product) }
        )

        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchQuery.value = query
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun setupFilterSpinners() {
        val categories = arrayOf(
            "Semua Kategori",
            "Makanan",
            "Minuman",
            "Obat & Kesehatan",
            "Skincare & Kosmetik",
            "Bumbu & Bahan Dapur",
            "Perawatan Pribadi",
            "Lainnya"
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerCategoryFilter.adapter = categoryAdapter
        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedCategory.value = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val statuses = arrayOf(
            "Semua Status",
            "Aman",
            "Segera Kedaluwarsa",
            "Hari Ini",
            "Kedaluwarsa"
        )

        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerStatusFilter.adapter = statusAdapter
        binding.spinnerStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedStatus.value = statuses[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeData() {
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            val size = products.size
            binding.tvProductCountInfo.text = "Menampilkan $size produk"

            if (products.isEmpty()) {
                binding.layoutEmptyProducts.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.layoutEmptyProducts.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
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