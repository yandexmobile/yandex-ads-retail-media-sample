package com.yandex.retailmedia.sample.di

import com.yandex.retailmedia.sample.data.ads.AdSessionTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    @Singleton
    fun adSessionTracker(): AdSessionTracker = AdSessionTracker()
}
