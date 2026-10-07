@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.di

import android.content.Context
import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdBindingsModule {

    @Provides
    @Singleton
    fun retailMediaAdRepository(@ApplicationContext context: Context): RetailMediaAdRepository =
        RetailMediaAdRepository(context)
}
