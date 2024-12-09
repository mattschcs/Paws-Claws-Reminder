package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavBar : AppCompatActivity() {

    private lateinit var bottomNavBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.navbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavBar = findViewById(R.id.bottomNavigationView)
        replaceFragment(Pets())

        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.pets -> replaceFragment(Pets())
                R.id.task -> replaceFragment(Task())
                R.id.reminders -> replaceFragment(Reminders())
                R.id.profile -> replaceFragment(Profile())

                else -> {

                }
            }
            true
        }
    }

//    replace the frame layout with the fragments
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}