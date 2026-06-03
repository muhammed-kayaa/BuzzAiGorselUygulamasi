package com.example.buzzai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.buzzai.databinding.FragmentStoreBinding

class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Basit Plan Butonu
        binding.btnBasitPlan.setOnClickListener {
            Toast.makeText(requireContext(), "Basit Plan: Google Play altyapısı test aşamasında! 🚀", Toast.LENGTH_SHORT).show()
        }

        // 2. Kişisel Plan Butonu
        binding.btnKisiselPlan.setOnClickListener {
            Toast.makeText(requireContext(), "Kişisel Plan: Google Play altyapısı test aşamasında! 🚀", Toast.LENGTH_SHORT).show()
        }

        // 3. İşletme Planı Butonu
        binding.btnIsletmePlani.setOnClickListener {
            Toast.makeText(requireContext(), "İşletme Planı: Google Play altyapısı test aşamasında! 🚀", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}