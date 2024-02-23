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
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.Job
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

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @POST("events/{id}/participants")
    suspend fun saveEventParticipants(@Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun deleteEventParticipants(@Path("id") id: Int): Response<Event>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Int): Response<Event>

    @GET("events/{id}/newer")
    suspend fun getNewerEvents(@Path("id") id: Int): Response<List<Event>>

    @GET("events/{id}/before") //Загружает посты до
    suspend fun getBeforeEvent(@Path("id") id: Int, @Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/after") //Загружает посты после
    suspend fun getAfterEvent(@Path("id") id: Int, @Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Int): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Int): Response<Unit>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    //Wall
    @POST("{authorId}/wall/{id}/likes")
    suspend fun likePostByIdOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int): Response<Post>

    @DELETE("{authorId}/wall/{id}/likes")
    suspend fun dislikePostByIdOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int): Response<Post>

    @GET("{authorId}/wall")
    suspend fun getWall(@Path("authorId") authorId: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}/newer")
    suspend fun getNewerPostsOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}/before") //Загружает посты до
    suspend fun getBeforePostOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}/after") //Загружает посты после
    suspend fun getAfterPostOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int, @Query("count") count: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}")
    suspend fun getPostByIdOnWall(@Path("authorId") authorId: Int, @Path("id") id: Int): Response<Post>

    @GET("{authorId}/wall/latest")
    suspend fun getLatestPostsOnWall(@Path("authorId") authorId: Int, @Query("count") count: Int): Response<List<Post>>

    //JOBs
    @GET("{userId}/jobs")
    suspend fun getJobs(@Path("userId") userId: Int): Response<List<Job>>

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun setMyJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun deleteMyJobs(@Path("id") jobId: Int): Response<Unit>
}