package xcom.nitesh.apps.devconnect.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.SignInActivity
import xcom.nitesh.apps.devconnect.Model.User
import xcom.nitesh.apps.devconnect.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    binding.about.text = user.about
                    binding.tvName.text = "${user.name}"
                    Glide.with(this).load(user.profileImageUrl).into(binding.profileimg)

                    user.skills.forEach { skill ->
                        val chip = Chip(requireContext()).apply {
                            text = skill
                            setChipBackgroundColorResource(android.R.color.transparent)
                            setTextColor(Color.WHITE)
                        }
                        binding.skillsChipGroup.addView(chip)
                    }
                } else {
                    Log.e("ProfileFragment", "User data is null")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to fetch user data", e)
            }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            requireActivity().finish()
        }

    }

}