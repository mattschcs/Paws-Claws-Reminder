package edu.uw.ischool.cmoh.paws_claws_reminder

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment

class Pets_MainFragment : Fragment() {

    private lateinit var petGrid: GridLayout
    private val TAG = "Pets_MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("PetDetailFragment", "Fragment loaded with arguments: ${arguments}")
        super.onViewCreated(view, savedInstanceState)

        // Initialize GridLayout
        petGrid = view.findViewById(R.id.pet_grid)

        // Load pet data
        loadPets()
    }

    private fun loadPets() {
        val pets = PetRepository.getAllPets()

        // Clear existing layout to avoid duplication
        if (petGrid.childCount > 0) {
            petGrid.removeAllViews()
        }

        // Dynamically add each pet
        pets.forEach { pet ->
            addPetToGrid(pet)
        }

        // Add the "Create" button at the end of the grid
        addCreateButton()
    }

    private fun addPetToGrid(pet: PetModel) {
        val petImage = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8)
            }
            setBackgroundResource(R.drawable.circle_background)
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (pet.photoUri.isNotEmpty()) {
                setImageURI(Uri.parse(pet.photoUri))
            } else {
                setImageResource(R.drawable.ic_pet_placeholder)
            }
        }

        val petName = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = pet.name
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

        val petContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                setMargins(16)
            }
            addView(petImage)
            addView(petName)
        }

        // Add click event
        petContainer.setOnClickListener {
            Log.d("Pets_MainFragment", "Clicked on pet: ${pet.name}")

            // Create a Bundle and pass data
            val bundle = Bundle().apply {
                putString("petName", pet.name)
            }

            val fragment = PetDetailFragment().apply {
                arguments = bundle
            }

            //  replace current Fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
        }

        petGrid.addView(petContainer)
    }


    private fun addCreateButton() {
        val createButton = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8)
            }
            setBackgroundResource(R.drawable.circle_background)
            setImageResource(R.drawable.ic_add)
        }

        val createText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Create"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

        val createContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                setMargins(16)
            }
            addView(createButton)
            addView(createText)
        }

        createButton.setOnClickListener {
            Log.d(TAG, "Create button clicked")
            val fragment = CreatePetFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
        }

        petGrid.addView(createContainer)
    }

    private fun navigateToPetDetail(petName: String) {
        try {
            Log.d(TAG, "Navigating to PetDetailFragment with pet name: $petName")
            val fragment = PetDetailFragment()
            val bundle = Bundle().apply {
                putString("petName", petName)
            }
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
            Log.d(TAG, "Navigation to PetDetailFragment successful.")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to PetDetailFragment: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        loadPets()
    }
}
