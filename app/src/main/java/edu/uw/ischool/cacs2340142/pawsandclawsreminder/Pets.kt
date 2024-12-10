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


class PetsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pets, container, false)

        // Initialize views
        val addPetPicture: CircleImageView = view.findViewById(R.id.add_pet_picture)

        // Set click listener for add_pet_picture
        addPetPicture.setOnClickListener {
            navigateToCreatePetFragment()
        }

        // Set up BottomNavigationView (only if it exists in the layout)
        val bottomNavigationView: BottomNavigationView? = view.findViewById(R.id.bottomNavigationView)
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
}