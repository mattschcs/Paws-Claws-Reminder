package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.*
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.net.HttpURLConnection
import java.net.URL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class pets : Fragment() {
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var petContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pets, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        petContainer = view.findViewById(R.id.pets_container)
        val addPetPicture: CircleImageView = view.findViewById(R.id.add_pet_picture)

        // Load pet data
        loadPetData()

        // Set up BottomNavigationView
        val bottomNavigationView: BottomNavigationView? = view.findViewById(R.id.bottomNavigationViewPet)
        bottomNavigationView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pets -> true
                R.id.task -> {
                    navigateToFragment(Task())
                    true
                }
                R.id.reminders -> {
                    navigateToFragment(Reminders())
                    true
                }
                R.id.profile -> {
                    navigateToFragment(user_account())
                    true
                }
                else -> false
            }
        }

        addPetPicture.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, create_pet())
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToPetProfile(petId: String) {
        val fragment = pet_profile()
        val args = Bundle()
        args.putString("petId", petId)
        fragment.arguments = args

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadPetData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val petRef = database.child("users").child(userId).child("pets")

            petRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (petSnapshot in snapshot.children) {
                        val petId = petSnapshot.key ?: continue
                        val name = petSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val imageUrl = petSnapshot.child("imageUrl").getValue(String::class.java)

                        addPetView(petId, name, imageUrl)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load pets: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun addPetView(petId: String, name: String, imageUrl: String?) {
        val petItemView = LayoutInflater.from(context).inflate(R.layout.item_pet, petContainer, false)

        val petNameTextView: TextView = petItemView.findViewById(R.id.pet_name)
        val petImageView: CircleImageView = petItemView.findViewById(R.id.pet_picture)

        petNameTextView.text = name

        // Load image using the custom function
        if (!imageUrl.isNullOrEmpty()) {
            loadPetImage(imageUrl, petImageView)
        } else {
            petImageView.setImageResource(R.drawable.ic_pet_placeholder) // Default placeholder image
        }

        // Set click listener to navigate to pet profile
        petItemView.setOnClickListener {
            navigateToPetProfile(petId)
        }

        petContainer.addView(petItemView)
    }

    private fun loadPetImage(imageUrl: String, imageView: CircleImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = java.net.URL(imageUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
