package edu.uw.ischool.cmoh.paws_claws_reminder

data class TaskModel(
    val taskId: String = "", // 确保 taskId 映射正确
    val userId: String = "",
    val petName: List<String>? = null, // 将 petName 改为 List<String> 以适配数组形式
    val taskName: String = "",
    val type: String = "",
    val details: String = "",
    val time: String = "",
    val repeats: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val checked: Boolean = false
)




