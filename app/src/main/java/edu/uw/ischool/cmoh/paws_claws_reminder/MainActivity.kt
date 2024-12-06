package edu.uw.ischool.cmoh.paws_claws_reminder

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins

class MainActivity : AppCompatActivity() {

    private lateinit var petGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 GridLayout
        petGrid = findViewById(R.id.pet_grid)

        // 加载宠物数据
        loadPets()
    }

    /**
     * 加载宠物数据并更新 GridLayout
     */
    private fun loadPets() {
        val pets = PetRepository.getAllPets()

        // 清空已有布局，避免重复添加
        if (petGrid.childCount > 0) {
            petGrid.removeAllViews()
        }

        // 动态添加每个宠物
        pets.forEach { pet ->
            addPetToGrid(pet)
        }

        // 添加 "Create" 按钮到 GridLayout 的最后
        addCreateButton()
    }

    /**
     * 将单个宠物添加到网格布局中
     */
    private fun addPetToGrid(pet: Pet) {
        // 宠物图片按钮
        val petImage = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(180, 180).apply {
                setMargins(8)
            }
            setBackgroundResource(R.drawable.circle_background)
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (pet.photoUri.isNotEmpty()) {
                setImageURI(Uri.parse(pet.photoUri))
            } else {
                setImageResource(R.drawable.ic_pet_placeholder)
            }
            setOnClickListener {
                val intent = Intent(this@MainActivity, PetDetailActivity::class.java).apply {
                    putExtra("petName", pet.name)
                }
                startActivity(intent)
            }
        }

        // 宠物名字文本
        val petName = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = pet.name
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

        // 宠物容器（垂直布局）
        val petContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                setMargins(16)
            }
        }
        petContainer.addView(petImage)
        petContainer.addView(petName)

        // 将容器添加到网格布局中
        petGrid.addView(petContainer)
    }

    /**
     * 添加 "Create" 按钮
     */
    private fun addCreateButton() {
        val createButton = ImageButton(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 180
                height = 180
                setMargins(16)
            }
            setBackgroundResource(R.drawable.circle_background)
            setImageResource(R.drawable.ic_add)
            setOnClickListener {
                // 跳转到创建宠物页面
                val intent = Intent(this@MainActivity, CreatePetActivity::class.java)
                startActivity(intent)
            }
        }

        // 将按钮添加到网格布局
        petGrid.addView(createButton)
    }

    /**
     * 返回时刷新数据
     */
    override fun onResume() {
        super.onResume()
        loadPets() // 确保主页面显示最新的宠物信息
    }
}