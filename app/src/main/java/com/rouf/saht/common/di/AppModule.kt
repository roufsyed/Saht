package com.rouf.saht.common.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.camera.lifecycle.ProcessCameraProvider
import com.rouf.saht.common.dispatcher.DispatcherProvider
import com.rouf.saht.common.helper.NotificationHelper
import com.rouf.saht.heartRate.repository.HeartRateRepository
import com.rouf.saht.heartRate.repository.HeartRateRepositoryImpl
import com.rouf.saht.pedometer.repository.PedometerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Keep
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide Coroutine Dispatchers
    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = DispatcherProvider.io // For background tasks (e.g., DB, sensor work)

    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = DispatcherProvider.main // For UI-related work

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = DispatcherProvider.default // For UI-related work

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun providePedometerRepository(): PedometerRepository = PedometerRepository()

    @Provides
    @Singleton
    fun provideProcessCameraProvider(
        @ApplicationContext context: Context
    ): ProcessCameraProvider = ProcessCameraProvider.getInstance(context).get()

    @Provides
    @Singleton
    fun provideHeartRateRepository(
        cameraProvider: ProcessCameraProvider
    ): HeartRateRepository = HeartRateRepositoryImpl(cameraProvider)}