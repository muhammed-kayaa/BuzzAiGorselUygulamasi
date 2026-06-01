package com.example.buzzai

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // Yapay zekaya fotoğrafı ve promptu göndereceğimiz uç nokta
    @Multipart
    @POST("v1/image-to-image")
    suspend fun processProductImage(
        @Part image: MultipartBody.Part,
        @Part prompt: MultipartBody.Part
    ): ImageResponse

}