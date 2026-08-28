package com.muallimsaniqr.flutter_app.di

import android.content.Context
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageDI {

    @Provides
    @Singleton
    fun provideAppPreference(@ApplicationContext context: Context): AppPreference {
        return AppPreference(context)
    }

}