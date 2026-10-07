package com.yandex.retailmedia.sample.di

import com.yandex.retailmedia.sample.data.api.StoreApi
import com.yandex.retailmedia.sample.data.api.storeJson
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
@Module
object TestNetworkModule {

    @Provides
    @Singleton
    fun storeApi(): StoreApi {
        val baseUrl = System.getProperty("test.baseUrl") ?: "http://localhost:8089/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient())
            .addConverterFactory(storeJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(StoreApi::class.java)
    }
}
