package xcom.nitesh.apps.devconnect.Model

data class User(
    val authId: String = "",
    val name: String = "",
    val about: String = "",
    val skills: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val email: String = "",
    val accessToken: String = ""
)
