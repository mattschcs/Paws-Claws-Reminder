package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddTask : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

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

        auth = FirebaseAuth.getInstance()
        val namesList = ArrayList<String>()

        val petName: CheckBox = view.findViewById(R.id.checkBox4)
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

        petName.setOnClickListener{
            if (petName.isChecked){
                namesList.add(petName.text.toString())
            } else {
                namesList.remove(petName.text.toString())
            }
        }

        finishTaskButton.setOnClickListener{
            val taskNameInput = taskName.text.toString()
            val typeSpinnerInput = typeSpinner.selectedItem.toString()
            val detailsInput = details.text.toString()
            val startDate = startCalendar.date.toString()
            val timeInput = time.text.toString()
            val repeatsSpinnerInput = repeatsSpinner.selectedItem.toString()
            val endDate = endCalendar.date.toString()

            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid
            val taskId = database.child("tasks").push().key

            if (userId != null) {
                if (taskId != null) {
                    saveTaskToDatabase(taskId, userId, namesList, taskNameInput, typeSpinnerInput, detailsInput, startDate,
                        timeInput, repeatsSpinnerInput, endDate, false)
                }
            }
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
        checked: Boolean
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
            "checked" to checked
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