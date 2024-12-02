package edu.uw.ischool.cmoh.paws_claws_reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask

data class Task(
    val id: Int,
    val name: String,
    val endDateTime: LocalDateTime,
    val userID: Int,
    var isChecked: Boolean = false
)

data class Reminder(
    val reminderId: Int,
    val reminderName: String,
    val minuteBeforeDeadline: Int?,
    val reminderRepeat: String,
    val phoneNumber: Int,
    val endDateTime: LocalDateTime,
)


class ReminderActivity : AppCompatActivity() {
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var recyclerView: RecyclerView
    lateinit var phoneNumberInput: EditText
    private val selectedTasks = mutableListOf<Task>()
    lateinit var deadlineReminder: EditText
    lateinit var reminderIntervalSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reminders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkSmsPermission()
        setContentView(R.layout.activity_reminders)
        recyclerView = findViewById(R.id.Task_list)
        phoneNumberInput = findViewById(R.id.phone_input)
        deadlineReminder = findViewById(R.id.reminderBeforeDeadline)
        reminderIntervalSpinner = findViewById(R.id.reminder_interval_spinner)
        recyclerView = findViewById(R.id.Task_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tasks.add(Task(1, "Buy groceries", LocalDateTime.now().plusHours(1),1))
        tasks.add(Task(2, "Submit project", LocalDateTime.now().minusHours(2),1))
        tasks.add(Task(3, "Doctor's appointment", LocalDateTime.now().plusDays(1),1))
        tasks.add(Task(4, "Buy food", LocalDateTime.now().plusHours(1),1))
        tasks.add(Task(5, "Submit final ", LocalDateTime.now().minusHours(2),1))
        tasks.add(Task(6, "pet's appointment", LocalDateTime.now().plusDays(1),1))
        tasks.add(Task(7, "Buy ice cream", LocalDateTime.now().plusHours(1),2))
        tasks.add(Task(8, "Submit final paper ", LocalDateTime.now().minusHours(2),2))
        tasks.add(Task(9, "appointment", LocalDateTime.now().plusDays(1),2))
        tasks.add(Task(10, "testing", LocalDateTime.now(),2))
        tasks.add(Task(10, "testing reminder before deadline", LocalDateTime.now().plusMinutes(5),2))

        val userId = intent.getIntExtra("userId", -1)
        val filteredTasks: MutableList<Task>  = tasks.filter { it.userID == userId }.toMutableList()
        taskAdapter = TaskAdapter(filteredTasks) { task ->
            onTaskSelectionChanged(task)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            Log.i("MainActivity", "Save button clicked")
            createReminders()
        }
    }

    private fun onTaskSelectionChanged(task: Task) {
        if (task.isChecked) {
            if (!selectedTasks.contains(task)) {
                selectedTasks.add(task)
            }
        } else {
            selectedTasks.remove(task)
        }
    }
    private fun createReminders() {
        val now = LocalDateTime.now()
        val reminders = selectedTasks.map { task ->
            val minuteBeforeDeadline = if (now.isAfter(task.endDateTime)) {
                null
            } else {
                deadlineReminder.text.toString().toIntOrNull()
            }
            Reminder(
                reminderId = task.id,
                reminderName = task.name,
                minuteBeforeDeadline = minuteBeforeDeadline,
                reminderRepeat = reminderIntervalSpinner.selectedItem.toString(),
                phoneNumber = phoneNumberInput.text.toString().toIntOrNull() ?: 0,
                endDateTime = task.endDateTime
            )
        }
        reminders.forEach { reminder ->
            Log.i("MainActivity", "Created reminder: $reminder")
            scheduleSmsReminder(reminder)
        }
    }
    private fun scheduleSmsReminder(reminder: Reminder) {
        val timer = Timer()
        val handler = Handler()
        fun scheduleTask() {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        sendSMS(reminder.phoneNumber.toString(), "Reminder: ${reminder.reminderName}")
                    }
                    when (reminder.reminderRepeat) {
                        "Minute" -> handler.postDelayed({ scheduleTask() }, 60 * 1000L)
                        "Hourly" -> handler.postDelayed({ scheduleTask() }, 60 * 60 * 1000L)
                        "Daily" -> handler.postDelayed({ scheduleTask() }, 24 * 60 * 60 * 1000L)
                        "Weekly" -> handler.postDelayed({ scheduleTask() }, 7 * 24 * 60 * 60 * 1000L)
                        "Monthly" -> handler.postDelayed({ scheduleTask() }, 30L * 24 * 60 * 60 * 1000L)
                    }
                }
            }, reminder.minuteBeforeDeadline?.toLong() ?: 0)
        }
        scheduleTask()
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }
    private fun checkSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permission not granted!")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.SEND_SMS), 1
            )
        } else {
            Log.d("MainActivity", "Permission granted!")
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permission granted!")
                } else {
                    Log.d("MainActivity", "Failed to obtain permission")
                    Toast.makeText(
                        this,
                        "Failed to obtain SMS permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

