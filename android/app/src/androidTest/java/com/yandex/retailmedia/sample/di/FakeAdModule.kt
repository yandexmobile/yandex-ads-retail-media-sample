@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.di

import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.clearMocks
import io.mockk.mockk
import javax.inject.Singleton

object AdTestDoubles {
    val repository: RetailMediaAdRepository = mockk()

    fun reset() {
        clearMocks(repository)
    }
}

@TestInstallIn(components = [SingletonComponent::class], replaces = [AdBindingsModule::class])
@Module
object FakeAdModule {

    @Provides
    @Singleton
    fun repository(): RetailMediaAdRepository = AdTestDoubles.repository
}
