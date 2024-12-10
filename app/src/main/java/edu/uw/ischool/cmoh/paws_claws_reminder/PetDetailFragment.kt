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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PetDetailFragment : Fragment() {

    private lateinit var petImageView: ImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var petDobTextView: TextView
    private lateinit var petRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter

    private val database = FirebaseDatabase.getInstance().reference
    private var userId: String? = null
    private val tag = "PetDetailFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petImageView = view.findViewById(R.id.img_pet)
        petNameTextView = view.findViewById(R.id.tv_pet_name)
        petTypeTextView = view.findViewById(R.id.tv_pet_type)
        petDobTextView = view.findViewById(R.id.tv_pet_age)
        petRecyclerView = view.findViewById(R.id.rv_task_list)

        petRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        petAdapter = PetAdapter(emptyList())
        petRecyclerView.adapter = petAdapter

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            showError("User not logged in.")
            return
        }

        val petName = arguments?.getString("petName")
        if (petName.isNullOrEmpty()) {
            showError("Pet Name is missing!")
            return
        }

        Log.d(tag, "User ID: $userId, Pet Name: $petName")

        // 调试 tasks 节点完整内容
        debugFullTasksNode()

        loadPetDetails(userId!!, petName)
        loadTasksForPet(userId!!, petName)
    }

    private fun loadPetDetails(userId: String, petName: String) {
        Log.d(tag, "Loading pet details for userId: $userId, petName: $petName")
        database.child("pets").child(userId).orderByChild("petName").equalTo(petName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "Full pet snapshot: ${snapshot.value}")
                    Log.d(tag, "Snapshot key: ${snapshot.key}, children count: ${snapshot.childrenCount}")
                    for (petSnapshot in snapshot.children) {
                        Log.d(tag, "Inspecting pet snapshot: ${petSnapshot.key}, value: ${petSnapshot.value}")
                        val pet = petSnapshot.getValue(PetModel::class.java)
                        if (pet != null) {
                            Log.d(tag, "Mapped pet: $pet")
                            updateUI(pet)
                        } else {
                            Log.e(tag, "Pet data is null for key: ${petSnapshot.key}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(tag, "Failed to load pet details: ${error.message}", error.toException())
                    showError("Failed to load pet details: ${error.message}")
                }
            })
    }


    private fun loadTasksForPet(userId: String, petName: String) {
        database.child("tasks").get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Log.w(tag, "No tasks found in the database.")
                return@addOnSuccessListener
            }

            val tasks = mutableListOf<TaskModel>()
            for (taskSnapshot in snapshot.children) {
                val task = taskSnapshot.getValue(TaskModel::class.java)
                if (task != null && task.userId == userId) {
                    // 检查 petName 是否匹配
                    if (task.petName != null && task.petName.contains(petName)) {
                        tasks.add(task)
                        Log.d(tag, "Task added: $task")
                    }
                }
            }

            Log.d(tag, "Total tasks loaded for pet: ${tasks.size}")
        }.addOnFailureListener {
            Log.e(tag, "Failed to load tasks: ${it.message}")
        }
    }




    private fun debugFullTasksNode() {
        database.child("tasks").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d(tag, "Full tasks node: ${snapshot.value}")
                for (child in snapshot.children) {
                    Log.d(tag, "Child key: ${child.key}, value: ${child.value}")
                }
            } else {
                Log.w(tag, "No data found in tasks node.")
            }
        }.addOnFailureListener { error ->
            Log.e(tag, "Error fetching tasks node: ${error.message}")
        }
    }




    private fun updateUI(pet: PetModel) {
        Log.d(tag, "Updating UI with pet data: $pet")
        petNameTextView.text = pet.petName
        petTypeTextView.text = pet.type
        petDobTextView.text = pet.dob.ifEmpty {
            Log.d(tag, "DOB is empty for pet: ${pet.petName}")
            "N/A" // Default placeholder for empty DOB
        }
        if (pet.photoUri.isNotEmpty()) {
            Log.d(tag, "Setting photo URI for pet: ${pet.photoUri}")
            petImageView.setImageURI(Uri.parse(pet.photoUri))
        } else {
            Log.d(tag, "Setting default photo for pet: ${pet.petName}")
            petImageView.setImageResource(R.drawable.ic_pet_placeholder)
        }
    }

    private fun showError(message: String) {
        Log.e(tag, message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
