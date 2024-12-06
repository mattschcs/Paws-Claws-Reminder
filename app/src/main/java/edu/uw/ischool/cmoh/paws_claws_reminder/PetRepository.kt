package edu.uw.ischool.cmoh.paws_claws_reminder

object PetRepository {
    private val pets = mutableListOf<Pet>()

    fun addPet(pet: Pet) {
        pets.add(pet)
    }

    fun getAllPets(): List<Pet> = pets

    fun getPetByName(name: String): Pet? = pets.find { it.name == name }
}
