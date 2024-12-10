package edu.uw.ischool.cmoh.paws_claws_reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PetAdapter(private var tasks: List<TaskModel>) :
    RecyclerView.Adapter<PetAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskDetails: TextView = itemView.findViewById(R.id.task_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.taskName
        holder.taskDetails.text = task.details
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskModel>) {
        tasks = newTasks
        notifyDataSetChanged() // 通知 RecyclerView 数据已更改
    }
}
