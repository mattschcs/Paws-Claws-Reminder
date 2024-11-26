package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime

data class Task(
    val id: Int,
    val name: String,
    val endDateTime: LocalDateTime,
    var isChecked: Boolean = false
)

class MainActivity : AppCompatActivity() {
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var recyclerView: RecyclerView
    lateinit var phoneNumberInput: EditText
    lateinit var deadlineReminder: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_tasks)
        recyclerView = findViewById(R.id.Task_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tasks.add(Task(1, "Buy groceries", LocalDateTime.now().plusHours(1)))
        tasks.add(Task(2, "Submit project", LocalDateTime.now().minusHours(2)))
        tasks.add(Task(3, "Doctor's appointment", LocalDateTime.now().plusDays(1)))

        taskAdapter = TaskAdapter(tasks) { task ->
            taskAdapter.onTaskRemoved(task)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter
    }


}