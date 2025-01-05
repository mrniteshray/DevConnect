package xcom.nitesh.apps.devconnect.Home

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.Model.User

class HomeRepo(val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    var currentUserLiveData = MutableLiveData<User?>()

    fun getCurrentUserData(): MutableLiveData<User?> {
        firestore.collection("users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                currentUserLiveData.value = user
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
        return currentUserLiveData
    }

}