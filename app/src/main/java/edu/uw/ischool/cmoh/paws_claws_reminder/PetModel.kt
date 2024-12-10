package edu.uw.ischool.cmoh.paws_claws_reminder

data class PetModel(
    val name: String,
    val photoUri: String = "",
    val type: String = "Other",
    val dob: String = ""
)
//change name to PetModel
