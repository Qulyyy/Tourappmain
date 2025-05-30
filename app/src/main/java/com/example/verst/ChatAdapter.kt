package com.example.verst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Chat(
    val avatarResId: Int,
    val name: String,
    val statusResId: Int, // Ресурс для статуса (зелёный/жёлтый круг)
    val lastMessage: String,
    val time: String,
    val isRead: Boolean // Прочитано ли сообщение
)

class ChatAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.chat_avatar)
        val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
        val name: TextView = itemView.findViewById(R.id.chat_name)
        val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        val time: TextView = itemView.findViewById(R.id.chat_time)
        val messageStatus: ImageView = itemView.findViewById(R.id.message_status)

        init {
            itemView.setOnClickListener {
                onChatClick(chats[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.avatar.setImageResource(chat.avatarResId)
        holder.statusIndicator.setBackgroundResource(chat.statusResId)
        holder.name.text = chat.name
        holder.lastMessage.text = chat.lastMessage
        holder.time.text = chat.time
        holder.messageStatus.setImageResource(
            if (chat.isRead) R.drawable.ic_message_read else R.drawable.ic_message_unread
        )
    }

    override fun getItemCount(): Int = chats.size
}