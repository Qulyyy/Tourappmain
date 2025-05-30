package com.example.verst

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.verst.LoginActivity
import com.example.verst.databinding.ItemScheduleBinding

data class Tourss(
    val date: String,
    val title: String,
    val location: String,
    val imageResId: Int // ID ресурса изображения
)

class TourAdapter(private val tours: List<Tourss>) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(tours[position])

    }

    override fun getItemCount(): Int = tours.size

    class TourViewHolder(private val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tour: Tourss) {
            binding.eventDate.text = tour.date
            binding.eventTitle.text = tour.title
            binding.eventSubtitle.text = tour.location
            binding.eventImage.setImageResource(tour.imageResId)
        }
    }
}