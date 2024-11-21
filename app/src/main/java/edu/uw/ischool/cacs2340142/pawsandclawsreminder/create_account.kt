package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import edu.uw.ischool.cacs2340142.pawsandclawsreminder.databinding.FragmentCreateAccountBinding


class create_account : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)
        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.createAccountHeader)
        headerCreateAccountPage.title = "Create Account"

        auth = FirebaseAuth.getInstance()

        val firstName: EditText = view.findViewById(R.id.firstName)
        val lastName: EditText = view.findViewById(R.id.lastName)
        val dateOfBirth: EditText = view.findViewById(R.id.dateOfBirth)
        val phoneNumber: EditText = view.findViewById(R.id.phoneNumber)
        val email: EditText =view.findViewById(R.id.email)
        val password: EditText = view.findViewById(R.id.password)
        val confirmPassword: EditText = view.findViewById(R.id.confirmPassword)

        val creatButton: Button = view.findViewById(R.id.createButton)



        creatButton.setOnClickListener{
            val firstNameInput = firstName.text.toString()
            val lastNameInput = lastName.text.toString()
            val dobInput = dateOfBirth.text.toString()
            val phoneNumberInput = phoneNumber.text.toString()
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()
            val confirmPasswordInput = confirmPassword.text.toString()

            if(passwordInput == confirmPasswordInput && firstNameInput.isNotEmpty() && lastNameInput.isNotEmpty() && dobInput.isNotEmpty() && phoneNumberInput.isNotEmpty() && emailInput.isNotEmpty()){
                registerUser(firstNameInput, lastNameInput, dobInput, phoneNumberInput, emailInput, passwordInput)
                parentFragmentManager.beginTransaction().replace(R.id.main, LogIn()).addToBackStack(null).commit()
            } else{
                Toast.makeText(context, "Ensure all fields are filled in and that passwords match", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun registerUser(firstName: String, lastName: String, dateOfBirth: String, phoneNumber: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                val userId = auth.currentUser?.uid
                val userAccount = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "dateOfBirth" to dateOfBirth,
                    "phoneNumber" to phoneNumber,
                    "email" to email,
                    "mainUser" to true)
                if(userId != null){
                    database.child("users").child(userId).setValue(userAccount).addOnCompleteListener{ dbTask ->
                        if(dbTask.isSuccessful){
                            Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(context, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Failed to create account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}