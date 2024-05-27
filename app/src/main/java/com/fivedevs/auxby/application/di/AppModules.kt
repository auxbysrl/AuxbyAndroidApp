package com.fivedevs.auxby.application.di

import android.content.Context
import com.fivedevs.auxby.application.App
import com.fivedevs.auxby.domain.utils.rx.AppRxSchedulers
import com.fivedevs.auxby.domain.utils.rx.RxBus
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    @Provides
    @Singleton
    fun provideRxSchedulers(): RxSchedulers {
        return AppRxSchedulers() as RxSchedulers
    }

    @Provides
    @Singleton
    fun provideRxBus(): RxBus {
        return RxBus()
    }

    @Provides
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): App{
        return app as App
    }
}