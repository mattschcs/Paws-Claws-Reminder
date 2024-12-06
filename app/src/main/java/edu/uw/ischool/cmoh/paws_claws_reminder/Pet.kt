package edu.uw.ischool.cmoh.paws_claws_reminder

data class Pet(
    val name: String,
    val photoUri: String = "",
    val type: String = "Other",
    val dob: String = ""
)

