package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import android.net.*
import androidx.activity.result.contract.ActivityResultContracts
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID


class create_pet : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var petImageView: CircleImageView
    private lateinit var petNameEditText: EditText
    private lateinit var petDOBPicker: DatePicker
    private lateinit var petTypeSpinner: Spinner
    private lateinit var addPetButton: Button
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            petImageView.setImageURI(uri)
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_pet, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        petImageView = view.findViewById(R.id.pet_profile_image)
        petNameEditText = view.findViewById(R.id.petName)
        petDOBPicker = view.findViewById(R.id.petDateOfBirth)
        petTypeSpinner = view.findViewById(R.id.spinner_pet_type)
        addPetButton = view.findViewById(R.id.addPetButton)
        val addPictureButton: ImageButton = view.findViewById(R.id.add_pet_picture_button)

        // Setup Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.pet_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            petTypeSpinner.adapter = adapter
        }

        // Setup DatePicker
        setupDatePicker(petDOBPicker)

        // Setup Image Picker
        addPictureButton.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // Setup Add Pet Button
        addPetButton.setOnClickListener {
            val petName = petNameEditText.text.toString().trim()
            val petDOB = getDateOfBirth(petDOBPicker)
            val petType = petTypeSpinner.selectedItem.toString()

            if (isInputValid(petName, petDOB, petType)) {
                createPet(petName, petDOB, petType)
            }
        }

        return view
    }

    private fun setupDatePicker(datePicker: DatePicker) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        datePicker.init(currentYear, currentMonth, currentDay, null)
    }

    private fun getDateOfBirth(datePicker: DatePicker): String {
        val year = datePicker.year
        val month = datePicker.month + 1
        val day = datePicker.dayOfMonth
        return "$day/$month/$year"
    }

    private fun isInputValid(petName: String, petDOB: String, petType: String): Boolean {
        return when {
            petName.isEmpty() || petDOB.isEmpty() || petType.isEmpty() -> {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                false
            }
            selectedImageUri == null -> {
                Toast.makeText(context, "Please select a pet image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun createPet(petName: String, petDOB: String, petType: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val petId = database.push().key ?: UUID.randomUUID().toString()
            val petRef = database.child("users").child(userId).child("pets").child(petId)

            val petData = mapOf(
                "name" to petName,
                "dob" to petDOB,
                "type" to petType
            )

            if (selectedImageUri != null) {
                uploadPetImage(petId, petData)
            } else {
                savePetToDatabase(petId, petData, null)
            }
        }
    }

    private fun uploadPetImage(petId: String, petData: Map<String, Any>) {
        val imageRef = storageReference.child("pet_images/$petId.jpg")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    savePetToDatabase(petId, petData, uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to upload pet image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePetToDatabase(petId: String, petData: Map<String, Any>, petImageUrl: String?) {
        val petWithImage = petData.toMutableMap()
        petImageUrl?.let {
            petWithImage["imageUrl"] = it
        }

        database.child("users").child(auth.currentUser!!.uid).child("pets").child(petId).setValue(petWithImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Pet added successfully", Toast.LENGTH_SHORT).show()
                    navigateBackToPetsFragment()
                } else {
                    Toast.makeText(context, "Failed to add pet: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateBackToPetsFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, pets())
            .addToBackStack(null)
            .commit()
    }
}
