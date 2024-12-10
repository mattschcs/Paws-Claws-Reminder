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

    private var selectedImageUri: Uri? = null
    private lateinit var newProfileImageView: CircleImageView

    private lateinit var petImageView: CircleImageView
    private lateinit var petNameEditText: EditText
    private lateinit var petTypeSpinner: Spinner
    private lateinit var petDOBPicker: DatePicker
    private lateinit var addPetButton: Button


    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            newProfileImageView.setImageURI(uri)
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_pet, container, false)
        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.createPetHeader)
        headerCreateAccountPage.title = "Create New Pet Profile"

        auth = FirebaseAuth.getInstance()

        petImageView = view.findViewById(R.id.pet_profile_image)
        petNameEditText = view.findViewById(R.id.petName)
        petTypeSpinner = view.findViewById(R.id.spinner_pet_type)
        petDOBPicker = view.findViewById(R.id.petDateOfBirth)
        addPetButton = view.findViewById(R.id.addPetButton)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.pet_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            petTypeSpinner.adapter = adapter
        }

        petImageView.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        addPetButton.setOnClickListener {
            addPet()
        }


        return view
    }

    private fun navigateBackToPetsFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ())
            .addToBackStack(null)
            .commit()
    }

    private fun addPet() {
        val petName = petNameEditText.text.toString().trim()
        val petType = petTypeSpinner.selectedItem.toString()
        val petDOB = "${petDOBPicker.month + 1}/${petDOBPicker.dayOfMonth}/${petDOBPicker.year}"

        if (petName.isEmpty() || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val petId = database.reference.push().key ?: UUID.randomUUID().toString()
        val petData = mapOf(
            "id" to petId,
            "name" to petName,
            "type" to petType,
            "dob" to petDOB
        )

        uploadPetImage(petId, petData)
    }


    private fun setupDatePicker(datePicker: DatePicker) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        datePicker.init(currentYear, currentMonth, currentDay, null)
    }

    private fun savePetToDatabase(petId: String, petData: Map<String, Any>, imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val petRef = database.reference.child("pets").child(petId)

        val petDataWithImage = petData.toMutableMap()
        petDataWithImage["imageUrl"] = imageUrl

        petRef.setValue(petDataWithImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Pet added successfully!", Toast.LENGTH_SHORT).show()
                    navigateBackToPetsFragment()
                } else {
                    Toast.makeText(requireContext(), "Failed to add pet to database.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadPetImage(petId: String, petData: Map<String, Any>) {
        val imageRef = storageReference.child("pet_images").child("$petId.jpg")
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


}