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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.buzzai.databinding.FragmentUretBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UretFragment : Fragment() {

	private var _binding: FragmentUretBinding? = null
	private val binding get() = _binding!!

	private var selectedImageUri: Uri? = null

	private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
		if (uri != null) {
			selectedImageUri = uri
			binding.imageView.setImageURI(uri)
		} else {
			Toast.makeText(requireContext(), "Görsel seçilmedi", Toast.LENGTH_SHORT).show()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentUretBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.pickImageButton.setOnClickListener {
			pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
		}

		binding.sendImageButton.setOnClickListener {
			val uri = selectedImageUri
			if (uri == null) {
				Toast.makeText(requireContext(), "Lütfen önce galeriden bir görsel seçin", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			// KULLANICI BUTONA BASTIĞI AN BİLDİRİMİ ATEŞLİYORUZ (İleri Özellik - 20 Puan)
			showProcessingNotification()

			lifecycleScope.launch {
				try {
					val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
					if (bytes == null) {
						Toast.makeText(requireContext(), "Görsel okunamadı", Toast.LENGTH_SHORT).show()
						return@launch
					}

					val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
					val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

					val promptRequest = "Generate a professional product ad".toRequestBody("text/plain".toMediaTypeOrNull())
					val promptPart = MultipartBody.Part.createFormData("prompt", null, promptRequest)

					// API'ye İstek Atılıyor
					val result = RetrofitClient.instance.processProductImage(part, promptPart)

					Toast.makeText(requireContext(), "Başarılı! URL: ${result.resultUrl}", Toast.LENGTH_LONG).show()

				} catch (e: Exception) {
					e.printStackTrace()
					Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	// BİLDİRİMİ OLUŞTURAN FONKSİYON
	private fun showProcessingNotification() {
		val channelId = "buzzai_channel"
		val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// Modern Android sürümleri için Kanal (Channel) oluşturma zorunluluğu
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(channelId, "Yapay Zeka İşlemleri", NotificationManager.IMPORTANCE_DEFAULT)
			notificationManager.createNotificationChannel(channel)
		}

		// Bildirimin Görünümü
		val builder = NotificationCompat.Builder(requireContext(), channelId)
			.setSmallIcon(android.R.drawable.ic_menu_camera)
			.setContentTitle("BuzzAI Stüdyo")
			.setContentText("Görseliniz yapay zeka ile işleniyor. Lütfen bekleyin...")
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)

		// Android 13 ve üstü için güvenlik izni kontrolü (Hatanın Çözümü)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				notificationManager.notify(1, builder.build())
			} else {
				// Eğer telefonda bildirim izni henüz verilmemişse uygulama çökmesin, sadece uyarı versin
				Toast.makeText(requireContext(), "Bildirim izni gerekli, ancak arka planda işlem devam ediyor...", Toast.LENGTH_SHORT).show()
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