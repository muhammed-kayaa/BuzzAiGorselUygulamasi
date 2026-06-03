package com.example.buzzai

import android.content.ContentValues
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.example.buzzai.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

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

        loadedImageUrl = arguments?.getString("imageUrl")
        if (!loadedImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(loadedImageUrl)
                .into(binding.resultImageView)
        }

        // --- İNDİR BUTONU ---
        binding.btnDownload.setOnClickListener {
            val drawable = binding.resultImageView.drawable
            if (drawable != null) {
                val bitmap = drawable.toBitmap()
                saveImageToGallery(bitmap)
            } else {
                Toast.makeText(requireContext(), "Görsel henüz yüklenmedi!", Toast.LENGTH_SHORT).show()
            }
        }

        // --- PAYLAŞ BUTONU ---
        binding.btnShare.setOnClickListener {
            val drawable = binding.resultImageView.drawable
            if (drawable != null) {
                val bitmap = drawable.toBitmap()
                shareImage(bitmap)
            } else {
                Toast.makeText(requireContext(), "Görsel henüz yüklenmedi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "BuzzAI_${System.currentTimeMillis()}.png"
        var fos: java.io.OutputStream? = null
        var imageUri: Uri? = null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

                Toast.makeText(requireContext(), "Görsel galeriye kaydedildi! 📸", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            imageUri?.let { uri -> contentResolver.delete(uri, null, null) }
            Toast.makeText(requireContext(), "Kaydetme başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Modern Paylaşım Fonksiyonu
    private fun shareImage(bitmap: Bitmap) {
        val filename = "BuzzAI_Share_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BuzzAI")
            }
        }

        val contentResolver = requireContext().contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Görseli Paylaş"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Paylaşım hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}