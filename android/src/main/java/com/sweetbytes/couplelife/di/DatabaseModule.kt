package com.sweetbytes.couplelife.di

import android.content.Context
import androidx.room.Room
import com.sweetbytes.couplelife.data.local.AppDatabase
import com.sweetbytes.couplelife.data.local.dao.EntryDao
import com.sweetbytes.couplelife.data.repository.EntryRepositoryImpl
import com.sweetbytes.couplelife.domain.repository.EntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "couplelife.db").build()

    @Provides
    fun provideEntryDao(db: AppDatabase): EntryDao = db.entryDao()

    @Provides
    @Singleton
    fun provideEntryRepository(dao: EntryDao): EntryRepository =
        EntryRepositoryImpl(dao)
}
