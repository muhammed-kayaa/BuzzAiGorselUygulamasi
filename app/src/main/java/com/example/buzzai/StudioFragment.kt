package com.example.buzzai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
// Glide removed from this fragment (unused here)
import com.example.buzzai.databinding.FragmentStudioBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar

class StudioFragment : Fragment() {

    private var _binding: FragmentStudioBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    // Varsayılan tarzımız (Eğer kullanıcı hiçbir şey seçmezse bu çalışır)
    private var selectedStylePrompt: String = "Cinematic high-end product photography"

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Kullanıcının yüklediği ham fotoğrafı çerçevede gösteriyoruz
            binding.imageView.setImageURI(uri)
        } else {
            Toast.makeText(requireContext(), "Görsel seçilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    // Ensure that credits are reset daily. If last reset day != today, reset to MAX (3)
    private fun ensureCreditsUpToDate(prefs: android.content.SharedPreferences) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)

        val lastYear = prefs.getInt("CREDITS_LAST_YEAR", -1)
        val lastDay = prefs.getInt("CREDITS_LAST_DAY_OF_YEAR", -1)

        if (lastYear != todayYear || lastDay != todayDay) {
            prefs.edit().putInt("DAILY_CREDITS", 3)
                .putInt("CREDITS_LAST_YEAR", todayYear)
                .putInt("CREDITS_LAST_DAY_OF_YEAR", todayDay)
                .apply()
        }
    }

    private fun decrementCredit(prefs: android.content.SharedPreferences) {
        val current = prefs.getInt("DAILY_CREDITS", 3)
        val next = (current - 1).coerceAtLeast(0)
        prefs.edit().putInt("DAILY_CREDITS", next).apply()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. ADIM: EĞER KULLANICI ANA SAYFADAN (KEŞFET) BİR TARZ SEÇİP GELDİYSE HAFIZADAN ONU OKU
        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
        val savedStyle = sharedPref.getString("SELECTED_STYLE", null)

        if (savedStyle != null) {
            selectedStylePrompt = savedStyle // Ana sayfadaki tarzı motorun beynine yazdık
            // Motor tarzı aldıktan sonra hafızayı temizleyelim ki hep o tarz takılı kalmasın
            sharedPref.edit().remove("SELECTED_STYLE").apply()
        }

        // --- STÜDYO İÇİNDEKİ MANUEL KART SEÇİMLERİ ---

        // Kullanıcı üstteki "Lüks Mermer" şablonuna tıklarsa
        binding.cardStyle1.setOnClickListener {
            selectedStylePrompt = "Product placed on a luxury marble countertop, studio lighting, 8k cinematic"
            Toast.makeText(requireContext(), "Tarz Seçildi: Lüks Mermer", Toast.LENGTH_SHORT).show()
        }

        // Kullanıcı "Ahşap Zemin" şablonuna tıklarsa
        binding.cardStyle2.setOnClickListener {
            selectedStylePrompt = "Product placed on a rustic wooden table, warm cozy lighting, professional ad"
            Toast.makeText(requireContext(), "Tarz Seçildi: Ahşap Zemin", Toast.LENGTH_SHORT).show()
        }

        // Kullanıcı "Doğal Işık" şablonuna tıklarsa
        binding.cardStyle3.setOnClickListener {
            selectedStylePrompt = "Product outdoor in nature, natural sunlight, green leaves background, photorealistic"
            Toast.makeText(requireContext(), "Tarz Seçildi: Doğal Işık", Toast.LENGTH_SHORT).show()
        }

        // FOTOĞRAF SEÇME BUTONU
        binding.pickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // ÜRETİM BUTONU VE API'YE GÖNDERİM
        binding.sendImageButton.setOnClickListener {
            val uri = selectedImageUri
            if (uri == null) {
                Toast.makeText(requireContext(), "Lütfen ham ürün görselini yükleyin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Günlük kredi kontrolü
            val prefs = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
            ensureCreditsUpToDate(prefs)
            val remaining = prefs.getInt("DAILY_CREDITS", 3)
            if (remaining <= 0) {
                Toast.makeText(requireContext(), "Günlük kredi hakkınız doldu. Yarın tekrar deneyin.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Kredi varsa ilerle ve bir kredi tüket
            decrementCredit(prefs)

            // Disable UI and show central modern progress indicator
            binding.sendImageButton.isEnabled = false
            binding.pickImageButton.isEnabled = false
            binding.progressIndicator.isVisible = true

            // O efsanevi bildirim fırlatılıyor (İleri Özellik - 20 Puan)
            showProcessingNotification()

            lifecycleScope.launch {
                try {
                    val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        Toast.makeText(requireContext(), "Görsel okunamadı", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Görseli formata çevir
                    val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

                    // Seçilen tarzı API'ye komut olarak yolla
                    val promptRequest = selectedStylePrompt.toRequestBody("text/plain".toMediaTypeOrNull())
                    val promptPart = MultipartBody.Part.createFormData("prompt", null, promptRequest)

                    // API İSTEĞİ (Retrofit)
                    val result = RetrofitClient.instance.processProductImage(part, promptPart)

                    // Başarılıysa, gezinti ile ResultFragment'e geç ve URL'i gönder
                    val bundle = bundleOf("imageUrl" to result.resultUrl)
                    val fragment = ResultFragment().apply { arguments = bundle }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    // Her durumda yükleme durumunu kapat
                    binding.progressIndicator.isVisible = false
                    binding.sendImageButton.isEnabled = true
                    binding.pickImageButton.isEnabled = true
                }
            }
        }
    }

    private fun showProcessingNotification() {
        val channelId = "buzzai_channel"
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Yapay Zeka İşlemleri", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("BuzzAI Stüdyo")
            .setContentText("Ürününüz seçtiğiniz tarza uyarlanıyor. Lütfen bekleyin...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1, builder.build())
            }
        } else {
            notificationManager.notify(1, builder.build())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}