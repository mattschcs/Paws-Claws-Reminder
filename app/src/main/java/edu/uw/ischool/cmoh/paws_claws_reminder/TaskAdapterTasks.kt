package edu.uw.ischool.cmoh.paws_claws_reminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskAdapterTasks(private val context: Context, private val tasks: List<TaskData>) : ArrayAdapter<TaskData>(context, 0, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

                val taskNameTextView = view.findViewById<TextView>(R.id.task_name)
                val taskDetailsTextView = view.findViewById<TextView>(R.id.task_details)
                val petNames = task?.petName?.joinToString(", ")
                val taskHeader = "${petNames}: ${task?.taskName}"
                val taskDetails = "${task?.type} \nDetails: ${task?.details} \nTime: ${task?.time} \nStart Date: ${task?.startDate}" +
                        "\nRepeats: ${task?.repeats} \nEnd Date: ${task?.endDate} \nLast Updated: ${task?.lastChecked}"
                val taskCheckBox: CheckBox = view.findViewById(R.id.itemCheckbox)

                taskNameTextView.text = taskHeader
                taskDetailsTextView.text = taskDetails
        if (task != null) {
            taskCheckBox.isChecked = task.checked
        }

                taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (task != null) {
                        task.checked = isChecked
                    }
                    val database = FirebaseDatabase.getInstance()
                    val userTasksRef = task.let { it?.userId?.let { it1 ->
                        task?.taskId?.let { it2 ->
                            database.getReference("tasks").child(
                                it1
                            ).child(it2)
                        }
                    } }
                    val currentDate = LocalDateTime.now().toString()

                    userTasksRef?.updateChildren(mapOf("checked" to isChecked))
                    userTasksRef?.updateChildren(mapOf("lastChecked" to currentDate))
                }
                return view
    }
}
