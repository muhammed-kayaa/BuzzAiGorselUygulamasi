package com.example.buzzai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.buzzai.databinding.FragmentStudioBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class StudioFragment : Fragment() {

    private var _binding: FragmentStudioBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedStylePrompt: String = "Cinematic high-end product photography"
    private val API_KEY = "Bearer sk-V96IDHZGbcP1UMbuZcePI3i5edRtuMUnVenpdSpT9w0nLbvC"

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imageView.setImageURI(uri)
        }
    }

    private fun ensureCreditsUpToDate(prefs: android.content.SharedPreferences) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val lastYear = prefs.getInt("CREDITS_LAST_YEAR", -1)
        val lastDay = prefs.getInt("CREDITS_LAST_DAY_OF_YEAR", -1)

        if (lastYear != todayYear || lastDay != todayDay) {
            prefs.edit().putInt("DAILY_CREDITS", 3)
                .putInt("CREDITS_LAST_YEAR", todayYear)
                .putInt("CREDITS_LAST_DAY_OF_YEAR", todayDay).apply()
        }
    }

    private fun decrementCredit(prefs: android.content.SharedPreferences) {
        val current = prefs.getInt("DAILY_CREDITS", 3)
        prefs.edit().putInt("DAILY_CREDITS", (current - 1).coerceAtLeast(0)).apply()
    }

    private fun saveToHistory(prefs: android.content.SharedPreferences, uri: String) {
        val currentHistory = prefs.getString("BUZZAI_HISTORY", "") ?: ""
        val newHistory = if (currentHistory.isEmpty()) uri else "$uri,$currentHistory"
        prefs.edit().putString("BUZZAI_HISTORY", newHistory).apply()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)

        binding.pickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.sendImageButton.setOnClickListener {
            val uri = selectedImageUri
            if (uri == null) {
                Toast.makeText(requireContext(), "Lütfen görsel yükleyin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ensureCreditsUpToDate(sharedPref)
            if (sharedPref.getInt("DAILY_CREDITS", 0) <= 0) {
                Toast.makeText(requireContext(), "Krediniz bitti!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.sendImageButton.isEnabled = false
            binding.pickImageButton.isEnabled = false
            binding.progressIndicator.isVisible = true
            showProcessingNotification()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = getBitmapFromUri(uri)
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
                    val stream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val requestFile = stream.toByteArray().toRequestBody("image/png".toMediaTypeOrNull())
                    // İstek hazırlığı
                    val imagePart = MultipartBody.Part.createFormData("image", "image.png", requestFile)
                    val promptReq = selectedStylePrompt.toRequestBody("text/plain".toMediaTypeOrNull())
                    val modeReq = "image-to-image".toRequestBody("text/plain".toMediaTypeOrNull()) // Burası çok kritik!
                    val outputFormatReq = "png".toRequestBody("text/plain".toMediaTypeOrNull())

// İstek atıyoruz (Mode parametresini ekledik)
                    val response = RetrofitClient.instance.generateImage(
                        apiKey = API_KEY,
                        image = imagePart,
                        prompt = promptReq,
                        mode = modeReq, // <--- API'nin istediği parametre buraya geldi
                        outputFormat = outputFormatReq
                    )

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            decrementCredit(sharedPref)
                            val resultFile = File(requireContext().cacheDir, "result_${System.currentTimeMillis()}.png")
                            FileOutputStream(resultFile).use { it.write(response.body()!!.bytes()) }
                            val resultUri = Uri.fromFile(resultFile).toString()

                            saveToHistory(sharedPref, resultUri)
                            binding.layoutResult.isVisible = true
                            binding.ivResult.setImageURI(Uri.parse(resultUri))
                            binding.cardResult.setOnClickListener {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, ResultFragment().apply { arguments = bundleOf("imageUrl" to resultUri) })
                                    .addToBackStack(null).commit()
                            }
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Hata: ${response.code()}"
                            Toast.makeText(requireContext(), "SD3 Hatası: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show() }
                } finally {
                    withContext(Dispatchers.Main) {
                        binding.progressIndicator.isVisible = false
                        binding.sendImageButton.isEnabled = true
                        binding.pickImageButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
    else MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

    private fun showProcessingNotification() {
        val channelId = "buzzai_channel"
        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "AI", NotificationManager.IMPORTANCE_DEFAULT))
        }
        manager.notify(1, NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("BuzzAI SD3").setContentText("İşleniyor...").build())
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}