package edu.uw.ischool.cmoh.paws_claws_reminder

object PetRepository {
    private val pets = mutableListOf<PetModel>() // 更改为使用 PetModel

    fun addPet(pet: PetModel) {
        pets.add(pet)
    }

    fun getAllPets(): List<PetModel> = pets

    fun getPetByName(name: String): PetModel? = pets.find { it.name == name }
}
