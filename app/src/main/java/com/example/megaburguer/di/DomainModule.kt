package com.example.megaburguer.di

import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSource
import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindAuthDataSource(
        authFirebaseDataSourceImpl: AuthFirebaseDataSourceImp
    ): AuthFirebaseDataSource

}