package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class create_account : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)
        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.createAccountHeader)
        headerCreateAccountPage.title = "Create Account"

        auth = FirebaseAuth.getInstance()

        val firstName: EditText = view.findViewById(R.id.firstName)
        val lastName: EditText = view.findViewById(R.id.lastName)
        val dateOfBirth: DatePicker = view.findViewById(R.id.dateOfBirth)
        val phoneNumber: EditText = view.findViewById(R.id.phoneNumber)
        val email: EditText = view.findViewById(R.id.email)
        val password: EditText = view.findViewById(R.id.password)
        val confirmPassword: EditText = view.findViewById(R.id.confirmPassword)

        val createButton: Button = view.findViewById(R.id.createButton)
        setupDatePicker(dateOfBirth)

        createButton.setOnClickListener {
            val firstNameInput = firstName.text.toString()
            val lastNameInput = lastName.text.toString()
            val dobInput = getDateOfBirth(dateOfBirth)
            val phoneNumberInput = phoneNumber.text.toString()
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()
            val confirmPasswordInput = confirmPassword.text.toString()

            if (isInputValid(firstNameInput, lastNameInput, dobInput, phoneNumberInput, emailInput, passwordInput, confirmPasswordInput)) {
                registerUser(firstNameInput, lastNameInput, dobInput, phoneNumberInput, emailInput, passwordInput)
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
        phoneNumber: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun registerUser(
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        phoneNumber: String,
        email: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    saveUserDataToDatabase(userId, firstName, lastName, dateOfBirth, phoneNumber, email)
                } else {
                    Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error"
                Toast.makeText(context, "Failed to create account: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserDataToDatabase(
        userId: String,
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        phoneNumber: String,
        email: String
    ) {
        val userAccount = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "dateOfBirth" to dateOfBirth,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "mainUser" to true
        )

        database.child("users").child(userId).setValue(userAccount).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main, LogIn())
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

