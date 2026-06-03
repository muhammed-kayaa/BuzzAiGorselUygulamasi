package com.example.buzzai

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
interface ApiService {
    @Multipart
    @POST("v2beta/stable-image/generate/sd3")
    suspend fun generateImage(
        @Header("Authorization") apiKey: String,
        @Header("Accept") accept: String = "image/png",
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody,
        @Part("mode") mode: RequestBody, // API bunu istiyor!
        @Part("output_format") outputFormat: RequestBody
    ): Response<ResponseBody>
}