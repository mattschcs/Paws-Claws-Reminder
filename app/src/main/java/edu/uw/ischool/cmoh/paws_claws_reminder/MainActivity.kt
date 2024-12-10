package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavBar: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()


        bottomNavBar = findViewById(R.id.bottomNavigationView)
        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.pets -> replaceFragment(PetsMainFragment())
                R.id.task -> replaceFragment(Task())
                R.id.reminders -> replaceFragment(Reminders())
                R.id.profile -> replaceFragment(Profile())
            }
            true
        }

        if (savedInstanceState == null) {
            startFragment(LogIn())
            bottomNavBar.visibility = View.GONE

        } else {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
        updateNavBarVisibility(fragment)
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.commit()
        updateNavBarVisibility(fragment)
    }

    fun updateNavBarVisibility(fragment: Fragment) {
        // Check the type of fragment and hide/show the navBar
        when (fragment) {
            is LogIn, is create_account -> {
                bottomNavBar.visibility = View.GONE // Hide navBar for login and create user
            }
            else -> {
                bottomNavBar.visibility = View.VISIBLE // Show navBar for other fragments
            }
        }
    }
}