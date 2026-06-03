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

            // Seçilen ham görseli hafızaya kaydet
            val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putString("STUDIO_HAM_GORSEL", uri.toString()).apply()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("BuzzAI_Prefs", Context.MODE_PRIVATE)

        // Konsepti, ham görseli ve üretilen görseli hafızadan oku ve ekrana bas (Fil Hafızası)
        fun restoreStudioState() {
            val savedStylePromptPref = sharedPref.getString("SELECTED_STYLE", null)
            val savedStyleNamePref = sharedPref.getString("SELECTED_STYLE_NAME", "Serbest Üretim")

            binding.tvActiveStyleName.text = savedStyleNamePref
            selectedStylePrompt = savedStylePromptPref ?: "Cinematic high-end product photography"

            val hamGorselString = sharedPref.getString("STUDIO_HAM_GORSEL", null)
            if (hamGorselString != null) {
                selectedImageUri = Uri.parse(hamGorselString)
                binding.imageView.setImageURI(selectedImageUri)
            } else {
                binding.imageView.setImageDrawable(null)
            }

            val uretilenGorselString = sharedPref.getString("STUDIO_URETILEN_GORSEL", null)
            if (uretilenGorselString != null) {
                binding.layoutResult.isVisible = true
                binding.ivResult.setImageURI(Uri.parse(uretilenGorselString))
            } else {
                binding.layoutResult.isVisible = false
            }
        }

        restoreStudioState()

        // YENİLE/SIFIRLA BUTONU (Hafızayı temizler ve ekranı sıfırlar)
        binding.btnRefreshConcept.setOnClickListener {
            sharedPref.edit()
                .remove("SELECTED_STYLE")
                .remove("SELECTED_STYLE_NAME")
                .remove("STUDIO_HAM_GORSEL")
                .remove("STUDIO_URETILEN_GORSEL")
                .apply()

            selectedImageUri = null
            restoreStudioState()
            Toast.makeText(requireContext(), "Stüdyo Sıfırlandı!", Toast.LENGTH_SHORT).show()
        }

        binding.pickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Sonuca tıklayınca büyüt (hide & add tekniği ile sayfa ölmez)
        binding.cardResult.setOnClickListener {
            val uretilenGorselString = sharedPref.getString("STUDIO_URETILEN_GORSEL", null)
            if (uretilenGorselString != null) {
                parentFragmentManager.beginTransaction()
                    .hide(this@StudioFragment)
                    .add(R.id.fragment_container, ResultFragment().apply { arguments = bundleOf("imageUrl" to uretilenGorselString) })
                    .addToBackStack(null)
                    .commit()
            }
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

                    val imagePart = MultipartBody.Part.createFormData("image", "image.png", requestFile)
                    val promptReq = selectedStylePrompt.toRequestBody("text/plain".toMediaTypeOrNull())
                    val modeReq = "image-to-image".toRequestBody("text/plain".toMediaTypeOrNull())
                    val strengthReq = "0.45".toRequestBody("text/plain".toMediaTypeOrNull()) // Strength düşürüldü ki kedi kediye benzesin
                    val outputFormatReq = "png".toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = RetrofitClient.instance.generateImage(
                        apiKey = API_KEY,
                        image = imagePart,
                        prompt = promptReq,
                        mode = modeReq,
                        strength = strengthReq,
                        outputFormat = outputFormatReq
                    )

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            decrementCredit(sharedPref)
                            val resultFile = File(requireContext().cacheDir, "result_${System.currentTimeMillis()}.png")
                            FileOutputStream(resultFile).use { it.write(response.body()!!.bytes()) }
                            val resultUri = Uri.fromFile(resultFile).toString()

                            saveToHistory(sharedPref, resultUri)

                            // Üretilen görseli Studio hafızasına da ekle
                            sharedPref.edit().putString("STUDIO_URETILEN_GORSEL", resultUri).apply()

                            binding.layoutResult.isVisible = true
                            binding.ivResult.setImageURI(Uri.parse(resultUri))

                            val savedStyleName = sharedPref.getString("SELECTED_STYLE_NAME", "Serbest Üretim")
                            Toast.makeText(requireContext(), "$savedStyleName başarıyla uygulandı!", Toast.LENGTH_LONG).show()

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