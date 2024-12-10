package edu.uw.ischool.cmoh.paws_claws_reminder

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class PetsMainFragment : Fragment() {

    private lateinit var petGrid: GridLayout
    private lateinit var database: DatabaseReference
    private val tag = "Pets_MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petGrid = view.findViewById(R.id.pet_grid)
        database = FirebaseDatabase.getInstance().getReference("pets")

        loadPetsFromFirebase()
    }

    private fun loadPetsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.e(tag, "No pets found.")
                    addCreateButton() // 确保即使没有宠物也会显示创建按钮
                    return
                }
                petGrid.removeAllViews()
                for (petSnapshot in snapshot.children) {
                    val pet = petSnapshot.getValue(PetModel::class.java)
                    if (pet != null) {
                        addPetToGrid(pet, petSnapshot.key ?: "")
                    } else {
                        Log.e(tag, "Failed to parse pet data: ${petSnapshot.value}")
                    }
                }
                addCreateButton()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Failed to load pets: ${error.message}")
            }
        })
    }



    private fun addPetToGrid(pet: PetModel, petId: String) {
        val petImage = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { setMargins(8) }
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

        petContainer.setOnClickListener {
            val bundle = Bundle().apply {
                putString("petId", petId)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, PetDetailFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
        }

        petGrid.addView(petContainer)
    }

    private fun addCreateButton() {
        val createButton = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { setMargins(8) }
            setBackgroundResource(R.drawable.circle_background)
            setImageResource(R.drawable.ic_add)
        }

        val createText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = getString(R.string.create_pet)
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, CreatePetFragment())
                .addToBackStack(null)
                .commit()
        }

        petGrid.addView(createContainer)
    }
}
