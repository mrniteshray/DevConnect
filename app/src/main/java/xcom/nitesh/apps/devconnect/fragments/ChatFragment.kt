package xcom.nitesh.apps.devconnect.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.ChatActivity
import xcom.nitesh.apps.devconnect.Model.Connection
import xcom.nitesh.apps.devconnect.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)

        binding.progressBar3.visibility = View.VISIBLE

        fetchAcceptedConnections(currentUserId) { connections ->
            adapter = ChatAdapter(connections){ con ->
                openChatScreen(con)
            }
            binding.chatRecyclerView.adapter = adapter
            binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchAcceptedConnections(userId: String, onResult: (List<Connection>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Connections")
            .document(userId)
            .collection("ConnectionsSubCollection")
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { documents ->
                val connections = documents.mapNotNull { it.toObject(Connection::class.java) }
                onResult(connections)
                binding.progressBar3.visibility = View.GONE
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    private fun openChatScreen(connection: Connection) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("chatId", generateChatId(currentUserId, connection))
            if(connection.senderId == currentUserId){
                putExtra("connectionName", connection.receiverName)
            }else{
                putExtra("connectionName", connection.senderName)
            }
        }
        startActivity(intent)
    }
    private fun generateChatId(userId: String, connection: Connection): String {
        if(userId == connection.senderId){
            return "${userId}_${connection.receiverId}"
        }
        return "${connection.senderId}_${userId}"
    }
}

