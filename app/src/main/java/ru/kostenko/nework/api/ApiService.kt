package ru.kostenko.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.kostenko.nework.dto.Token
import ru.kostenko.nework.dto.User


interface ApiService {
//    Users
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token> //Запрос на Login

    @Multipart
    @POST("users/registration")
    suspend fun newUser(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part file: MultipartBody.Part
    ):Response<Token> //Запрос на Registration

    @GET("/api/users")
    suspend fun getUsers(): Response<List<User>>

    @GET("/api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>


}