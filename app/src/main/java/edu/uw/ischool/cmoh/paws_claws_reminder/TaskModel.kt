package edu.uw.ischool.cmoh.paws_claws_reminder

data class TaskModel(
    val taskId: String = "",
    val userId: String = "",
    val petName: List<String>? = emptyList(),
    val taskName: String = "",
    val type: String = "",
    val details: String = "",
    val time: String = "",
    val repeats: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val checked: Boolean = false
) {
    override fun toString(): String {
        return "TaskModel(taskId='$taskId', userId='$userId', petName=$petName, " +
                "taskName='$taskName', type='$type', details='$details', time='$time', " +
                "repeats='$repeats', startDate='$startDate', endDate='$endDate', checked=$checked)"
    }
}





