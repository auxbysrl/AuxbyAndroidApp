package com.fivedevs.auxby.application.di

import android.content.Context
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.RoomDB
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.managers.OffersUpdateManager
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModules {

    @Provides
    @Singleton
    fun provideRoomDB(@ApplicationContext context: Context): AppDatabase {
        return RoomDB(context).appDatabase
    }

    @Provides
    @Singleton
    fun providePreferencesService(@ApplicationContext context: Context): PreferencesService {
        return PreferencesService(context)
    }

    @Provides
    @Singleton
    fun provideRepository(appDatabase: AppDatabase): UserRepository {
        return UserRepository(appDatabase)
    }

    @Provides
    @Singleton
    fun provideOfferUpdateManage(
        dataApi: DataApi,
        offersRepository: OffersRepository,
        rxSchedulers: RxSchedulers
    ): OffersUpdateManager {
        return OffersUpdateManager(dataApi, rxSchedulers, offersRepository)
    }
}