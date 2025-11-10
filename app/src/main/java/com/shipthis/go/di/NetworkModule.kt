package com.shipthis.go.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shipthis.go.data.api.AuthApiService
import com.shipthis.go.data.api.GoBuildApiService
import com.shipthis.go.data.repository.AuthRepository
import com.shipthis.go.util.BackendUrlProvider
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authRepository: Lazy<AuthRepository> // Use Lazy to break circular dependency
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            // Get JWT from auth repository
            val jwt = authRepository.get().getJwt()

            val newRequest = if (jwt != null) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $jwt")
                    .build()
            } else {
                originalRequest
            }

            val response = chain.proceed(newRequest)

            // Handle 401 - clear invalid JWT
            if (response.code == 401) {
                authRepository.get().handleUnauthorized()
            }

            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BackendUrlProvider.getApiUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGoBuildApiService(retrofit: Retrofit): GoBuildApiService {
        return retrofit.create(GoBuildApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}
