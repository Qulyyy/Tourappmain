package com.example.verst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderTourAdapter(
    private val tours: List<WebTours>
) : RecyclerView.Adapter<OrderTourAdapter.OrderTourViewHolder>() {

    inner class OrderTourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.tour_image)
        val title: TextView = itemView.findViewById(R.id.tour_title)
        val location: TextView = itemView.findViewById(R.id.tour_location)
        val price: TextView = itemView.findViewById(R.id.tour_price)
        val date: TextView = itemView.findViewById(R.id.tour_date)

        fun bind(tour: WebTours) {
            image.setImageResource(R.drawable.bg_img)
            title.text = tour.title
            location.text = tour.location
            price.text = "${tour.price} $"
            date.text = tour.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderTourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_tour, parent, false)
        return OrderTourViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderTourViewHolder, position: Int) {
        holder.bind(tours[position])
    }

    override fun getItemCount(): Int = tours.size
}