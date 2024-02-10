package ru.kostenko.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.kostenko.nework.dto.PushToken
import ru.kostenko.nework.dto.Token
import java.io.File

interface ApiService {
//    Авторизация
    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken): Response<Unit>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token> //Запрос на Login

    @Multipart
    @POST("users/registration")
    suspend fun newUser(
        @Part("login") login: String,
        @Part("pass") pass: String,
        @Part("name") name: String,
        @Part file: MultipartBody.Part
    ):Response<Token> //Запрос на Registration
}