package edu.uw.ischool.cmoh.paws_claws_reminder

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PetAdapter(
    private val pets: List<PetModel>, // 修改为 PetModel
    private val onPetClick: (PetModel) -> Unit // 修改为 PetModel
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val petImage: ImageView = view.findViewById(R.id.img_pet)
        val petName: TextView = view.findViewById(R.id.tv_pet_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        holder.petImage.setImageURI(Uri.parse(pet.photoUri))
        holder.petName.text = pet.name
        holder.itemView.setOnClickListener { onPetClick(pet) }
    }

    override fun getItemCount(): Int = pets.size
}
