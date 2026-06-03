package com.example.buzzai

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.buzzai.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)

        // KREDİ GÖSTERİMİ
        val kalanKredi = sharedPref.getInt("DAILY_CREDITS", 3)
        binding.tvCredits.text = "Kalan Günlük Kredi: $kalanKredi/3"

        // 1. KULLANICI AYARLARI (Göstermelik havalı menü açılır)
        binding.cardKullaniciAyarlari.setOnClickListener {
            val options = arrayOf("Profili Düzenle", "Şifre Değiştir", "Hesabı Sil")
            AlertDialog.Builder(requireContext())
                .setTitle("Kullanıcı Ayarları")
                .setItems(options) { _, which ->
                    Toast.makeText(requireContext(), "${options[which]} seçeneği yakında aktif olacak.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Kapat", null)
                .show()
        }

        // 2. BİLDİRİMLER
        val isNotifEnabled = sharedPref.getBoolean("NOTIFICATIONS_ENABLED", true)
        binding.swNotifications.isChecked = isNotifEnabled
        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("NOTIFICATIONS_ENABLED", isChecked).apply()
            val msg = if (isChecked) "Bildirimler Açıldı" else "Bildirimler Kapatıldı"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // 3. DİL SEÇENEKLERİ
        binding.cardDil.setOnClickListener {
            Toast.makeText(requireContext(), "Dil seçenekleri çok yakında eklenecek!", Toast.LENGTH_SHORT).show()
        }

        // 4. INSTAGRAM HESABI (Doğrudan Stratejik Ajans sayfasına gider)
        binding.cardInstagram.setOnClickListener {
            val uri = Uri.parse("https://www.instagram.com/stratejikajans/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.instagram.android") // Önce uygulamayı açmayı dener
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Uygulama yoksa tarayıcıda açar
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }

        // 5. PAYLAŞ
        binding.cardPaylas.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "BuzzAI ile harika yapay zeka görselleri üretiyorum! Sen de hemen denemelisin.")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "BuzzAI'yi Paylaş"))
        }

        // 6. PUAN VER
        binding.cardPuanVer.setOnClickListener {
            val appPackageName = requireContext().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        // 7. BİZE ULAŞIN
        binding.cardBizeUlasin.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("iletisim@buzzai.com"))
                putExtra(Intent.EXTRA_SUBJECT, "BuzzAI Uygulaması Hakkında")
            }
            try {
                startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "Cihazda kurulu bir e-posta uygulaması bulunamadı.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}