package com.ricardomello.moisestest.player.di

import android.content.Context
import android.content.ComponentName
import androidx.media3.common.MediaItem
import com.ricardomello.moisestest.player.SongPlayer
import com.ricardomello.moisestest.player.SongPlayerImpl
import com.ricardomello.moisestest.player.MusicController
import com.ricardomello.moisestest.player.MusicControllerImpl
import com.ricardomello.moisestest.player.MediaItemFactory
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.ricardomello.moisestest.player.service.MusicPlayerService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindSongPlayer(impl: SongPlayerImpl): SongPlayer

    companion object {
        @Provides
        @Singleton
        fun provideMusicController(
            controllerFuture: ListenableFuture<MediaController>,
            mediaItemFactory: MediaItemFactory,
        ): MusicController = MusicControllerImpl(controllerFuture, mediaItemFactory)

        @Provides
        @Singleton
        fun provideMediaItemFactory(): MediaItemFactory = MediaItemFactory { url ->
            MediaItem.fromUri(url)
        }

        @Provides
        @Singleton
        fun provideMediaControllerFuture(
            @ApplicationContext context: Context,
        ): ListenableFuture<MediaController> = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, MusicPlayerService::class.java)),
        ).buildAsync()
    }
}
