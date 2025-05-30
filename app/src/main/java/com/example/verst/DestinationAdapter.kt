package com.example.verst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DestinationAdapter(
    private val tours: List<WebTours>,
    private val onTourClick: (WebTours) -> Unit
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.destination_image)
        val name: TextView = itemView.findViewById(R.id.destination_name)
        val location: TextView = itemView.findViewById(R.id.destination_location)
        val rating: TextView = itemView.findViewById(R.id.destination_rating)
        val reviewsCount: TextView = itemView.findViewById(R.id.reviews_count)
        val reviewAvatar3: ImageView = itemView.findViewById(R.id.review_avatar_3)
        val shareButton: ImageButton = itemView.findViewById(R.id.share_button)

        fun bind(tour: WebTours) {
            image.setImageResource(R.drawable.bg_img)
            name.text = tour.title
            location.text = tour.location
            rating.text = tour.date
            reviewsCount.text = "+50"
            reviewAvatar3.setImageResource(R.drawable.avatar_placeholder)

            // Клик по всему элементу для перехода на TourActivity
            itemView.setOnClickListener {
                onTourClick(tour)
            }

            // Клик по shareButton для показа Toast
            shareButton.setOnClickListener {
                itemView.context.showToast("Share ${tour.title}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(tours[position])
    }

    override fun getItemCount(): Int = tours.size
}

fun android.content.Context.showToast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}