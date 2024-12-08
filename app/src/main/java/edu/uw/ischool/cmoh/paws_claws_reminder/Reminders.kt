package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask

data class TaskToDo(
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Reminders.newInstance] factory method to
 * create an instance of this fragment.
 */
class Reminders : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<TaskToDo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var phoneNumberInput: EditText
    private val selectedTasks = mutableListOf<TaskToDo>()
    private lateinit var deadlineReminder: EditText
    private lateinit var reminderIntervalSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkSmsPermission()
        val view =  inflater.inflate(R.layout.fragment_reminders, container, false)
        recyclerView = view?.findViewById(R.id.Task_list) ?: throw NullPointerException("RecyclerView not found")
        phoneNumberInput = view?.findViewById(R.id.phone_input) ?: throw NullPointerException("Phone input not found")
        deadlineReminder = view?.findViewById(R.id.reminderBeforeDeadline) ?: throw NullPointerException("Deadline reminder not found")
        reminderIntervalSpinner = view?.findViewById(R.id.reminder_interval_spinner) ?: throw NullPointerException("Reminder interval spinner not found")
        tasks.add(TaskToDo(1, "Buy groceries", LocalDateTime.now().plusHours(1),1))
        tasks.add(TaskToDo(2, "Submit project", LocalDateTime.now().minusHours(2),1))
        tasks.add(TaskToDo(3, "Doctor's appointment", LocalDateTime.now().plusDays(1),1))
        tasks.add(TaskToDo(4, "Buy food", LocalDateTime.now().plusHours(1),1))
        tasks.add(TaskToDo(5, "Submit final ", LocalDateTime.now().minusHours(2),1))
        tasks.add(TaskToDo(6, "pet's appointment", LocalDateTime.now().plusDays(1),1))
        tasks.add(TaskToDo(7, "Buy ice cream", LocalDateTime.now().plusHours(1),2))
        tasks.add(TaskToDo(8, "Submit final paper ", LocalDateTime.now().minusHours(2),2))
        tasks.add(TaskToDo(9, "appointment", LocalDateTime.now().plusDays(1),2))
        tasks.add(TaskToDo(10, "testing", LocalDateTime.now(),2))
        tasks.add(TaskToDo(10, "testing reminder before deadline", LocalDateTime.now().plusMinutes(5),2))
        val userId = arguments?.getInt("userId", -1) ?: -1
        val filteredTasks = tasks.filter { it.userID == userId }.toMutableList()

        taskAdapter = TaskAdapter(filteredTasks) { task ->
            onTaskSelectionChanged(task)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = taskAdapter

        val saveButton: Button = view.findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            Log.i("ReminderFragment", "Save button clicked")
            createReminders()
        }

        return view
    }

    private fun onTaskSelectionChanged(task: TaskToDo) {
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
                requireContext().getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }
    private fun checkSmsPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d("ReminderFragment", "Permission not granted!")
            requestPermissions(
                arrayOf(Manifest.permission.SEND_SMS), 1
            )
        } else {
            Log.d("ReminderFragment", "Permission granted!")
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ReminderFragment", "Permission granted!")
                } else {
                    Log.d("ReminderFragment", "Failed to obtain permission")
                    Toast.makeText(
                        requireContext(),
                        "Failed to obtain SMS permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Reminders.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Reminders().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}