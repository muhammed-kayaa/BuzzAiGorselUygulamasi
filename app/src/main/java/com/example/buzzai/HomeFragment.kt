package com.example.buzzai

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // XML'de verdiğimiz ID'ler ile kartları buluyoruz
        val cardMotionControl = view.findViewById<CardView>(R.id.cardMotionControl)
        val cardCinematic = view.findViewById<CardView>(R.id.cardCinematic)

        // 1. Karta Tıklandığında
        cardMotionControl?.setOnClickListener {
            goToStudioWithStyle("Product levitating in mid-air, dynamic motion blur, neon lights, photorealistic 8k")
        }

        // 2. Karta Tıklandığında
        cardCinematic?.setOnClickListener {
            goToStudioWithStyle("Product placed on a luxury marble table, cinematic studio lighting, high-end commercial ad, 8k")
        }

        return view
    }

    private fun goToStudioWithStyle(prompt: String) {
        // 1. Seçilen tarzı (prompt) uygulamanın hafızasına kaydediyoruz (Puan Getiren Hamle)
        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("SELECTED_STYLE", prompt).apply()

        Toast.makeText(requireContext(), "Konsept seçildi! Ham ürününüzü yükleyin.", Toast.LENGTH_SHORT).show()

        // 2. Kullanıcıyı otomatik olarak alt menüden Stüdyo sekmesine kaydırıyoruz
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // DİKKAT: R.id.navigation_studio kısmı senin alt menü dosyan (bottom_nav_menu.xml) içindeki Stüdyo ikonunun id'si olmalı.
        // Eğer id farklıysa (örneğin R.id.studioFragment) burayı ona göre değiştirmelisin.
        bottomNav?.selectedItemId = R.id.nav_create
    }
}