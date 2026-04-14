package com.ricardomello.moisestest.album.di

import com.ricardomello.moisestest.album.data.AlbumRepositoryImpl
import com.ricardomello.moisestest.shared.domain.AlbumRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlbumModule {

    @Provides
    @Singleton
    fun provideAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository = impl
}
