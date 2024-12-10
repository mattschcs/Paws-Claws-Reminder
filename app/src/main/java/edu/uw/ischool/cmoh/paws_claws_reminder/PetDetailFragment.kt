package edu.uw.ischool.cmoh.paws_claws_reminder

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PetDetailFragment : Fragment() {

    private lateinit var petImageView: ImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var petDobTextView: TextView

    private val database = FirebaseDatabase.getInstance().reference
    private var userId: String? = null // User ID
    private val tag = "PetDetailFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petImageView = view.findViewById(R.id.img_bella)
        petNameTextView = view.findViewById(R.id.tv_bella)
        petTypeTextView = view.findViewById(R.id.tv_pet_type)
        petDobTextView = view.findViewById(R.id.tv_pet_age)

        // Get the current userId
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            showError("User not logged in.")
            return
        }

        val petId = arguments?.getString("petId")
        if (petId.isNullOrEmpty()) {
            showError("Pet ID is missing!")
        } else {
            loadPetDetails(userId!!, petId)
        }
    }

    private fun loadPetDetails(userId: String, petId: String) {
        database.child("pets").child(userId).child(petId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val pet = snapshot.getValue(PetModel::class.java)
                        if (pet != null) {
                            updateUI(pet)
                        } else {
                            showError("Failed to parse pet details.")
                        }
                    } else {
                        showError("Pet not found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Error loading pet details: ${error.message}")
                }
            })
    }

    private fun updateUI(pet: PetModel) {
        petNameTextView.text = pet.name
        petTypeTextView.text = pet.type
        petDobTextView.text = pet.dob
        if (pet.photoUri.isNotEmpty()) {
            petImageView.setImageURI(Uri.parse(pet.photoUri))
        } else {
            petImageView.setImageResource(R.drawable.ic_pet_placeholder)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
