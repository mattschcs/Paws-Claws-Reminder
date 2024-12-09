package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth


class LogIn : Fragment() {
    private lateinit var auth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        auth = FirebaseAuth.getInstance()

        val emailInput: EditText = view.findViewById(R.id.email_input)
        val passwordInput: EditText =view.findViewById(R.id.password_input)
        val signInButton: Button = view.findViewById(R.id.sign_in_button)
        val createAccountLink: TextView = view.findViewById(R.id.sign_up_link)

        signInButton.setOnClickListener{
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                loginUser(email, password)
            } else{
                Toast.makeText(context, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountLink.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.main, create_account()).addToBackStack(null).commit()
        }
        return view
    }

    private fun loginUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                parentFragmentManager.beginTransaction().replace(R.id.main, Pets()).commit()
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}