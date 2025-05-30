package com.example.verst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.verst.databinding.FragmentCalendarBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var currentWeekStart: LocalDate
    private lateinit var tvDateTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.calendarRecyclerView
        tvDateTitle = binding.currentDate

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        currentWeekStart = getStartOfWeek(LocalDate.now())
        updateCalendar()

        binding.btnPrev.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            updateCalendar()
        }

        binding.btnNext.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            updateCalendar()
        }

        // Создаем список туров
        val tours = listOf(
            Tourss(
                date = "26 май 2025",
                title = "Тур3",
                location = "Сочи",
                imageResId = R.drawable.niladri_reservoir
            ),
            Tourss(
                date = "26 май 2025",
                title = "Тур2",
                location = "Рязань",
                imageResId = R.drawable.niladri_reservoir
            ),
            Tourss(
                date = "26 май 2025",
                title = "Тур1",
                location = "Москва",
                imageResId = R.drawable.niladri_reservoir
            )
        )

        // Настройка списка туров
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduleRecyclerView.adapter = TourAdapter(tours)

        // Кнопки верхней панели
        binding.backButton.setOnClickListener { /* Назад */ }
        binding.notificationButton.setOnClickListener { /* Уведомления */ }
        binding.viewAllButton.setOnClickListener { /* View all */ }
    }

    private fun updateCalendar() {
        val today = LocalDate.now()

        val weekDays = (0..6).map { i ->
            val date = currentWeekStart.plusDays(i.toLong())
            DayItem(
                date = date,
                letter = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2).replaceFirstChar { it.uppercase() },
                number = date.dayOfMonth.toString(),
                isSelected = date == today
            )
        }.toMutableList()

        val selectedIndex = weekDays.indexOfFirst { it.date == today }.takeIf { it >= 0 } ?: 0
        val selectedDate = weekDays[selectedIndex].date

        tvDateTitle.text = "${selectedDate.dayOfMonth} ${
            selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        }"

        adapter = CalendarAdapter(weekDays, selectedPosition = selectedIndex) { position, dayItem ->
            moveHighlightToPosition(position)
            tvDateTitle.text = "${dayItem.date.dayOfMonth} ${dayItem.date.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
        }
        recyclerView.adapter = adapter

        moveHighlightToPosition(selectedIndex)
    }

    private fun getStartOfWeek(date: LocalDate): LocalDate {
        return date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    }

    private fun moveHighlightToPosition(position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            val highlight = binding.highlightView

            if (viewHolder != null && highlight != null) {
                val itemView = viewHolder.itemView
                val targetX = itemView.left.toFloat() + recyclerView.left

                if (highlight.visibility != View.VISIBLE) {
                    highlight.translationX = targetX
                    highlight.visibility = View.VISIBLE
                } else {
                    highlight.animate()
                        .translationX(targetX)
                        .setDuration(200)
                        .start()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}