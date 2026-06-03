package com.example.buzzai

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<MaterialButton>(R.id.btnUltraPlan)?.setOnClickListener {
            if (activity is MainActivity) {
                (requireActivity() as MainActivity).simulateNavigationClick(R.id.custom_nav_store)
            }
        }

        val videoPlayer = view.findViewById<VideoView>(R.id.videoPlayer)
        try {
            val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.tanitim_video
            videoPlayer.setVideoURI(Uri.parse(videoPath))
            videoPlayer.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.setVolume(0f, 0f)
                videoPlayer.start()
            }
        } catch (e: Exception) { e.printStackTrace() }

        view.findViewById<CardView>(R.id.cardStrategy)?.setOnClickListener {
            Toast.makeText(requireContext(), "2.0 Strategy Özel Sayfası Açılacak", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CardView>(R.id.cardBusiness)?.setOnClickListener {
            Toast.makeText(requireContext(), "AI İşletme Özel Sayfası Açılacak", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CardView>(R.id.cardPersonal)?.setOnClickListener {
            Toast.makeText(requireContext(), "AI Kişisel Özel Sayfası Açılacak", Toast.LENGTH_SHORT).show()
        }

        // ====================================================================
        // --- 3. DİNAMİK YAPAY ZEKA (PROMPT) YÖNLENDİRMELERİ ---
        // ====================================================================

        // Kategori 1: Trendler (Motion -> Cinematic)
        view.findViewById<CardView>(R.id.cardMotionControl)?.setOnClickListener {
            goToStudioWithStyle("Dynamic motion blur, neon stage lights, energetic performance vibe, highly detailed 8k", "Motion Control")
        }
        view.findViewById<CardView>(R.id.cardCinematic)?.setOnClickListener {
            goToStudioWithStyle("Cinematic lighting, dramatic rain effect, moody street aesthetic, 8k resolution, photorealistic", "Cinematic Render")
        }

        // Kategori 2: İlham Verenler (Neon -> Mermer -> Ahşap)
        view.findViewById<CardView>(R.id.cardNeon)?.setOnClickListener {
            goToStudioWithStyle("Product bathed in vibrant neon pink and purple lights, dark cyberpunk club aesthetic, 8k commercial quality", "Neon Işıklar")
        }
        view.findViewById<CardView>(R.id.cardMarble)?.setOnClickListener {
            goToStudioWithStyle("Product placed on a premium white marble pedestal, soft natural lighting, luxurious aesthetic, 8k", "Lüks Mermer")
        }
        view.findViewById<CardView>(R.id.cardWood)?.setOnClickListener {
            goToStudioWithStyle("Product placed on a rustic wooden table, warm sunlight filtering through a window, cozy atmosphere, photorealistic", "Ahşap Zemin")
        }

        // Kategori 3: AI İşletme (Kozmetik/Smoothie -> Podyum/Arıtma -> Kahve/Kanat)
        view.findViewById<CardView>(R.id.cardCosmetic)?.setOnClickListener {
            goToStudioWithStyle("Dynamic splash photography of a cold strawberry beverage, floating fresh fruits, vibrant pink and red hues, studio commercial lighting, 8k", "Dinamik Sıçrama")
        }
        view.findViewById<CardView>(R.id.cardPodium)?.setOnClickListener {
            goToStudioWithStyle("Professional product photography of a water purification system, futuristic neon city rooftop setting, sleek metallic podium, high-end commercial render, 8k", "Fütüristik Podyum")
        }
        view.findViewById<CardView>(R.id.cardCoffee)?.setOnClickListener {
            goToStudioWithStyle("Creative beverage product shot, iced coffee cups with magical glowing wings, warm cafe ambiance, cinematic lighting, 8k", "Sihirli Ürün Çekimi")
        }

        // Kategori 4: AI Kişisel (Cyberpunk -> Anime -> LinkedIn)
        view.findViewById<CardView>(R.id.cardCyberpunk)?.setOnClickListener {
            goToStudioWithStyle("Cyberpunk style character portrait, neon pink and blue city lights in the background, futuristic attire, highly detailed, Unreal Engine 5 render", "Cyberpunk Avatar")
        }
        view.findViewById<CardView>(R.id.cardAnime)?.setOnClickListener {
            goToStudioWithStyle("Portrait of a person in Makoto Shinkai anime style, vibrant colors, beautiful anime sky background, highly detailed illustration", "Anime Karakter")
        }
        view.findViewById<CardView>(R.id.cardLinkedin)?.setOnClickListener {
            goToStudioWithStyle("Professional corporate headshot portrait, modern office background, soft studio lighting, sharp focus, 8k", "LinkedIn Profil")
        }

        return view
    }

    private fun goToStudioWithStyle(prompt: String, styleName: String) {
        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("SELECTED_STYLE", prompt)
            .putString("SELECTED_STYLE_NAME", styleName)
            .apply()

        Toast.makeText(requireContext(), "$styleName konsepti seçildi!", Toast.LENGTH_SHORT).show()

        if (activity is MainActivity) {
            (requireActivity() as MainActivity).simulateNavigationClick(R.id.custom_nav_create)
        }
    }
}