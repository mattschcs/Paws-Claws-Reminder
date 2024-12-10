package edu.uw.ischool.cmoh.paws_claws_reminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class Task : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addTaskButton: Button = view.findViewById(R.id.add_task)

        addTaskButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.main, AddTask()).commit()
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Task().apply {
            }
    }
}