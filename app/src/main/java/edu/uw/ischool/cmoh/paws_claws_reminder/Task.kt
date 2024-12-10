package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Task : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskListView: ListView = view.findViewById(R.id.task_list)
        val addTaskButton: Button = view.findViewById(R.id.add_task)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        val tasksList = mutableListOf<TaskData>()
        val adapter = TaskAdapterTasks(requireContext(), tasksList)
        taskListView.adapter = adapter

        val database = FirebaseDatabase.getInstance()
        val userTasksRef = userId?.let { database.getReference("tasks").child(it) }

        userTasksRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tasksList.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(TaskData::class.java)
                    if (task != null) {
                        tasksList.add(task)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load tasks", error.toException())
            }
        })
        addTaskButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.main, AddTask()).commit()
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Task().apply {
            }
    }
}

data class TaskData(
    val taskId: String = "",
    val userId: String = "",
    val petName: ArrayList<String> = arrayListOf(),
    val taskName: String = "",
    val type: String = "",
    val details: String = "",
    val startDate: String = "",
    val time: String = "",
    val repeats: String = "",
    val endDate: String = "",
    var checked: Boolean = false,
    var lastChecked: String = ""
)