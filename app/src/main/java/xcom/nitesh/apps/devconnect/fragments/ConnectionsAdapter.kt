package xcom.nitesh.apps.devconnect.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import xcom.nitesh.apps.devconnect.Model.Connection
import xcom.nitesh.apps.devconnect.R

class ConnectionsAdapter(
    private val onActionClick: (Connection, String) -> Unit
) : ListAdapter<Connection, ConnectionsAdapter.ConnectionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection, parent, false)
        return ConnectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateConnectionStatus(connectionId: String, newStatus: String) {
        val updatedList = currentList.map {
            if (it.connectionId == connectionId) it.copy(status = newStatus) else it
        }
        submitList(updatedList)
    }

    inner class ConnectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val skillsTextView: TextView = itemView.findViewById(R.id.skillsTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val declineButton: Button = itemView.findViewById(R.id.declineButton)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)

        fun bind(connection: Connection) {
            val isSender = connection.senderId == FirebaseAuth.getInstance().currentUser?.uid

            nameTextView.text = if (isSender) connection.receiverName else connection.senderName
            skillsTextView.text = connection.skills.joinToString(", ")
            statusTextView.text = connection.status.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
            }
            Glide.with(itemView.context).load(
                if (isSender) connection.receiverProfileImageUrl else connection.senderProfileImageUrl
            ).into(profileImageView)

            if (connection.status == "pending" && !isSender) {
                acceptButton.visibility = View.VISIBLE
                declineButton.visibility = View.VISIBLE
                acceptButton.setOnClickListener { onActionClick(connection, "accept") }
                declineButton.setOnClickListener { onActionClick(connection, "decline") }
            } else {
                acceptButton.visibility = View.GONE
                declineButton.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Connection>() {
        override fun areItemsTheSame(oldItem: Connection, newItem: Connection): Boolean {
            return oldItem.connectionId == newItem.connectionId
        }

        override fun areContentsTheSame(oldItem: Connection, newItem: Connection): Boolean {
            return oldItem == newItem
        }
    }
}
