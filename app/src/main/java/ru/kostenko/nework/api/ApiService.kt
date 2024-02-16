package ru.kostenko.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.kostenko.nework.dto.Media
import ru.kostenko.nework.dto.Post
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

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

//    Posts
    @GET("posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Int): Response<List<Post>>

    @GET("posts/{id}/before") //Загружает посты до
    suspend fun getBeforePost(@Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after") //Загружает посты после
    suspend fun getAfterPost(@Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removePostById(@Path("id") id: Int): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Int): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") id: Int): Response<Post>

    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    //Media
    @Multipart
    @POST("media")
    suspend fun saveMediaOnServer(@Part part: MultipartBody.Part): Response<Media>

    //EVENTS


}