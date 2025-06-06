package com.example.verst

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TourAdapter(
    private var tours: List<WebTours>
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    companion object {
        private const val TAG = "TourAdapter"
    }

    inner class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_title)
        val location: TextView = itemView.findViewById(R.id.event_subtitle)
        val date: TextView = itemView.findViewById(R.id.event_date)
        val image: ImageView = itemView.findViewById(R.id.event_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = tours[position]
        holder.title.text = tour.title
        holder.location.text = tour.location

        // Форматирование даты в "26 January 2025"
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
            val date = LocalDate.parse(tour.date, inputFormatter)
            holder.date.text = date.format(outputFormatter)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to format date: ${tour.date}", e)
            holder.date.text = tour.date // Fallback
        }

        // Загрузка изображения
        try {
            // Приводим imageResId к нижнему регистру и убираем лишние пробелы
            val cleanImageResId = tour.imageResId.trim().lowercase()
            Log.d(TAG, "Loading image: $cleanImageResId")
            val resId = holder.itemView.context.resources.getIdentifier(
                cleanImageResId, "drawable", holder.itemView.context.packageName
            )
            if (resId != 0) {
                holder.image.setImageResource(resId)
            } else {
                Log.w(TAG, "Resource not found for $cleanImageResId, using fallback")
                holder.image.setImageResource(R.drawable.niladri_reservoir) // Заглушка
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load image: ${tour.imageResId}", e)
            holder.image.setImageResource(R.drawable.niladri_reservoir) // Заглушка
        }
    }

    override fun getItemCount(): Int = tours.size

    fun updateTours(newTours: List<WebTours>) {
        tours = newTours
        notifyDataSetChanged()
    }
}