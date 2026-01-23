package com.example.megaburguer.di

import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSource
import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import com.example.megaburguer.data.repository.extract.ExtractDataSource
import com.example.megaburguer.data.repository.extract.ExtractDataSourceImp
import com.example.megaburguer.data.repository.menu.MenuDataSource
import com.example.megaburguer.data.repository.menu.MenuDataSourceImp
import com.example.megaburguer.data.repository.orderItems.OrderItemDataSource
import com.example.megaburguer.data.repository.orderItems.OrderItemDataSourceImp
import com.example.megaburguer.data.repository.orderPrint.OrderPrintDataSource
import com.example.megaburguer.data.repository.orderPrint.OrderPrintDataSourceImp
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

    @Binds
    abstract fun bindMenuDataSource(
        menuDataSourceImpl: MenuDataSourceImp
    ): MenuDataSource

    @Binds
    abstract fun bindOrderItemDataSource(
        orderItemDataSourceImp: OrderItemDataSource
    ): OrderItemDataSource

    @Binds
    abstract fun bindExtractDataSource(
        extractDataSourceImp: ExtractDataSourceImp
    ): ExtractDataSource

    @Binds
    abstract fun bindOrderPrintDataSource(
        orderPrintDataSourceImp: OrderPrintDataSourceImp
    ): OrderPrintDataSource
}