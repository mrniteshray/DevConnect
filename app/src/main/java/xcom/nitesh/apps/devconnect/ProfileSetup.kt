package xcom.nitesh.apps.devconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.databinding.ActivityProfileSetupBinding

class ProfileSetup : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityProfileSetupBinding
    private val selectedSkills = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chipGroup: ChipGroup = findViewById(R.id.skills_chip_group)

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.setOnClickListener {
                if (chip.isChecked) {
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white))
                    chip.setChipBackgroundColorResource(R.color.colorPrimary)
                    selectedSkills.add(chip.text.toString())
                } else {
                    chip.setTextColor(ContextCompat.getColor(this, R.color.black))
                    chip.setChipBackgroundColorResource(R.color.white)
                    selectedSkills.remove(chip.text.toString())
                }
            }
        }

        val email = intent.getStringExtra("email")
        val authId = intent.getStringExtra("authId")
        val profilePicUrl = intent.getStringExtra("profilePicUrl")
        val accessToken = intent.getStringExtra("accessToken")
        val name = intent.getStringExtra("name")

        Glide.with(this)
            .load(profilePicUrl)
            .into(binding.profileimg)

        binding.tvName.setText("Hello! \n $name")

        binding.saveButton.setOnClickListener {
            binding.progressBar2.visibility = View.VISIBLE
            Toast.makeText(this, binding.about.text.toString(), Toast.LENGTH_SHORT).show()
            if (binding.about.text.toString().isNotEmpty()){
                saveUserProfile(name!!,binding.about.text.toString(),authId!!,selectedSkills, email!!, profilePicUrl!!, accessToken!!)
            }else{
                binding.progressBar2.visibility = View.GONE
                Toast.makeText(this, "Please add some about yourself", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveUserProfile(name: String,about:String,authId: String,skills: List<String>, email: String, imageUrl: String, accessToken: String) {
        val user = hashMapOf(
            "name" to name,
            "about" to about,
            "skills" to skills,
            "email" to email,
            "authId" to authId,
            "profileImageUrl" to imageUrl,
            "accessToken" to accessToken
        )

        db = FirebaseFirestore.getInstance()
        db.collection("users").document(authId)
            .set(user)
            .addOnSuccessListener {
                binding.progressBar2.visibility = View.GONE
                Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to save user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MainActivity", "Error saving user data", exception)
            }
    }
}