package com.example.myapplication.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.myapplication.voice.SpeechToTextController
import com.example.myapplication.voice.TextToSpeechController
import com.example.myapplication.voice.AzureTtsController

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {
    @Provides @Singleton
    fun provideSpeechToTextController(@ApplicationContext ctx: Context): SpeechToTextController =
        SpeechToTextController(requireNotNull(ctx as? Application) { "Application context required" })

    @Provides @Singleton
    fun provideTextToSpeechController(@ApplicationContext ctx: Context): TextToSpeechController =
        AzureTtsController(requireNotNull(ctx as? Application) { "Application context required" })
}
