package com.example.verst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToursAdapter(
    private val tours: List<WebTours>,
    private val onTourClick: (WebTours) -> Unit,
    private val isLoading: Boolean = false,
    private val isError: Boolean = false,
    private val onRetryClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        private const val VIEW_TYPE_ERROR = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> VIEW_TYPE_LOADING
            isError -> VIEW_TYPE_ERROR
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tour_adapter, parent, false)
                TourViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            VIEW_TYPE_ERROR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_error, parent, false)
                ErrorViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TourViewHolder -> {
                val tour = tours[position]
                holder.bind(tour)
                holder.itemView.setOnClickListener { onTourClick(tour) }
            }
            is ErrorViewHolder -> {
                holder.bind(onRetryClick)
            }
            // LoadingViewHolder не требует bind
        }
    }

    override fun getItemCount(): Int = if (isLoading || isError) 1 else tours.size

    class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tourImage: ImageView = itemView.findViewById(R.id.tour_image)
        private val tourTitle: TextView = itemView.findViewById(R.id.tour_title)
        private val tourLocation: TextView = itemView.findViewById(R.id.tour_location)
        private val tourPrice: TextView = itemView.findViewById(R.id.tour_price)

        fun bind(tour: WebTours) {
            tourTitle.text = tour.title
            tourPrice.text = tour.price
            tourLocation.text = tour.location
            // Преобразуем imageResId (строку) в идентификатор ресурса
            val resId = itemView.context.resources.getIdentifier(
                tour.imageResId, "drawable", itemView.context.packageName
            )
            tourImage.setImageResource(if (resId != 0) resId else R.drawable.niladri_reservoir)
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val retryButton: Button = itemView.findViewById(R.id.retry_button)

        fun bind(onRetryClick: (() -> Unit)?) {
            retryButton.setOnClickListener { onRetryClick?.invoke() }
        }
    }
}