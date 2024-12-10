package edu.uw.ischool.cacs2340142.pawsandclawsreminder

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



class user_account : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var profilePicture: CircleImageView
    private lateinit var userName: TextView
    private lateinit var userAge: TextView
    private lateinit var userEmail: TextView
    private lateinit var userPhoneNumber: TextView
    private var profileId: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationViewProfile)

        // Highlight the Profile tab
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationViewProfile)
        navBar?.selectedItemId = R.id.profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pets -> {
                    navigateToFragment(Pets())
                    true
                }
                R.id.task -> {
                    navigateToFragment(Task())
                    true
                }
                R.id.reminders -> {
                    navigateToFragment(Reminders())
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }

    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_account, container, false)
        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.userAccountHeader)
        headerCreateAccountPage.title = "Profile"



        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        profilePicture = view.findViewById(R.id.profile_image)
        userName = view.findViewById(R.id.users_name)
        userAge = view.findViewById(R.id.user_age)
        userEmail = view.findViewById(R.id.user_email)
        userPhoneNumber = view.findViewById(R.id.userPhoneNumber)
        val switchUser: Button = view.findViewById(R.id.switchUserButton)

        // Retrieve profileId from arguments
        profileId = arguments?.getString("profileId")

        // Load user data
        loadUserData()

        switchUser.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, user_profiles())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            val userRef: DatabaseReference = if (profileId == null) {
                // Load main user data
                database.child("users").child(userId)
            } else {
                // Load selected user profile data
                database.child("users").child(userId).child("profiles").child(profileId!!)
            }

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Fetch user data
                        val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                        val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                        val email = if (profileId == null) {
                            snapshot.child("email").getValue(String::class.java) ?: "N/A"
                        } else {
                            null // No email for profiles
                        }
                        val birthday = snapshot.child("dateOfBirth").getValue(String::class.java) ?: "N/A"
                        val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "N/A"
                        val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)

                        // Concatenate first name and last name
                        val fullName = "$firstName $lastName".trim()

                        // Calculate age
                        val age = if (birthday != "N/A") calculateAge(birthday) else 0

                        // Update UI
                        userName.text = fullName
                        userAge.text = "Age: $age"
                        userPhoneNumber.text = "Phone: $phoneNumber"

                        // Show or hide email dynamically
                        if (email != null) {
                            userEmail.visibility = View.VISIBLE
                            userEmail.text = "Email: $email"
                        } else {
                            userEmail.visibility = View.GONE
                        }

                        // Load profile image
                        if (!profileImageUrl.isNullOrEmpty()) {
                            loadImageFromUrl(profileImageUrl, profilePicture)
                        }
                    } else {
                        Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun calculateAge(birthday: String): Int {
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
            return age
        }

        return 0
    }
}
