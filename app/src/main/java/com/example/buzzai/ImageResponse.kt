package com.example.buzzai // Android Studio bunu otomatik ekler, eklemezse sen yaz

import com.google.gson.annotations.SerializedName

data class ImageResponse(
    @SerializedName("result_url")
    val resultUrl: String
)