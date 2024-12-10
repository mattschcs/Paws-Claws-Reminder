package edu.uw.ischool.cmoh.paws_claws_reminder

//import com.google.firebase.database.PropertyName

data class PetModel(
    val petName: String = "",
    val userId: String = "",
    val dob: String = "",
    val photoUri: String = "",
    val type: String = "",
    val name: String = "" // 添加此字段

) {
    override fun toString(): String {
        return "PetModel(petName='$petName', userId='$userId', dob='$dob', photoUri='$photoUri', type='$type')"
    }
}


