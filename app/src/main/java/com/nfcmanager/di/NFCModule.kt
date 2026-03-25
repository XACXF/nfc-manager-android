package com.nfcmanager.di

import android.content.Context
import com.nfcmanager.nfc.NFCWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NFCModule {
    
    @Provides
    @Singleton
    fun provideNFCWriter(
        @ApplicationContext context: Context
    ): NFCWriter {
        return NFCWriter(context)
    }
}