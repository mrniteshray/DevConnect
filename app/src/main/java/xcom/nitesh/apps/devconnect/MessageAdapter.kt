package xcom.nitesh.apps.devconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val MESSAGE_TYPE_SENT = 1
    private val MESSAGE_TYPE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MESSAGE_TYPE_SENT) {
            SentMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.send_msg, parent, false)
            )
        } else {
            ReceivedMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.rec_msg, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) MESSAGE_TYPE_SENT else MESSAGE_TYPE_RECEIVED
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sendmsg)
        fun bind(message: Message) {
            messageTextView.text = message.messageText
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.recmsg)
        fun bind(message: Message) {
            messageTextView.text = message.messageText
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.senderId == newItem.senderId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
