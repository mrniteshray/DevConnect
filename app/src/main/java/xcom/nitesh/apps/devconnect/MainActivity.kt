package xcom.nitesh.apps.devconnect

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import xcom.nitesh.apps.devconnect.Home.HomeFragment
import xcom.nitesh.apps.devconnect.databinding.ActivityMainBinding
import xcom.nitesh.apps.devconnect.fragments.ChatFragment
import xcom.nitesh.apps.devconnect.fragments.ConnectionFragment
import xcom.nitesh.apps.devconnect.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),  // State when item is selected
                intArrayOf(-android.R.attr.state_checked) // State when item is not selected
            )

            val newColors = intArrayOf(
                ContextCompat.getColor(this, R.color.colorPrimary),  // New selected color
                ContextCompat.getColor(this, R.color.black)   // New default color
            )

            val newColorStateList = ColorStateList(states, newColors)
            binding.navigation.itemIconTintList = newColorStateList


            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, HomeFragment())
                commit()
            }
            binding.navigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> {

                        binding.navigation.itemIconTintList = newColorStateList
                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.fragment_container, HomeFragment())
                            commit()
                        }
                        true
                    }

                    R.id.chat -> {
                        binding.navigation.itemIconTintList = newColorStateList
                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.fragment_container, ChatFragment())
                            commit()
                        }
                        true
                    }

                    R.id.conn -> {
                        binding.navigation.itemIconTintList = newColorStateList
                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.fragment_container, ConnectionFragment())
                            commit()
                        }
                        true
                    }

                    R.id.profile ->{
                        binding.navigation.itemIconTintList = newColorStateList
                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.fragment_container, ProfileFragment())
                            commit()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
}