package xcom.nitesh.apps.devconnect.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.devconnect.R
import xcom.nitesh.apps.devconnect.databinding.FragmentConnectionBinding

class ConnectionFragment : Fragment() {

    private lateinit var binding: FragmentConnectionBinding
    private lateinit var adapter: ConnectionsAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)

        adapter = ConnectionsAdapter { connection, action ->
            handleConnectionAction(connection, action)
        }
        binding.recyclerViewConnections.adapter = adapter
        binding.recyclerViewConnections.layoutManager = LinearLayoutManager(requireContext())

        fetchConnections(currentUserId) {
            binding.recyclerViewConnections.visibility = View.VISIBLE
            binding.progressBarConnections.visibility = View.GONE
            adapter.submitList(it)
        }
    }

    private fun handleConnectionAction(connection: Connection, action: String) {
        val db = FirebaseFirestore.getInstance()

        val senderId = connection.senderId
        val receiverId = connection.receiverId
        val connectionId = connection.connectionId

        val senderRef = db.collection("Connections")
            .document(senderId)
            .collection("ConnectionsSubCollection")
            .document(connectionId)

        val receiverRef = db.collection("Connections")
            .document(receiverId)
            .collection("ConnectionsSubCollection")
            .document(connectionId)

        when (action) {
            "accept" -> {
                senderRef.update("status", "accepted")
                receiverRef.update("status", "accepted")
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Connection accepted", Toast.LENGTH_SHORT).show()
                        adapter.updateConnectionStatus(connectionId, "accepted")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to accept connection", Toast.LENGTH_SHORT).show()
                    }
            }
            "decline" -> {
                senderRef.update("status", "declined")
                receiverRef.update("status", "declined")
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Connection declined", Toast.LENGTH_SHORT).show()
                        adapter.updateConnectionStatus(connectionId, "declined")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to decline connection", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun fetchConnections(userId: String, onResult: (List<Connection>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Connections")
            .document(userId)
            .collection("ConnectionsSubCollection")
            .get()
            .addOnSuccessListener { documents ->
                val connections = documents.mapNotNull { it.toObject(Connection::class.java) }
                onResult(connections)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}


