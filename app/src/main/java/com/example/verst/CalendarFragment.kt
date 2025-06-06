package com.example.verst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.verst.databinding.FragmentCalendarBinding
import com.example.verst.network.ApiClient.apiService
import com.example.verst.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var tourAdapter: TourAdapter
    private lateinit var currentWeekStart: LocalDate
    private lateinit var tvDateTitle: TextView
    private var allTours: List<WebTours> = emptyList()
    private var selectedDate: LocalDate? = null

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
        selectedDate = LocalDate.now()
        updateCalendar()

        binding.btnPrev.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            updateCalendar()
        }

        binding.btnNext.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            updateCalendar()
        }

        // Настройка списка туров
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tourAdapter = TourAdapter(emptyList())
        binding.scheduleRecyclerView.adapter = tourAdapter

        // Загрузка туров с сервера
        loadTours()

        // Кнопки верхней панели
        binding.backButton.setOnClickListener { /* Назад */ }
        binding.notificationButton.setOnClickListener { /* Уведомления */ }
        binding.viewAllButton.setOnClickListener { /* View all */ }
    }

    private fun loadTours() {

        apiService.getAllTours().enqueue(object : Callback<List<WebTours>> {
            override fun onResponse(call: Call<List<WebTours>>, response: Response<List<WebTours>>) {
                if (response.isSuccessful) {
                    allTours = response.body() ?: emptyList()
                    updateToursForSelectedDate()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load tours: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<WebTours>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateToursForSelectedDate() {
        selectedDate?.let { date ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedDateString = date.format(formatter)
            val filteredTours = allTours.filter { it.date == selectedDateString }
            tourAdapter.updateTours(filteredTours)
            // Показать/скрыть плейсхолдер
            if (filteredTours.isEmpty()) {
                binding.scheduleRecyclerView.visibility = View.GONE
                binding.noToursPlaceholder.visibility = View.VISIBLE
            } else {
                binding.scheduleRecyclerView.visibility = View.VISIBLE
                binding.noToursPlaceholder.visibility = View.GONE
            }
        }
    }

    private fun updateCalendar() {
        val today = LocalDate.now()

        val weekDays = (0..6).map { i ->
            val date = currentWeekStart.plusDays(i.toLong())
            DayItem(
                date = date,
                letter = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2).replaceFirstChar { it.uppercase() },
                number = date.dayOfMonth.toString(),
                isSelected = date == selectedDate
            )
        }.toMutableList()

        val selectedIndex = weekDays.indexOfFirst { it.date == selectedDate }.takeIf { it >= 0 } ?: 0
        selectedDate = weekDays[selectedIndex].date

        tvDateTitle.text = "${selectedDate?.dayOfMonth} ${
            selectedDate?.month?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
        }"

        adapter = CalendarAdapter(weekDays, selectedIndex) { position, dayItem ->
            selectedDate = dayItem.date
            moveHighlightToPosition(position)
            tvDateTitle.text = "${dayItem.date.dayOfMonth} ${dayItem.date.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
            updateToursForSelectedDate()
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