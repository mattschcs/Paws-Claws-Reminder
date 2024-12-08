package edu.uw.ischool.cmoh.paws_claws_reminder

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime

class TaskAdapter (
    private val tasks: MutableList<TaskToDo>,
    private val onTaskSelectionChanged: (TaskToDo) -> Unit
): RecyclerView.Adapter<TaskAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.taskCheckBox)
        val taskName: TextView = view.findViewById(R.id.TaskName)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_task, parent, false)
        return ReminderViewHolder(view)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name

        val currentTime = LocalDateTime.now()
        holder.checkbox.buttonTintList = ColorStateList.valueOf(
            if (currentTime.isAfter(task.endDateTime)) Color.parseColor("#800080")
            else Color.parseColor("#FFD700")
        )

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = task.isChecked
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            task.isChecked = isChecked
            onTaskSelectionChanged(task)
        }
    }

}