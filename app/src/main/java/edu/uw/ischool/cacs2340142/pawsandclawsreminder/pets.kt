package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.*
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import de.hdodenhof.circleimageview.CircleImageView
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView


class pets : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var petsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pets, container, false)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize Views
        petsContainer = view.findViewById(R.id.dynamic_pets)
        val addPetPicture: CircleImageView = view.findViewById(R.id.add_pet_picture)

        // Set click listener for add_pet_picture
        addPetPicture.setOnClickListener {
            navigateToCreatePetFragment()
        }

        // Set up BottomNavigationView
        val bottomNavigationView: BottomNavigationView? =
            view.findViewById(R.id.bottomNavigationViewPet)
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


        loadAllPets()

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCreatePetFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, create_pet())
            .addToBackStack(null)
            .commit()
    }

    private fun loadAllPets() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                petsContainer.removeAllViews()
                for (userSnapshot in snapshot.children) {
                    val petsSnapshot = userSnapshot.child("pets")
                    if (petsSnapshot.exists()) {
                        for (petSnapshot in petsSnapshot.children) {
                            val petName =
                                petSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                            val petImageUrl =
                                petSnapshot.child("imageUrl").getValue(String::class.java)
                            addPetToContainer(petName, petImageUrl)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load pets: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun addPetToContainer(petName: String, petImageUrl: String?) {
        val petView = layoutInflater.inflate(R.layout.item_pet, petsContainer, false)

        val petNameTextView = petView.findViewById<TextView>(R.id.pet_name)
        val petImageView = petView.findViewById<CircleImageView>(R.id.pet_picture)

        petNameTextView.text = petName

        if (!petImageUrl.isNullOrEmpty()) {
            loadPetImage(petImageUrl, petImageView)
        } else {
            petImageView.setImageResource(R.drawable.pet_icon_temp) // Default placeholder
        }

        petsContainer.addView(petView)
    }


    private fun loadPetImage(imageUrl: String, imageView: CircleImageView) {
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
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.pet_icon_temp) // Default placeholder
                }
            }
        }
    }
}