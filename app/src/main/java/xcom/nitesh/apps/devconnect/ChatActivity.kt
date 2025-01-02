package xcom.nitesh.apps.devconnect

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import xcom.nitesh.apps.devconnect.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var chatId: String = ""
    private var connectionName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId") ?: ""
        connectionName = intent.getStringExtra("connectionName") ?: ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = connectionName

        setupRecyclerView()
        fetchMessages()

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(currentUserId)
        binding.recyclerViewMessages.adapter = adapter
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchMessages() {
        db.collection("Chats")
            .document(chatId)
            .collection("MessagesSubCollection")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                val messages = snapshots?.toObjects(Message::class.java) ?: emptyList()
                adapter.submitList(messages)
                binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(text: String) {
        val message = Message(
            senderId = currentUserId,
            messageText = text,
            timestamp = System.currentTimeMillis()
        )
        db.collection("Chats")
            .document(chatId)
            .collection("MessagesSubCollection")
            .add(message)
    }
}
