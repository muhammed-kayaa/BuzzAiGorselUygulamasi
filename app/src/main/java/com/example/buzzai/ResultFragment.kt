package com.example.buzzai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.buzzai.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    // Gelen resmin yolu
    private var loadedImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Resmi Argument'ten al ve ekranda goster
        loadedImageUrl = arguments?.getString("imageUrl")
        if (!loadedImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(loadedImageUrl)
                .into(binding.resultImageView)
        }

        // --- İNDİR BUTONU TIKLANINCA ÇALIŞACAK KOD ---
        binding.btnDownload.setOnClickListener {
            val drawable = binding.resultImageView.drawable
            if (drawable != null) {
                // Resmi hafızaya al
                val bitmap = drawable.toBitmap()
                saveImageToGallery(bitmap)
            } else {
                Toast.makeText(requireContext(), "Görsel henüz yüklenmedi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Metin Üretme (Eski kodun birebir aynısı)
        binding.btnGenerateCaption.setOnClickListener {
            binding.btnGenerateCaption.isEnabled = false
            binding.tvGenerated.text = "Yapay Zeka metni yazıyor... ⏳"

            lifecycleScope.launch {
                delay(2000)
                val isInstagram = binding.rbInstagram.isChecked
                val generatedCaption = generateCaption(isInstagram)
                binding.tvGenerated.text = generatedCaption

                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("generated_caption", generatedCaption)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(requireContext(), "Metin panoya kopyalandı! ✨", Toast.LENGTH_SHORT).show()
                binding.btnGenerateCaption.isEnabled = true
            }
        }
    }

    // Modern Android Sürümlerinde Galeriye Fotoğraf Kaydetme Fonksiyonu
    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "BuzzAI_${System.currentTimeMillis()}.png"
        var fos: java.io.OutputStream? = null
        var imageUri: Uri? = null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            // Android 10 ve üstü için resimler klasörünü belirtiyoruz
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BuzzAI")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentResolver = requireContext().contentResolver

        try {
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.also { uri ->
                imageUri = uri
                fos = contentResolver.openOutputStream(uri)
                fos?.let {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.close()
                }

                // İndirme bittiyse bayrağı 0 yapıp resmi herkese görünür kıl
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

                Toast.makeText(requireContext(), "Görsel başarıyla galeriye kaydedildi! 📸", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Hata çıkarsa varsa yarım kalmış dosyayı sil
            imageUri?.let { uri ->
                contentResolver.delete(uri, null, null)
            }
            Toast.makeText(requireContext(), "Kaydetme başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateCaption(isInstagram: Boolean): String {
        return if (isInstagram) {
            "✨ Yeni seviyeyi keşfet! ✨ This piece is everything I didn't know I needed — pure elegance meets functionality. 💎😍 The attention to detail is impeccable. Absolutely obsessed! 🖤 #LuxuryStyle #DesignedForSuccess #MustHaveProduct"
        } else {
            "WAIT— did you see this?? 😲 This product is literally INSANE! No cap, I'm so obsessed and you NEED this energy in your life! 🔥✨ #FYP #ForYouPage #Viral #TrendingNow #MustGet"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}