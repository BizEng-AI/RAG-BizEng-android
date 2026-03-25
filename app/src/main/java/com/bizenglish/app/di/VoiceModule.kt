package com.bizenglish.app.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.bizenglish.app.voice.SpeechToTextController
import com.bizenglish.app.voice.TextToSpeechController
import com.bizenglish.app.voice.AzureTtsController

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {
    @Provides @Singleton
    fun provideSpeechToTextController(@ApplicationContext ctx: Context): SpeechToTextController =
        SpeechToTextController(ctx as Application)

    @Provides @Singleton
    fun provideTextToSpeechController(@ApplicationContext ctx: Context): TextToSpeechController =
        AzureTtsController(ctx as Application) as TextToSpeechController
}

