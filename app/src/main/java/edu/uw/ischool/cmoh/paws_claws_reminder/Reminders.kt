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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Timer
import java.util.TimerTask

data class TaskToDo(
    val id: Int,
    val name: String,
    val endDateTime: LocalDateTime,
    val userID: String,
    var isChecked: Boolean = false
)
data class Reminder(
    val reminderId: Int = 0,  // Default value
    val reminderName: String = "",  // Default value
    val minuteBeforeDeadline: Int? = null,  // Default value
    val reminderRepeat: String = "",  // Default value
    val phoneNumber: String = "",  // Default value
    val endDateTime: String = "",  // Default value
    val userID: String = ""  // Default value
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
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<TaskToDo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var phoneNumberInput: EditText
    private val selectedTasks = mutableListOf<TaskToDo>()
    private lateinit var deadlineReminder: EditText
    private lateinit var reminderIntervalSpinner: Spinner
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var save_button: Button
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSmsPermission()
        phoneNumberInput = view.findViewById(R.id.phone_input)
        save_button = view.findViewById(R.id.save_button)
        recyclerView = view.findViewById(R.id.Task_list)
        phoneNumberInput = view.findViewById(R.id.phone_input)
        deadlineReminder = view.findViewById(R.id.reminderBeforeDeadline)
        reminderIntervalSpinner = view.findViewById(R.id.reminder_interval_spinner)
        val phoneNumberbeforeclick = phoneNumberInput.text.toString()
        Log.i("ReminderFragment", "phoneNumberbeforeclick: $phoneNumberbeforeclick")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        Log.i("ReminderFragment", "user ID ${userId}")
        loadTaskFromDatabase(userId.toString())
        loadRemindersFromDatabase(userId.toString())
        val filteredTasks = tasks.filter { it.userID == userId }.toMutableList()
        taskAdapter = TaskAdapter(mutableListOf()) { task ->
            onTaskSelectionChanged(task)
        }
//        taskAdapter = TaskAdapter(tasks.filter { it.userID == userId }.toMutableList()) { task ->
//            onTaskSelectionChanged(task)
//        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = taskAdapter

        val saveButton: Button = view.findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString()
            val updatedPhoneNumber = phoneNumber
            Log.i("ReminderFragment", "Phone Number right now: $phoneNumber")
            if (phoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show()
            } else {
                Log.i("ReminderFragment", "$phoneNumber")
                Log.i("ReminderFragment", "Phone Number entered: $phoneNumber")
            }
            Log.i("ReminderFragment", "Save button clicked")

            createReminders(updatedPhoneNumber)
        }
    }

    private fun loadTaskFromDatabase(userId: String) {
        val taskRef = database.child("tasks").child(userId)
        Log.i("ReminderFragment", "task Ref : $taskRef")
        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasksList = mutableListOf<TaskToDo>()
                for (taskSnapshot in dataSnapshot.children) {
                    val taskDetails = taskSnapshot.value as? Map<String, Any>?
                    val taskId = taskDetails?.get("taskId") as String
                    val taskName = taskDetails["taskName"] as String
                    val endDateMillis = (taskDetails["endDate"] as String).toLongOrNull()

                    val endDateTime = LocalDateTime.ofInstant(endDateMillis?.let {
                        Instant.ofEpochMilli(
                            it
                        )
                    }, ZoneId.systemDefault())
                    Log.i("ReminderFragment", " taskDetailsSnapshot : $taskDetails")
                    val taskToDo = TaskToDo(
                        id = taskId.hashCode(),
                        name = taskName,
                        endDateTime = endDateTime,
                        userID = userId,
                        isChecked = false
                    )
                    tasks.add(taskToDo)
                }
                Log.i("ReminderFragment", "entire task: ${tasks}")
                val filteredTasks = tasks.filter { it.userID == userId }.toMutableList()
                taskAdapter.updateTasks(filteredTasks)
                tasksList.forEach { task ->
                    Log.i("ReminderFragment", "Loaded task: ${task.name}, EndDate: ${task.endDateTime}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReminderFragment", "Failed to load tasks: ${error.message}")
            }
        })
    }


    private fun loadRemindersFromDatabase(userId: String) {
        val remindersRef = database.child("reminders").child(userId)
        Log.i("ReminderFragment", "reminder Ref : $remindersRef")
        remindersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val remindersList = mutableListOf<Reminder>()
                Log.i("ReminderFragment", "inside the  override ")
                for (reminderSnapshot in dataSnapshot.children) {
                    val reminder = reminderSnapshot.getValue(Reminder::class.java)
                    Log.i("ReminderFragment", "for loop : $reminder")
                    reminder?.let { remindersList.add(it) }
                }
                Log.i("ReminderFragment", "entire remindersList : $remindersList")
                remindersList.forEach { reminder ->
                    Log.i("ReminderFragment", "Loaded reminder: $reminder")
                    Log.i("ReminderFragment", "Repeat: ${reminder.reminderRepeat}")
                    scheduleSmsReminder(reminder)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReminderFragment", "Failed to load reminders: ${error.message}")
            }
        })
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

    private fun createReminders(updatedPhoneNumber: String) {
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
                phoneNumber = updatedPhoneNumber,
                endDateTime = task.endDateTime.toString(),
                userID = task.userID
            )
        }
        reminders.forEach { reminder ->
            Log.i("MainActivity", "Created reminder: $reminder")
            scheduleSmsReminder(reminder)
            saveReminderToDatabase(reminder)
        }
    }

    private fun saveReminderToDatabase(reminder: Reminder) {
        val userId = reminder.userID
        val reminderId = reminder.reminderId.toString()

        val reminderMap = mapOf(
            "reminderId" to reminder.reminderId,
            "reminderName" to reminder.reminderName,
            "minuteBeforeDeadline" to reminder.minuteBeforeDeadline,
            "reminderRepeat" to reminder.reminderRepeat,
            "phoneNumber" to reminder.phoneNumber,
            "endDateTime" to reminder.endDateTime,
            "userID" to reminder.userID
        )

        val remindersRef = database.child("reminders").child(userId).child(reminderId)

        remindersRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val existingReminder = dataSnapshot.value as Map<String, Any?>
                if (existingReminder == reminderMap) {
                    Log.i("ReminderFragment", "Reminder is identical; no update needed.")
                } else {

                    remindersRef.setValue(reminderMap)
                        .addOnSuccessListener {
                            Log.i("ReminderFragment", "Reminder updated successfully for user $userId.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ReminderFragment", "Failed to update reminder for user $userId.", e)
                        }
                }
            } else {

                remindersRef.setValue(reminderMap)
                    .addOnSuccessListener {
                        Log.i("ReminderFragment", "Reminder added successfully for user $userId.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReminderFragment", "Failed to add reminder for user $userId.", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("ReminderFragment", "Error checking for existing reminder.", e)
        }
    }

    private fun scheduleSmsReminder(reminder: Reminder) {
        val timer = Timer()
        val handler = Handler()

        fun scheduleTask() {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        sendSMS(reminder.phoneNumber, "Reminder: ${reminder.reminderName}")
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
        Log.d("ReminderFragment", "SmS function!")
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
            PackageManager.PERMISSION_GRANTED
        ) {
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
}