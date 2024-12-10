package edu.uw.ischool.cmoh.paws_claws_reminder

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.File

class CreatePetFragment : Fragment() {
    private lateinit var uploadPhotoButton: Button
    private lateinit var petNameEditText: EditText
    private lateinit var petTypeSpinner: Spinner
    private lateinit var petDobEditText: EditText
    private lateinit var createButton: Button
    private lateinit var photoPreview: ImageView
    private var petPhotoUri: String = ""

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_pet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        uploadPhotoButton = view.findViewById(R.id.Upload_photo)
        petNameEditText = view.findViewById(R.id.et_pet_name)
        petTypeSpinner = view.findViewById(R.id.spinner_pet_type)
        petDobEditText = view.findViewById(R.id.et_pet_dob)
        createButton = view.findViewById(R.id.btn_create_pet)
        photoPreview = view.findViewById(R.id.photo_preview)

        // Initialize ActivityResultLauncher
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
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

        // Set Spinner data source
        ArrayAdapter.createFromResource(
            requireContext(), R.array.pet_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            petTypeSpinner.adapter = adapter
        }

        uploadPhotoButton.setOnClickListener { selectPhoto() }
        createButton.setOnClickListener { createPetProfile() }
    }

    private fun selectPhoto() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Take photo
                        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePhotoLauncher.launch(takePhotoIntent)
                    }
                    1 -> { // Choose from gallery
                        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickPhotoLauncher.launch(pickPhotoIntent)
                    }
                }
            }
            .show()
    }

    private fun showPhotoPreview(photoBitmap: Bitmap) {
        val previewView = ImageView(requireContext()).apply {
            setImageBitmap(photoBitmap)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        AlertDialog.Builder(requireContext())
            .setView(previewView)
            .setPositiveButton("Use Photo") { _, _ ->
                val uri = saveBitmapToUri(photoBitmap)
                petPhotoUri = uri.toString()
                updatePhotoPreview()
            }
            .setNegativeButton("Retake") { dialog, _ ->
                dialog.dismiss()
                selectPhoto()
            }
            .show()
    }

    private fun saveBitmapToUri(bitmap: Bitmap): Uri {
        val file = File(requireContext().filesDir, "temp_photo_${System.currentTimeMillis()}.jpg")
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
            Toast.makeText(requireContext(), "Pet name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val petDob = petDobEditText.text.toString()
        if (petDob.isNotBlank() && !isValidDateFormat(petDob)) {
            Toast.makeText(requireContext(), "Invalid date format. Please use MM/DD/YYYY.", Toast.LENGTH_SHORT).show()
            return
        }

        val petType = petTypeSpinner.selectedItem.toString()

        val pet = PetModel( // Changed to use PetModel
            name = petName,
            photoUri = petPhotoUri,
            type = petType,
            dob = petDob
        )
        PetRepository.addPet(pet)

        // Return to main screen
        parentFragmentManager.popBackStack()
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
