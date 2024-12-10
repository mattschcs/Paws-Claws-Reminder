package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.util.Locale

class AddTask : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference
    private val petsDatabase = FirebaseDatabase.getInstance().getReference("pets")
    private val yourPetNames = ArrayList<String>()
    private val selectedNamesList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPetNamesFromDatabase()
        val petContainer: LinearLayout = view.findViewById(R.id.pets_container)
        addPetCheckboxes(petContainer)

        auth = FirebaseAuth.getInstance()


        val taskName: EditText = view.findViewById(R.id.task_name)
        val typeSpinner: Spinner =  view.findViewById(R.id.task_type_spinner)
        val details: EditText = view.findViewById(R.id.additional_details)
        val startCalendar: CalendarView = view.findViewById(R.id.start_calender)
        val time: EditText = view.findViewById(R.id.editTextTime)
        val repeatsSpinner: Spinner =  view.findViewById(R.id.repeats_spinner)
        val endCalendar: CalendarView = view.findViewById(R.id.end_calender)
        val finishTaskButton: Button = view.findViewById(R.id.create_task)

        val types = resources.getStringArray(R.array.TaskType)
        val repeats = resources.getStringArray(R.array.TaskRepeats)

        if(typeSpinner != null){
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, types)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = adapter
        }

        if(repeatsSpinner != null){
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, repeats)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Optional: For dropdown style
            repeatsSpinner.adapter = adapter
        }
        var startDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(startCalendar.date)
        var endDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(endCalendar.date)

        startCalendar.setOnDateChangeListener(CalendarView.OnDateChangeListener { _, year, month, day ->
            startDate = String.format("%04d-%02d-%02d", year, month + 1, day)
        })
        endCalendar.setOnDateChangeListener(CalendarView.OnDateChangeListener { _, year, month, day ->
            endDate = String.format("%04d-%02d-%02d", year, month + 1, day)
        })

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        finishTaskButton.setOnClickListener{
            val taskNameInput = taskName.text.toString()
            val typeSpinnerInput = typeSpinner.selectedItem.toString()
            val detailsInput = details.text.toString()

            val timeInput = time.text.toString()
            val repeatsSpinnerInput = repeatsSpinner.selectedItem.toString()

            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid
            val taskId = database.child("tasks").push().key
            val createdDate = LocalDateTime.now().toString()


            val startDateObj = dateFormat.parse(startDate)
            val endDateObj = dateFormat.parse(endDate)

            if(startDateObj <= endDateObj){
                if (userId != null) {
                    if (taskId != null) {
                        saveTaskToDatabase(
                            taskId,
                            userId,
                            selectedNamesList,
                            taskNameInput,
                            typeSpinnerInput,
                            detailsInput,
                            startDate,
                            timeInput,
                            repeatsSpinnerInput,
                            endDate,
                            false,
                            createdDate
                        )
                    }
                }

            } else{
                //make toast
                Toast.makeText(requireContext(), "End date can not be before start date", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadPetNamesFromDatabase(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        petsDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                yourPetNames.clear()
                for (petSnapshot in snapshot.children) {
                    val pet = petSnapshot.getValue(PetModel::class.java)
                    val petId = petSnapshot.key ?: ""

                    if (pet != null) {
                        yourPetNames.add(pet.petName) // Assuming PetModel has a "name" property
                        Log.d("PetLoaded", "Loaded pet: ${pet.petName} with ID: $petId")
                    } else {
                        Log.e("DatabaseError", "Failed to parse pet data: ${petSnapshot.value}")
                    }
                }
                // Find the container and populate checkboxes
                val petsContainer = view?.findViewById<LinearLayout>(R.id.pets_container)
                petsContainer?.let { addPetCheckboxes(it) }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Failed to load pets: ${error.message}")
            }
        })
    }

    private fun addPetCheckboxes(container: LinearLayout) {
        // Clear existing checkboxes if necessary
        container.removeAllViews()

        for (petName in yourPetNames) {
            val checkBox = CheckBox(requireContext()).apply {
                text = petName
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Update selectedPetNames based on checkbox state
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedNamesList.add(petName)
                    Log.d("PetSelection", "$petName added to selected list")
                } else {
                    selectedNamesList.remove(petName)
                    Log.d("PetSelection", "$petName removed from selected list")
                }
            }

            // Add the checkbox to the container
            container.addView(checkBox)
        }
    }

    private fun saveTaskToDatabase(
        taskId: String,
        userId: String,
        petName: ArrayList<String>,
        taskName: String,
        type: String,
        details: String,
        startDate: String,
        time: String,
        repeats: String,
        endDate: String,
        checked: Boolean,
        lastChecked: String
    ) {
        val taskList = mapOf(
            "taskId" to taskId,
            "userId" to userId,
            "petName" to petName,
            "taskName" to taskName,
            "type" to type,
            "details" to details,
            "startDate" to startDate,
            "time" to time,
            "repeats" to repeats,
            "endDate" to endDate,
            "checked" to checked,
            "lastChecked" to lastChecked
        )

        database.child("tasks").child(userId).child(taskId).setValue(taskList).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main, LogIn())
                    .addToBackStack(null)
                    .commit()
                parentFragmentManager.beginTransaction().replace(R.id.main, Task()).commit()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to save task data: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        fun newInstance() =
            AddTask().apply {
            }
    }
}