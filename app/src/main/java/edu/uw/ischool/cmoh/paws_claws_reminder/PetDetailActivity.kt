package edu.uw.ischool.cmoh.paws_claws_reminder

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class PetDetailActivity : AppCompatActivity() {
    private lateinit var petImageView: ImageView
    private lateinit var petNameTextView: TextView
    private lateinit var petTypeTextView: TextView
    private lateinit var petAgeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_detail)

        petImageView = findViewById(R.id.img_bella)
        petNameTextView = findViewById(R.id.tv_bella)
        petTypeTextView = findViewById(R.id.tv_pet_type)
        petAgeTextView = findViewById(R.id.tv_pet_age)

        val petName = intent.getStringExtra("petName")
        if (petName.isNullOrEmpty()) {
            Toast.makeText(this, "Pet name is missing!", Toast.LENGTH_SHORT).show()
            finish() // 结束当前 Activity
            return
        }
        val pet = PetRepository.getPetByName(petName)


        pet?.let {
            petImageView.setImageURI(Uri.parse(it.photoUri))
            petNameTextView.text = it.name
            petTypeTextView.text = it.type
            petAgeTextView.text = it.dob
        }
    }
}
