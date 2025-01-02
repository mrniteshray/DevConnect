package xcom.nitesh.apps.devconnect

data class Message(
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)

