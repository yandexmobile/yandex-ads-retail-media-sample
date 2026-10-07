package com.yandex.retailmedia.sample.di

import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.CartRepositoryImpl
import com.yandex.retailmedia.sample.data.CatalogRepository
import com.yandex.retailmedia.sample.data.CatalogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository
}
