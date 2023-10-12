package com.codemantri.shieldme.di

import android.content.Context
import androidx.room.Room
import com.codemantri.shieldme.data.AppDatabase
import com.codemantri.shieldme.data.ContactDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "shield_me.db"
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideContactDAO(appDatabase: AppDatabase): ContactDAO =
        appDatabase.getContactDao()

}