package edu.uw.ischool.cmoh.paws_claws_reminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.time.LocalDateTime

class TaskAdapterTasks(private val context: Context, private val tasks: List<TaskData>) : ArrayAdapter<TaskData>(context, 0, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        val taskNameTextView = view.findViewById<TextView>(R.id.task_name)
        val taskDetailsTextView = view.findViewById<TextView>(R.id.task_details)
        var petNames = task?.petName?.joinToString(", ")
        val taskHeader = "${petNames}: ${task?.taskName}"
        val taskDetails = "${task?.type} \nDetails: ${task?.details} \nTime: ${task?.time} \nStart Date: ${task?.startDate}" +
                "\nRepeats: ${task?.repeats} \nEnd Date:${task?.endDate} "

        taskNameTextView.text = taskHeader
        taskDetailsTextView.text = taskDetails

        return view
    }
}
