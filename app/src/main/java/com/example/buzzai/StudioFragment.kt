package com.example.buzzai

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class StudioFragment : Fragment() {

    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnGenerate: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null

    // DÜZELTİLDİ: Sözleşme doğru şekilde ActivityResultContracts içerisinden çağrıldı
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .centerCrop()
                .into(ivProductImage)
            btnGenerate.isEnabled = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_studio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProductImage = view.findViewById(R.id.ivProductImage)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        progressBar = view.findViewById(R.id.progressBar)

        btnSelectImage.setOnClickListener {
            // DÜZELTİLDİ: Launch ederken PickVisualMediaRequest kullanıyoruz
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnGenerate.setOnClickListener {
            // Bir sonraki adımda Retrofit'i buraya bağlayacağız!
        }
    }
}