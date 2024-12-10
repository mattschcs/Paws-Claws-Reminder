package edu.uw.ischool.cacs2340142.pawsandclawsreminder

import android.util.Log
import com.google.firebase.database.*

object PetRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("pets")
    private val cache = mutableListOf<PetModel>()
    private const val TAG = "PetRepository"

    fun getAllPets(callback: (List<PetModel>) -> Unit) {
        // 如果缓存中有数据，直接返回
        if (cache.isNotEmpty()) {
            callback(cache)
            return
        }

        // 从 Firebase 获取所有宠物数据
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pets = mutableListOf<PetModel>()
                snapshot.children.forEach { child ->
                    val pet = child.getValue(PetModel::class.java)
                    if (pet != null) {
                        pets.add(pet)
                    }
                }
                cache.clear()
                cache.addAll(pets)
                callback(pets)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch pets: ${error.message}")
                callback(emptyList()) // 返回空列表作为错误处理
            }
        })
    }

    fun getPetById(petId: String, callback: (PetModel?) -> Unit) {
        database.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pet = snapshot.getValue(PetModel::class.java)
                callback(pet)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch pet by ID: ${error.message}")
                callback(null) // 返回 null 作为错误处理
            }
        })
    }

    fun addPet(pet: PetModel, callback: (Boolean) -> Unit) {
        val petId = database.push().key ?: return callback(false)
        database.child(petId).setValue(pet)
            .addOnSuccessListener {
                cache.add(pet) // 更新缓存
                callback(true)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to add pet: ${error.message}")
                callback(false)
            }
    }

    fun updatePet(petId: String, pet: PetModel, callback: (Boolean) -> Unit) {
        database.child(petId).setValue(pet)
            .addOnSuccessListener {
                val index = cache.indexOfFirst { it.name == pet.name }
                if (index != -1) {
                    cache[index] = pet // 更新缓存
                }
                callback(true)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to update pet: ${error.message}")
                callback(false)
            }
    }

    fun deletePet(petId: String, callback: (Boolean) -> Unit) {
        database.child(petId).removeValue()
            .addOnSuccessListener {
                cache.removeAll { it.name == petId } // 从缓存中移除
                callback(true)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to delete pet: ${error.message}")
                callback(false)
            }
    }
}
