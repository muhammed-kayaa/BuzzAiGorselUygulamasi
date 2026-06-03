package com.example.buzzai

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyHistory)

        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
        val historyString = sharedPref.getString("BUZZAI_HISTORY", "") ?: ""

        val historyList = if (historyString.isNotEmpty()) {
            historyString.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        if (historyList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE

            rvHistory.layoutManager = LinearLayoutManager(requireContext())

            // Adaptörü oluştururken Tıklanma olayını da veriyoruz
            val adapter = HistoryAdapter(historyList) { clickedImageUrl ->
                // Bir resme tıklandığında ResultFragment'ı (Tam Boy / Kaydet ekranını) aç
                parentFragmentManager.beginTransaction()
                    .hide(this@HistoryFragment) // History sayfasını gizle
                    .add(R.id.fragment_container, ResultFragment().apply { arguments = bundleOf("imageUrl" to clickedImageUrl) })
                    .addToBackStack(null) // Geri tuşuyla tekrar History'ye dönebilmek için
                    .commit()
            }
            rvHistory.adapter = adapter
        }

        return view
    }
}