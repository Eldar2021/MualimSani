package com.muallimsaniqr.flutter_app.di

import com.muallimsaniqr.flutter_app.data.repository.MualimRepoImpl
import com.muallimsaniqr.flutter_app.domain.repository.MualimRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryDI {

    @Binds
    @Singleton
    abstract fun bindMualimRepository(mualimRepoImpl: MualimRepoImpl): MualimRepository


}