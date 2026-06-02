package com.example.buzzai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.buzzai.databinding.FragmentResultBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

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

        // Load image from arguments using Glide
        val imageUrl = arguments?.getString("imageUrl")
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .centerCrop()
                .into(binding.resultImageView)
        }

        // Generate caption button click listener
        binding.btnGenerateCaption.setOnClickListener {
            // Disable button and show loading state
            binding.btnGenerateCaption.isEnabled = false
            binding.tvGenerated.text = "Yapay Zeka metni yazıyor... ⏳"

            // Simulate API call with 2-second delay
            lifecycleScope.launch {
                delay(2000) // 2 seconds delay simulating API

                // Determine selected platform and generate caption
                val isInstagram = binding.rbInstagram.isChecked
                val generatedCaption = generateCaption(isInstagram)

                // Display result
                binding.tvGenerated.text = generatedCaption

                // Copy to clipboard
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("generated_caption", generatedCaption)
                clipboard.setPrimaryClip(clip)

                // Show success toast
                Toast.makeText(requireContext(), "Metin panoya kopyalandı! ✨", Toast.LENGTH_SHORT).show()

                // Re-enable button
                binding.btnGenerateCaption.isEnabled = true
            }
        }
    }

    /**
     * Generate a platform-specific caption.
     * Instagram: Professional tone with luxury hashtags.
     * TikTok: Viral, energetic tone with #fyp and #viral hashtags.
     */
    private fun generateCaption(isInstagram: Boolean): String {
        return if (isInstagram) {
            // Instagram: Professional, luxury-focused tone
            "✨ Yeni seviyeyi keşfet! ✨ This piece is everything I didn't know I needed — pure elegance meets functionality. 💎😍 The attention to detail is impeccable. Absolutely obsessed! 🖤 #LuxuryStyle #DesignedForSuccess #MustHaveProduct"
        } else {
            // TikTok: Viral, energetic tone
            "WAIT— did you see this?? 😲 This product is literally INSANE! No cap, I'm so obsessed and you NEED this energy in your life! 🔥✨ #FYP #ForYouPage #Viral #TrendingNow #MustGet"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

