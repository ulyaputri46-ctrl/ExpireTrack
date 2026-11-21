package com.example.expiretrack.ui.activity

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.databinding.ActivityAddProductBinding
import com.example.expiretrack.ui.viewmodel.ProductViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
    }

    private lateinit var binding: ActivityAddProductBinding
    private val viewModel: ProductViewModel by viewModels()

    private var selectedImageUri: String? = null
    private var tempCameraUri: Uri? = null
    private var editingProduct: ProductEntity? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            val savedLocalUri = copyImageToInternalStorage(this, sourceUri)
            if (savedLocalUri != null) {
                selectedImageUri = savedLocalUri
                binding.imgPhotoPreview.setImageURI(Uri.parse(savedLocalUri))
                binding.imgPhotoPreview.visibility = View.VISIBLE
                binding.layoutPhotoPlaceholder.visibility = View.GONE
            } else {
                Toast.makeText(this, "Gagal memuat foto produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && tempCameraUri != null) {
            val savedLocalUri = copyImageToInternalStorage(this, tempCameraUri!!)
            if (savedLocalUri != null) {
                selectedImageUri = savedLocalUri
                binding.imgPhotoPreview.setImageURI(Uri.parse(savedLocalUri))
                binding.imgPhotoPreview.visibility = View.VISIBLE
                binding.layoutPhotoPlaceholder.visibility = View.GONE
            } else {
                Toast.makeText(this, "Gagal menyimpan foto dari kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdowns()
        setupDatePickers()

        editingProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_PRODUCT, ProductEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_PRODUCT) as? ProductEntity
        }

        editingProduct?.let { setupEditMode(it) }

        binding.cardPickPhoto.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupToolbar() {
        binding.toolbarAddProduct.setNavigationOnClickListener {
            finish()
        }

        if (editingProduct != null) {
            binding.toolbarAddProduct.title = "Edit Produk"
        }
    }

    private fun setupDropdowns() {
        val categories = arrayOf(
            "Makanan",
            "Minuman",
            "Obat & Kesehatan",
            "Skincare & Kosmetik",
            "Bumbu & Bahan Dapur",
            "Perawatan Pribadi",
            "Lainnya"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        binding.etPurchaseDate.setText(todayStr)
        binding.etExpiryDate.setText(todayStr)

        binding.etPurchaseDate.setOnClickListener {
            showDatePickerDialog(binding.etPurchaseDate.text.toString()) { date ->
                binding.etPurchaseDate.setText(date)
            }
        }

        binding.etExpiryDate.setOnClickListener {
            showDatePickerDialog(binding.etExpiryDate.text.toString()) { date ->
                binding.etExpiryDate.setText(date)
            }
        }
    }

    private fun showDatePickerDialog(currentDateStr: String, onDateSet: (String) -> Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        try {
            val date = dateFormat.parse(currentDateStr)
            if (date != null) calendar.time = date
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val result = dateFormat.format(selectedCalendar.time)
            onDateSet(result)
        }, year, month, day).show()
    }

    private fun setupEditMode(product: ProductEntity) {
        binding.etProductName.setText(product.name)
        binding.actvCategory.setText(product.category, false)
        binding.etBrand.setText(product.brand)
        binding.etPurchaseDate.setText(product.purchaseDate)
        binding.etExpiryDate.setText(product.expiryDate)
        binding.etQuantity.setText(product.quantity.toString())
        binding.etNotes.setText(product.notes ?: "")

        if (!product.imageUri.isNullOrEmpty()) {
            selectedImageUri = product.imageUri
            try {
                val uri = Uri.parse(product.imageUri)
                contentResolver.openInputStream(uri)?.close()
                binding.imgPhotoPreview.setImageURI(uri)
                binding.imgPhotoPreview.visibility = View.VISIBLE
                binding.layoutPhotoPlaceholder.visibility = View.GONE
            } catch (e: Exception) {
                // Fallback if image was deleted or inaccessible
                selectedImageUri = null
            }
        }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val brand = binding.etBrand.text.toString().trim()
        val purchaseDate = binding.etPurchaseDate.text.toString().trim()
        val expiryDate = binding.etExpiryDate.text.toString().trim()
        val quantityStr = binding.etQuantity.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (name.isEmpty()) {
            binding.etProductName.error = "Nama produk wajib diisi"
            binding.etProductName.requestFocus()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Harap pilih kategori produk", Toast.LENGTH_SHORT).show()
            return
        }

        if (purchaseDate.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Tanggal pembelian dan kedaluwarsa wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val pDate = dateFormat.parse(purchaseDate)
            val eDate = dateFormat.parse(expiryDate)

            if (pDate != null && eDate != null && eDate.before(pDate)) {
                Toast.makeText(
                    this,
                    "Tanggal kedaluwarsa tidak boleh lebih awal dari tanggal pembelian!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull() ?: 1

        val product = ProductEntity(
            id = editingProduct?.id ?: "",
            name = name,
            category = category,
            brand = brand,
            purchaseDate = purchaseDate,
            expiryDate = expiryDate,
            quantity = quantity,
            notes = if (notes.isNotEmpty()) notes else null,
            imageUri = selectedImageUri
        )

        if (editingProduct == null) {
            viewModel.insertProduct(product) { newId ->
                if (newId != null) {
                    Toast.makeText(this, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            viewModel.updateProduct(product) { success ->
                if (success) {
                    Toast.makeText(this, "Informasi produk berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal memperbarui database", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val photoDir = File(context.filesDir, "product_photos")
            if (!photoDir.exists()) photoDir.mkdirs()

            val photoFile = File(photoDir, "photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(photoFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Ambil Foto (Kamera)", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> selectImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        try {
            val photoFile = File.createTempFile(
                "temp_camera_${System.currentTimeMillis()}",
                ".jpg",
                cacheDir
            )
            tempCameraUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            tempCameraUri?.let {
                takePictureLauncher.launch(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
        }
    }
}