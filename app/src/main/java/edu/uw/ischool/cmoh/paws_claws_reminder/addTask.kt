package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class addTask : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val finishTaskButton: Button = view.findViewById(R.id.create_task)
        val typeSpinner: Spinner =  view.findViewById(R.id.task_type_spinner)
        val repeatsSpinner: Spinner =  view.findViewById(R.id.repeats_spinner)
        val types = resources.getStringArray(R.array.TaskType)
        val repeats = resources.getStringArray(R.array.TaskRepeats)

        if(typeSpinner != null){
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, types)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = adapter
        }

        if(repeatsSpinner != null){
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, repeats)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Optional: For dropdown style
            repeatsSpinner.adapter = adapter
        }

        finishTaskButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.frame_layout, Task()).commit()
        }
    }

    companion object {
        fun newInstance() =
            addTask().apply {
            }
    }
}