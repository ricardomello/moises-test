package com.ricardomello.moisestest.home.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ricardomello.moisestest.BuildConfig
import com.ricardomello.moisestest.shared.data.remote.MusicRemoteDataSource
import com.ricardomello.moisestest.home.data.ItunesSearchApi
import com.ricardomello.moisestest.home.data.SongRepositoryImpl
import com.ricardomello.moisestest.shared.data.local.AppDatabase
import com.ricardomello.moisestest.shared.data.local.SongDao
import com.ricardomello.moisestest.home.data.remote.ItunesRemoteDataSource
import com.ricardomello.moisestest.shared.domain.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingLevel = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = loggingLevel })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideItunesSearchApi(retrofit: Retrofit): ItunesSearchApi =
        retrofit.create(ItunesSearchApi::class.java)

    @Provides
    @Singleton
    fun provideMusicRemoteDataSource(impl: ItunesRemoteDataSource): MusicRemoteDataSource = impl

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS album_tracks")
            }
        }
        val migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN isSearchResult INTEGER NOT NULL DEFAULT 0")
            }
        }
        val migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE songs_new (
                        trackId INTEGER PRIMARY KEY NOT NULL,
                        trackName TEXT NOT NULL,
                        artistName TEXT NOT NULL,
                        collectionId INTEGER NOT NULL,
                        collectionName TEXT,
                        artworkUrl100 TEXT,
                        previewUrl TEXT,
                        primaryGenreName TEXT,
                        trackTimeMillis INTEGER,
                        releaseDate TEXT,
                        lastPlayedAt INTEGER DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO songs_new
                    SELECT trackId, trackName, artistName, collectionId, collectionName,
                           artworkUrl100, previewUrl, primaryGenreName, trackTimeMillis,
                           releaseDate, NULL
                    FROM songs
                """.trimIndent())
                db.execSQL("DROP TABLE songs")
                db.execSQL("ALTER TABLE songs_new RENAME TO songs")
            }
        }
        val migration5To6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN trackNumber INTEGER DEFAULT NULL")
            }
        }
        return Room.databaseBuilder(context, AppDatabase::class.java, "moises_test.db")
            .addMigrations(migration2To3, migration3To4, migration4To5, migration5To6)
            .build()
    }

    @Provides
    @Singleton
    fun provideSongDao(db: AppDatabase): SongDao = db.songDao()

    @Provides
    @Singleton
    fun provideSongRepository(impl: SongRepositoryImpl): SongRepository = impl
}
