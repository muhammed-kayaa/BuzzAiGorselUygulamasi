package com.example.buzzai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buzzai.databinding.ItemHistoryBinding

class HistoryAdapter(
    private var items: List<String>,
    private val onItemClick: (String) -> Unit // Tıklanma olayını dışarı aktarmak için eklendi
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Glide.with(binding.ivHistoryItem.context)
                .load(url)
                .centerCrop()
                .into(binding.ivHistoryItem)

            // Resme Tıklandığında çalışacak kod
            binding.root.setOnClickListener {
                onItemClick(url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHistoryBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}