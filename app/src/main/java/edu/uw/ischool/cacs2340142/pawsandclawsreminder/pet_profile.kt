package edu.uw.ischool.cacs2340142.pawsandclawsreminder


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class pet_profile : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var petProfileImage: CircleImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petAgeTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var switchPetButton: Button

    private var selectedPetId: String? = null // Received from navigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_profile, container, false)

        // Initialize views
        petProfileImage = view.findViewById(R.id.petProfileImage)
        petNameTextView = view.findViewById(R.id.pets_name)
        petAgeTextView = view.findViewById(R.id.pet_age)
        petTypeTextView = view.findViewById(R.id.pet_type)
        switchPetButton = view.findViewById(R.id.switchPetButton)


        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the selectedPetId from arguments
        selectedPetId = arguments?.getString("petId")

        // Load pet data
        selectedPetId?.let { loadPetData(it) } ?: run {
            Toast.makeText(context, "Pet ID not found", Toast.LENGTH_SHORT).show()
        }

        switchPetButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, pets())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun loadPetData(petId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userId = it.uid
            val petRef = database.child("users").child(userId).child("pets").child(petId)

            petRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val dob = snapshot.child("dob").getValue(String::class.java) ?: "Unknown"
                        val type = snapshot.child("type").getValue(String::class.java) ?: "Unknown"
                        val imageUrl =
                            snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                        petNameTextView.text = name
                        petTypeTextView.text = type

                        // Calculate age
                        petAgeTextView.text = calculateAge(dob)?.let { "$it years old" } ?: "Age Unknown"

                        // Load image
                        loadImageFromUrl(imageUrl, petProfileImage)
                    } else {
                        Toast.makeText(context, "Pet data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load pet data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadImageFromUrl(imageUrl: String, imageView: CircleImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateAge(birthday: String): Int? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = dateFormat.parse(birthday)
            val today = Calendar.getInstance()

            birthDate?.let {
                val birthCalendar = Calendar.getInstance()
                birthCalendar.time = it

                var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}