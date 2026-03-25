package com.nfcmanager.di

import android.content.Context
import com.nfcmanager.data.database.AppDatabase
import com.nfcmanager.data.database.NFCDataDao
import com.nfcmanager.data.repository.NFCRepository
import com.nfcmanager.nfc.NFCManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideNFCDataDao(database: AppDatabase): NFCDataDao {
        return database.nfcDataDao()
    }
    
    @Provides
    @Singleton
    fun provideNFCRepository(nfcDataDao: NFCDataDao): NFCRepository {
        return NFCRepository(nfcDataDao)
    }
    
    @Provides
    @Singleton
    fun provideNFCManager(@ApplicationContext context: Context): NFCManager {
        return NFCManager(context)
    }
}
