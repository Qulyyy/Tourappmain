package com.example.verst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verst.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Тестовые данные для списка чатов
        val chats = listOf(
            Chat(
                avatarResId = R.drawable.avatar_placeholder,
                name = "Sajib Rahman",
                statusResId = R.drawable.circle_status_yellow,
                lastMessage = "Hi, John! How are you doing?",
                time = "09:46",
                isRead = false
            ),
            Chat(
                avatarResId = R.drawable.avatar_placeholder,
                name = "Adom Shafi",
                statusResId = R.drawable.circle_status_green,
                lastMessage = "Typing...",
                time = "08:42",
                isRead = true
            ),
            Chat(
                avatarResId = R.drawable.avatar_placeholder,
                name = "HR Rumen",
                statusResId = R.drawable.circle_status_green,
                lastMessage = "You: Cool! Let's meet at 18:00 near the traveling!",
                time = "Yesterday",
                isRead = true
            ),
            Chat(
                avatarResId = R.drawable.avatar_placeholder,
                name = "Anjelina",
                statusResId = R.drawable.circle_status_yellow,
                lastMessage = "You: Hey, will you come to the party on Saturday?",
                time = "00:00",
                isRead = false
            ),
            Chat(
                avatarResId = R.drawable.avatar_placeholder,
                name = "Aleka Shorna",
                statusResId = R.drawable.circle_status_green,
                lastMessage = "Thank you for coming! Your or...",
                time = "05:52",
                isRead = true
            )
        )

        // Настройка RecyclerView
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatsRecyclerView.adapter = ChatAdapter(chats) { chat ->
            // Обработка клика по чату
            // Здесь можно открыть чат
        }

        // Обработчики кликов
        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.menuButton.setOnClickListener { /* Открыть меню */ }
        binding.newMessageButton.setOnClickListener { /* Новый чат */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}