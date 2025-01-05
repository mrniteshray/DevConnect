package xcom.nitesh.apps.devconnect.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import xcom.nitesh.apps.devconnect.Model.Connection
import xcom.nitesh.apps.devconnect.R

class ChatAdapter(val userlist : List<Connection>, val onChatClick : (Connection)-> Unit ): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currenuseruid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    inner class ChatViewHolder(itemview : View) : RecyclerView.ViewHolder(itemview) {

        val userimg = itemview.findViewById<ImageView>(R.id.imageView)
        val username = itemview.findViewById<TextView>(R.id.textView3)
        fun bind(user: Connection) {
            itemView.setOnClickListener {
                onChatClick(user)
            }
            if (currenuseruid == user.senderId) {
                username.text = user.receiverName
                Glide.with(itemView.context)
                    .load(user.receiverProfileImageUrl)
                    .into(userimg)
            } else {
                username.text = user.senderName
                Glide.with(itemView.context)
                    .load(user.senderProfileImageUrl)
                    .into(userimg)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list,parent,false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userlist.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(userlist[position])
    }
}