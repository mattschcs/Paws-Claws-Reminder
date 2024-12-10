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


class add_user_profle : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    private var selectedImageUri: Uri? = null
    private lateinit var newProfileImageView: CircleImageView


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
        val view = inflater.inflate(R.layout.fragment_add_user_profle, container, false)

        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.createProfileHeader)
        headerCreateAccountPage.title = "Create New Profile"

        auth = FirebaseAuth.getInstance()

        val firstName: EditText = view.findViewById(R.id.new_profile_firstName)
        val lastName: EditText = view.findViewById(R.id.new_profile_lastName)
        val dateOfBirth: DatePicker = view.findViewById(R.id.new_profile_dateOfBirth)
        val phoneNumber: EditText = view.findViewById(R.id.new_profile_phoneNumber)
        newProfileImageView = view.findViewById(R.id.new_profile_user_profile_image)
        val addPictureButton: ImageButton = view.findViewById(R.id.new_profile_add_picture_button)
        val createButton: Button = view.findViewById(R.id.createButton)

        setupDatePicker(dateOfBirth)

        addPictureButton.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        createButton.setOnClickListener {
            val firstNameInput = firstName.text.toString()
            val lastNameInput = lastName.text.toString()
            val dobInput = getDateOfBirth(dateOfBirth)
            val phoneNumberInput = phoneNumber.text.toString()

            if (isInputValid(firstNameInput, lastNameInput, dobInput, phoneNumberInput)) {
                createProfile(firstNameInput, lastNameInput, dobInput, phoneNumberInput)
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

    private fun isInputValid(
        firstName: String,
        lastName: String,
        dob: String,
        phoneNumber: String
    ): Boolean {
        return when {
            firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || phoneNumber.isEmpty() -> {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun createProfile(
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val profileId = "${userId}_$phoneNumber" // Generate unique profile ID
            val profileRef = database.child("users").child(userId).child("profiles").child(profileId)

            val profileData = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "dateOfBirth" to dateOfBirth,
                "phoneNumber" to phoneNumber,
                "mainUser" to false // Boolean type
            )

            if (selectedImageUri != null) {
                uploadProfileImage(profileId, profileData)
            } else {
                saveProfileToDatabase(profileId, profileData, null)
            }
        }
    }

    private fun uploadProfileImage(profileId: String, profileData: Map<String, Any>) {
        val imageRef = storageReference.child("profile_images").child("$profileId.jpg")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProfileToDatabase(profileId, profileData, uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to upload profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileToDatabase(profileId: String, profileData: Map<String, Any>, profileImageUrl: String?) {
        val profileWithImage = profileData.toMutableMap()
        profileImageUrl?.let {
            profileWithImage["profileImage"] = it
        }

        database.child("users").child(auth.currentUser!!.uid).child("profiles").child(profileId).setValue(profileWithImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Profile created successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, user_profiles())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(context, "Failed to create profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun selectImage() {
        val getImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                newProfileImageView.setImageURI(uri)
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
        getImageLauncher.launch("image/*")
    }

}