package com.example.megaburguer.di

import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSource
import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import com.example.megaburguer.data.repository.tables.TablesDataSource
import com.example.megaburguer.data.repository.tables.TablesDataSourceImp
import com.example.megaburguer.data.repository.users.UserDataSource
import com.example.megaburguer.data.repository.users.UserDataSourceImp
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

    @Binds
    abstract fun bindUserDataSource(
        userDataSourceImpl: UserDataSourceImp
    ): UserDataSource

    @Binds
    abstract fun bindTablesDataSource(
        tablesDataSourceImpl: TablesDataSourceImp
    ): TablesDataSource



}