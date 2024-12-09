package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import java.text.SimpleDateFormat
import java.util.*



class user_account : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_account, container, false)
        val headerUserAccountPage: Toolbar = view.findViewById(R.id.userAccountHeader)
        headerUserAccountPage.title = "Account Info"

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null){
            val userId = currentUser.uid

            val userName: TextView = view.findViewById(R.id.users_name)
            val userAge: TextView = view.findViewById(R.id.user_age)
            val userEmail: TextView = view.findViewById(R.id.user_email)
            val userPhoneNumber: TextView = view.findViewById(R.id.userPhoneNumber)

            loadUserInfo(userId, userName, userAge, userEmail, userPhoneNumber)
        } else{
            Toast.makeText(context, "Please log in to continue", Toast.LENGTH_LONG).show()
        }

        return view
    }

    private fun loadUserInfo(
        userId: String,
        userName: TextView,
        userAge: TextView,
        userEmail: TextView,
        userPhoneNumber: TextView
    ) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Extract individual fields directly
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: "Unknown"
                    val dateOfBirth = snapshot.child("dateOfBirth").getValue(String::class.java) ?: "Unknown"
                    val email = snapshot.child("email").getValue(String::class.java) ?: "Unknown"
                    val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "Unknown"

                    val name = "$firstName $lastName"
                    
                    userName.text = name
                    userAge.text = "Age: ${calculateAge(dateOfBirth)}"
                    userEmail.text = "Email: $email"
                    userPhoneNumber.text = "Phone: $phoneNumber"
                } else {
                    Toast.makeText(context, "User not found!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load user info: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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