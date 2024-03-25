package ru.kostenko.nework.api

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.kostenko.nework.BuildConfig
import ru.kostenko.nework.authorization.AppAuth
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
    }
    @Singleton
    @Provides
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

    @Singleton
    @Provides
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            appAuth.authStateFlow.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .addInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .addHeader("Api-Key", BuildConfig.REQ_API_KEY)
                    .build()
            )
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        clientOkHttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder().registerTypeAdapter(
                OffsetDateTime :: class.java,
                object : TypeAdapter<OffsetDateTime>(){
                    override fun write(out: JsonWriter, value: OffsetDateTime?){
                        out.value(value?.toEpochSecond())
                    }
                    override fun read(jsonReader: JsonReader): OffsetDateTime =
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(jsonReader.nextLong())
                            , ZoneId.systemDefault()
                        )
                }).create()
        )
        ).client(clientOkHttp)
        .baseUrl(BASE_URL)
        .build()

    @Singleton
    @Provides
    fun provideApiService(
        retrofit: Retrofit,
    ): ApiService = retrofit.create()
}