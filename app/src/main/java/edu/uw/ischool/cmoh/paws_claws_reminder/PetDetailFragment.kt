package edu.uw.ischool.cmoh.paws_claws_reminder

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class PetDetailFragment : Fragment() {

    private lateinit var petImageView: ImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var petAgeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化控件
        petImageView = view.findViewById(R.id.img_bella)
        petNameTextView = view.findViewById(R.id.tv_bella)
        petTypeTextView = view.findViewById(R.id.tv_pet_type)
        petAgeTextView = view.findViewById(R.id.tv_pet_age)

        // 获取传递过来的宠物名称
        val petName = arguments?.getString("petName")
        if (petName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Pet name is missing!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // 返回上一个 Fragment
            return
        }

        // 根据宠物名称加载数据
        val pet = PetRepository.getPetByName(petName)
        pet?.let {
            if (it.photoUri.isNotEmpty()) {
                petImageView.setImageURI(Uri.parse(it.photoUri))
            } else {
                petImageView.setImageResource(R.drawable.ic_pet_placeholder)
            }
            petNameTextView.text = it.name
            petTypeTextView.text = it.type
            petAgeTextView.text = it.dob
        } ?: run {
            Toast.makeText(requireContext(), "Pet not found!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
