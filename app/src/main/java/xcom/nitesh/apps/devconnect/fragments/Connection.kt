package xcom.nitesh.apps.devconnect.fragments

data class Connection(
    val connectionId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val senderProfileImageUrl: String = "",
    val receiverProfileImageUrl: String = "",
    val skills: List<String> = emptyList(),
    val status: String = "", // Pending, Accepted, Declined
    val timestamp: Long = 0L
)

