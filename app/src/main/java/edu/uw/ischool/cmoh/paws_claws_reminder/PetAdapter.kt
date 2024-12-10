package edu.uw.ischool.cmoh.paws_claws_reminder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PetAdapter(private var tasks: List<TaskModel>) :
    RecyclerView.Adapter<PetAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.tv_task_name)
        val taskDetails: TextView = itemView.findViewById(R.id.tv_task_details)
        val imgStatus: ImageView = itemView.findViewById(R.id.img_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        Log.d("PetAdapter", "Binding task: $task")
        holder.taskName.text = task.taskName
        holder.taskDetails.text = task.details

        // 动态设置状态图片
        if (task.checked) {
            holder.imgStatus.setImageResource(R.drawable.ic_check)
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_cross)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskModel>) {
        tasks = newTasks
        notifyDataSetChanged() // 通知 RecyclerView 数据已更改
    }
}
