package com.example.buzzai

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.buzzai.databinding.FragmentUretBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

/**
 * Fragment that lets the user pick an image from the gallery and send it to the server.
 * Uses ViewBinding (FragmentUretBinding). The layout should expose at least:
 * - ImageView with id: imageView
 * - Button with id: pickImageButton
 * - Button with id: sendImageButton
 */
class UretFragment : Fragment() {

	private var _binding: FragmentUretBinding? = null
	private val binding get() = _binding!!

	private var selectedImageUri: Uri? = null

	// Register the picker before the fragment is created
	private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
		if (uri != null) {
			selectedImageUri = uri
			// Display selected image
			binding.imageView.setImageURI(uri)
		} else {
			Toast.makeText(requireContext(), "No media selected", Toast.LENGTH_SHORT).show()
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
			// Launch the gallery picker for images only
			pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
		}

		binding.sendImageButton.setOnClickListener {
			val uri = selectedImageUri
			if (uri == null) {
				Toast.makeText(requireContext(), "Please pick an image first", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			lifecycleScope.launch {
				try {
					val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
					if (bytes == null) {
						Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
						return@launch
					}

					val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
					val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

					val promptRequest = "Generate a professional product ad".toRequestBody("text/plain".toMediaTypeOrNull())
					val promptPart = MultipartBody.Part.createFormData("prompt", null, promptRequest)

					// API'den doğrudan ImageResponse objesi geliyor, Response değil.
					val result = RetrofitClient.instance.processProductImage(part, promptPart)

					// Başarılı bir şekilde döndüyse result.resultUrl elimizdedir.
					Toast.makeText(requireContext(), "Başarılı! URL: ${result.resultUrl}", Toast.LENGTH_LONG).show()

				} catch (e: Exception) {
					e.printStackTrace()
					Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}