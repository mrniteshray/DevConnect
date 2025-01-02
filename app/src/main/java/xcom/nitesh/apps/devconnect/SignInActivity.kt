package xcom.nitesh.apps.devconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.load.resource.bitmap.Rotate
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.textView.animate().apply {
            duration = 2000
            translationYBy(-100f)
        }.start()

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.SigninBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            signInWithGitHub()
        }
    }
    fun signInWithGitHub() {
        val provider = OAuthProvider.newBuilder("github.com")
            .setScopes(listOf("user:email", "read:user"))
            .build()

        auth.startActivityForSignInWithProvider(this, provider)
            .addOnSuccessListener { authResult: AuthResult? ->
                val user = auth.currentUser
                val firestore = FirebaseFirestore.getInstance()
                val userDocRef = firestore.collection("users").document(user!!.uid)
                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "User"
                        Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        binding.progressBar.visibility = View.GONE
                        finish()
                    } else {
                        val accessToken = (authResult?.credential as? OAuthCredential)?.accessToken
                        val name = authResult?.additionalUserInfo?.profile?.get("name") as? String ?: "No Name"
                        val profilePicUrl = user.photoUrl?.toString() ?: "No Profile Picture URL"
                        val email = user.email
                        val authUid = user.uid

                        val intent = Intent(this, ProfileSetup::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("authId", authUid)
                        intent.putExtra("profilePicUrl", profilePicUrl)
                        intent.putExtra("accessToken", accessToken)
                        intent.putExtra("name", name)
                        startActivity(intent)
                        binding.progressBar.visibility = View.GONE
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    // Handle errors
                    Toast.makeText(this, "Error checking profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }


            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Sign-in failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Sign-in failed", exception)
            }
    }
}