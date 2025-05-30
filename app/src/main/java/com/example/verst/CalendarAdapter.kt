package com.example.verst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<DayItem>,
    private var selectedPosition: Int,
    private val onDayClick: (Int, DayItem) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val letter: TextView = itemView.findViewById(R.id.dayLetter)
        val number: TextView = itemView.findViewById(R.id.dayNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.letter.text = day.letter
        holder.number.text = day.number

        // Устанавливаем цвет текста в зависимости от isSelected
        if (day.isSelected) {
            holder.letter.setTextColor(android.graphics.Color.WHITE)
            holder.number.setTextColor(android.graphics.Color.WHITE)
        } else {
            holder.letter.setTextColor(android.graphics.Color.BLACK)
            holder.number.setTextColor(android.graphics.Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                // Сбрасываем isSelected для предыдущего элемента
                days[selectedPosition].isSelected = false
                // Устанавливаем isSelected для нового элемента
                days[position].isSelected = true
                val previous = selectedPosition
                selectedPosition = position
                notifyItemChanged(previous)
                notifyItemChanged(position)
                onDayClick(position, day)
            }
        }
    }

    override fun getItemCount(): Int = days.size
}