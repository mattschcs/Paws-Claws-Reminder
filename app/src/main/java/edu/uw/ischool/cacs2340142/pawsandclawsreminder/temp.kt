package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class temp : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_temp, container, false)

        val signOutButton: Button = view.findViewById(R.id.tempbtn)

        signOutButton.setOnClickListener {
            signOutUser()
        }

        return view
    }

    private fun signOutUser() {
        // Clear user session (if any saved data, like SharedPreferences)
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", 0)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect to the login screen
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


}