package edu.uw.ischool.cmoh.paws_claws_reminder

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.util.Log


class PetDetailFragment : Fragment() {

    private lateinit var petImageView: ImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var petAgeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PetDetailFragment", "Fragment loaded with arguments: $arguments")

        petImageView = view.findViewById(R.id.img_bella)
        petNameTextView = view.findViewById(R.id.tv_bella)
        petTypeTextView = view.findViewById(R.id.tv_pet_type)
        petAgeTextView = view.findViewById(R.id.tv_pet_age)

        val petName = arguments?.getString("petName")
        if (petName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Pet name is missing!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val pet = PetRepository.getPetByName(petName)
        if (pet != null) {
            Log.d("PetDetailFragment", "Loaded pet details: $pet")
            if (pet.photoUri.isNotEmpty()) {
                petImageView.setImageURI(Uri.parse(pet.photoUri))
            } else {
                petImageView.setImageResource(R.drawable.ic_pet_placeholder)
            }
            petNameTextView.text = pet.name
            petTypeTextView.text = pet.type
            petAgeTextView.text = pet.dob
        } else {
            Toast.makeText(requireContext(), "Pet not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
