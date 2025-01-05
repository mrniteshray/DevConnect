package xcom.nitesh.apps.devconnect.Home

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.R
import xcom.nitesh.apps.devconnect.databinding.FragmentHomeBinding
import xcom.nitesh.apps.devconnect.Model.Connection
import kotlin.random.Random


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var currentUserimage : String
    lateinit var currentUserName : String
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    private lateinit var homeViewModel: HomeViewModel

    private val matchedUsers = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = HomeViewModel(requireContext())

        homeViewModel.fetchCurrentUserData()

        homeViewModel.currentuserdata.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.progressBar4.visibility = View.GONE
                binding.startMatchingButton.visibility = View.VISIBLE
                binding.helloText.visibility = View.VISIBLE
                binding.userImage.visibility = View.VISIBLE
                binding.helloText.text = "Hello! \n ${user.name}"
                Glide.with(requireContext())
                    .load(user.profileImageUrl.toString())
                    .into(binding.userImage)
            }
        }

//        firestore.collection("users").document(currentUserId!!).addSnapshotListener { value, error ->
//            if (error != null) {
//                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
//                return@addSnapshotListener
//            }
//            currentUserimage = value?.get("profileImageUrl") as? String ?: ""
//            currentUserName = value?.get("name") as? String ?: ""
//            Glide.with(this).load(currentUserimage).into(binding.userImage)
//            binding.helloText.text = "Hello! \n"+currentUserName
//        }

        binding.startMatchingButton.setOnClickListener {
            fetchMatchUser()
            startMatching()
        }
    }

    fun fetchMatchUser(){
        firestore.collection("Connections")
            .document(currentUserId!!)
            .collection("ConnectionsSubCollection")
            .get()
            .addOnSuccessListener {
                val connections = it.documents.mapNotNull { it.toObject(Connection::class.java) }

                for(connection in connections){
                    if(connection.senderId == currentUserId){
                        matchedUsers.add(connection.receiverId)
                    }
                    if(connection.receiverId == currentUserId){
                        matchedUsers.add(connection.senderId)
                    }
                }
            }
    }

    private fun startMatching() {
        binding.userImage.animate().apply {
            duration = 1000
            translationYBy(-300f)
        }.start()
        binding.searchingText.animate().apply {
            duration = 1000
            translationYBy(-300f)
        }.start()
        binding.startMatchingButton.animate().alpha(0f).setDuration(500).withEndAction {
            binding.startMatchingButton.visibility = View.GONE
        }

        // Hide hello text with fade-out animation
        binding.helloText.animate().alpha(0f).setDuration(500).withEndAction {
            binding.helloText.visibility = View.GONE
        }

        // Show loading and searching animations
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.searchingText.visibility = View.VISIBLE
        binding.searchingText.text = "Searching for profiles..."

        Handler(Looper.getMainLooper()).postDelayed({
            showMatchedProfile()
        }, 2000)
    }

    private fun showMatchedProfile() {
        // Hide initial views and show loading
        binding.startMatchingButton.visibility = View.GONE
        binding.helloText.visibility = View.GONE
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.searchingText.visibility = View.VISIBLE

        findMatch()
    }

    private fun findMatch() {

        firestore.collection("users").document(currentUserId!!)
            .get()
            .addOnSuccessListener { currentUserSnapshot ->
                val currentUserSkills = currentUserSnapshot["skills"] as? List<String> ?: emptyList()

                //fetching all user
                firestore.collection("users")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val userList = querySnapshot.documents.filter { document ->
                            val otherUserSkills = document["skills"] as? List<String> ?: emptyList()
                            val authId = document["authId"] as? String
                            authId != currentUserId && !matchedUsers.contains(authId) &&
                                    currentUserSkills.any { it in otherUserSkills }
                        }

                        if (userList.isNotEmpty()) {
                            val randomUser = userList[Random.nextInt(userList.size)]
                            matchedUsers.add(randomUser.id)
                            displayMatchedProfile(randomUser.id, randomUser.data ?: emptyMap())
                        } else {
                            resetMatchingUI()
                            Toast.makeText(requireContext(), "No matching profiles found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.searchingText.text = "Error fetching users: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                binding.searchingText.text = "Error fetching current user: ${e.message}"
            }
    }

    private fun displayMatchedProfile(userId: String, userData: Map<String, Any>) {
        binding.loadingAnimation.visibility = View.GONE
        binding.searchingText.text = "Profile Matched!"

        val userImage = userData["profileImageUrl"] as? String ?: ""
        binding.matchedProfileImage.visibility = View.VISIBLE
        Glide.with(this)
            .load(userImage)
            .into(binding.matchedProfileImage)

        binding.matchedProfileImage.setOnClickListener {
            showUserDetailsDialog(userId, userData)
        }
    }

    private fun showUserDetailsDialog(userId: String, userData: Map<String, Any>) {

        val colors = listOf(
            "#FFCDD2",
            "#C8E6C9",
            "#BBDEFB",
            "#D1C4E9",
            "#FFF9C4",
            "#FFECB3",
            "#B3E5FC"
        )

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_match_user, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val usertext = dialogView.findViewById<TextView>(R.id.usernamestxt)
        val cancelButton = dialogView.findViewById<ImageView>(R.id.cancelButton)
        val matchedUserImage = dialogView.findViewById<ImageView>(R.id.matchedUserImage)
        val matchedUserSkills = dialogView.findViewById<ChipGroup>(R.id.matchedUserSkills)
        val matchedUserAbout = dialogView.findViewById<TextView>(R.id.matchedUserAbout)
        val connectButton = dialogView.findViewById<Button>(R.id.connectButton)
        val animatedTick = dialogView.findViewById<LottieAnimationView>(R.id.animatedTick)

        val username = userData["name"] as? String ?: ""
        val userImage = userData["profileImageUrl"] as? String ?: ""
        val userSkills = userData["skills"] as? List<String> ?: listOf("N/A")
        val userAbout = userData["about"] as? String ?: "No details available."

        cancelButton.setOnClickListener {
            resetMatchingUI()
            dialog.dismiss()
        }
        for ((index, skill) in userSkills.withIndex()) {
            val chip = Chip(requireContext())
            chip.text = skill
            chip.setChipBackgroundColorResource(android.R.color.transparent)
            chip.setTextColor(Color.WHITE)
            chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(colors[index % colors.size]))
            matchedUserSkills.addView(chip)
        }

        usertext.text = username
        Glide.with(this).load(userImage).into(matchedUserImage)
        matchedUserAbout.text = "About: $userAbout"

        connectButton.setOnClickListener {
            sendConnectionRequest(currentUserId!!,userId,currentUserName,userData["name"] as String,currentUserimage,userImage,userSkills)
            connectButton.visibility = View.GONE
            usertext.visibility = View.GONE
            matchedUserImage.visibility = View.GONE
            matchedUserSkills.visibility = View.GONE
            matchedUserAbout.visibility = View.GONE
            cancelButton.visibility = View.GONE
            animatedTick.visibility = View.VISIBLE
            animatedTick.animate().alpha(1f).setDuration(3000).withEndAction {
                resetMatchingUI()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun sendConnectionRequest(
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        senderProfileImageUrl: String,
        receiverProfileImageUrl: String,
        receiverSkills: List<String>
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val timestamp = System.currentTimeMillis()
        val connectionId = firestore.collection("Connections").document().id

        val connectionData = hashMapOf(
            "connectionId" to connectionId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "senderName" to senderName,
            "receiverName" to receiverName,
            "senderProfileImageUrl" to senderProfileImageUrl,
            "receiverProfileImageUrl" to receiverProfileImageUrl,
            "skills" to receiverSkills,
            "status" to "pending",
            "timestamp" to timestamp
        )


        val senderPath = "Connections/$currentUserId/ConnectionsSubCollection/$connectionId"
        val receiverPath = "Connections/$receiverId/ConnectionsSubCollection/$connectionId"

        val senderRef = firestore.document(senderPath)
        val receiverRef = firestore.document(receiverPath)

        firestore.runBatch { batch ->
            batch.set(senderRef, connectionData)
            batch.set(receiverRef, connectionData)
        }
    }



    private fun resetMatchingUI() {

        binding.userImage.animate().apply {
            duration = 1000
            translationYBy(300f)
        }.start()
        binding.searchingText.animate().apply {
            duration = 1000
            translationYBy(300f)
        }.start()
        binding.startMatchingButton.animate().alpha(1f).setDuration(500).withEndAction {
            binding.startMatchingButton.visibility = View.VISIBLE
        }

        binding.helloText.animate().alpha(1f).setDuration(500).withEndAction {
            binding.helloText.visibility = View.VISIBLE
        }

        binding.searchingText.visibility = View.GONE

        binding.loadingAnimation.visibility = View.GONE
        binding.matchedProfileImage.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}