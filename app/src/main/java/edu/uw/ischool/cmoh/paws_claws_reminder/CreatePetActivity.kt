package edu.uw.ischool.cmoh.paws_claws_reminder

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class CreatePetActivity : AppCompatActivity() {
    private lateinit var uploadPhotoButton: Button
    private lateinit var petNameEditText: EditText
    private lateinit var petTypeSpinner: Spinner
    private lateinit var petDobEditText: EditText
    private lateinit var createButton: Button
    private lateinit var photoPreview: ImageView // 更新的预览图片视图
    private var petPhotoUri: String = ""

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pet)

        // 初始化控件
        uploadPhotoButton = findViewById(R.id.Upload_photo)
        petNameEditText = findViewById(R.id.et_pet_name)
        petTypeSpinner = findViewById(R.id.spinner_pet_type)
        petDobEditText = findViewById(R.id.et_pet_dob)
        createButton = findViewById(R.id.btn_create_pet)
        photoPreview = findViewById(R.id.photo_preview)

        // 初始化 ActivityResultLauncher
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    // 对于 API 33 或更高版本
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    // 对于 API 28 到 32 的情况
                    @Suppress("DEPRECATION")
                    result.data?.extras?.get("data") as? Bitmap
                }
                if (photoBitmap != null) {
                    showPhotoPreview(photoBitmap)
                }
            }
        }




        pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    petPhotoUri = selectedImageUri.toString()
                    updatePhotoPreview()
                }
            }
        }

        // 设置 Spinner 数据源
        ArrayAdapter.createFromResource(
            this, R.array.pet_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            petTypeSpinner.adapter = adapter
        }

        uploadPhotoButton.setOnClickListener { selectPhoto() }
        createButton.setOnClickListener { createPetProfile() }
    }

    private fun selectPhoto() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Upload Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // 拍照
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePhotoLauncher.launch(takePhotoIntent)
                }
                1 -> { // 从相册选择
                    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickPhotoLauncher.launch(pickPhotoIntent)
                }
            }
        }
        builder.show()
    }

    private fun showPhotoPreview(photoBitmap: Bitmap) {
        val builder = android.app.AlertDialog.Builder(this)
        val previewView = ImageView(this).apply {
            setImageBitmap(photoBitmap)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        builder.setView(previewView)
        builder.setPositiveButton("Use Photo") { _, _ ->
            val uri = saveBitmapToUri(photoBitmap)
            petPhotoUri = uri.toString()
            updatePhotoPreview()
        }
        builder.setNegativeButton("Retake") { dialog, _ ->
            dialog.dismiss()
            selectPhoto()
        }
        builder.show()
    }

    private fun saveBitmapToUri(bitmap: Bitmap): Uri {
        val file = File(filesDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return Uri.fromFile(file)
    }

    private fun updatePhotoPreview() {
        if (petPhotoUri.isNotEmpty()) {
            photoPreview.setImageURI(Uri.parse(petPhotoUri))
        } else {
            photoPreview.setImageResource(R.drawable.ic_pet_placeholder)
        }
    }

    private fun createPetProfile() {
        val petName = petNameEditText.text.toString()
        if (petName.isBlank()) {
            Toast.makeText(this, "Pet name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val petDob = petDobEditText.text.toString()
        if (petDob.isNotBlank() && !isValidDateFormat(petDob)) {
            Toast.makeText(this, "Invalid date format. Please use MM/DD/YYYY.", Toast.LENGTH_SHORT).show()
            return
        }

        val petType = petTypeSpinner.selectedItem.toString()

        val pet = Pet(
            name = petName,
            photoUri = petPhotoUri,
            type = petType,
            dob = petDob
        )
        PetRepository.addPet(pet)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")
            java.time.LocalDate.parse(date, formatter)
            true
        } catch (e: java.time.format.DateTimeParseException) {
            false
        }
    }
}
