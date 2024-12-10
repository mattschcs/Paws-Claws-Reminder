package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class user_profiles : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dynamicProfilesContainer: LinearLayout
    private lateinit var addUserProfilePicture: CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profiles, container, false)
        val headerCreateAccountPage: Toolbar = view.findViewById(R.id.userProfilesHeader)
        headerCreateAccountPage.title = "User Profiles"

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        dynamicProfilesContainer = view.findViewById(R.id.dynamic_profiles)
        addUserProfilePicture = view.findViewById(R.id.add_user_profile_picture)

        // Load main user and other profiles
        loadMainUser(view)
        loadOtherProfiles(view)

        // Add user button click listener
        addUserProfilePicture.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, add_user_profle())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun loadMainUser(view: View) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                        val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)

                        val mainUserProfilePicture: CircleImageView =
                            view.findViewById(R.id.main_user_profile_picture)
                        val mainUsersName: TextView = view.findViewById(R.id.main_users_name)

                        mainUsersName.text = name

                        // Load main user's profile image
                        if (!profileImageUrl.isNullOrEmpty()) {
                            loadImageFromUrl(profileImageUrl, mainUserProfilePicture)
                        }

                        // Make main user's profile clickable
                        mainUserProfilePicture.setOnClickListener {
                            val mainUserFragment = user_account()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, mainUserFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load main user: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadOtherProfiles(view: View) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            val profilesRef = database.child("users").child(userId).child("profiles")

            profilesRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val name = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)
                    val profileId = snapshot.key

                    if (profileId != null) {
                        // Create a layout similar to the main user and add user UI
                        val profileLayout = LayoutInflater.from(context)
                            .inflate(R.layout.user_profile_item, dynamicProfilesContainer, false) as ViewGroup

                        val profilePicture: CircleImageView = profileLayout.findViewById(R.id.profile_picture)
                        val profileName: TextView = profileLayout.findViewById(R.id.profile_name)

                        // Set profile name
                        profileName.text = name

                        // Load profile image if available
                        if (!profileImageUrl.isNullOrEmpty()) {
                            loadImageFromUrl(profileImageUrl, profilePicture)
                        }

                        // Make profile clickable
                        profilePicture.setOnClickListener {
                            val userAccountFragment = user_account()
                            val bundle = Bundle().apply { putString("profileId", profileId) }
                            userAccountFragment.arguments = bundle

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, userAccountFragment)
                                .addToBackStack(null)
                                .commit()
                        }

                        // Add the dynamic profile layout to the container before the "Add User" button
                        dynamicProfilesContainer.addView(profileLayout, dynamicProfilesContainer.childCount)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load profiles: ${error.message}", Toast.LENGTH_SHORT).show()
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
}

