package com.techfathers.gifstorm

import android.app.Application
import android.content.Intent
import android.os.Build
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.techfathers.gifstorm.data.db.AppDatabase
import com.techfathers.gifstorm.data.network.Apis
import com.techfathers.gifstorm.data.network.NetworkConnectionInterceptor
import com.techfathers.gifstorm.data.preferences.PreferenceProvider
import com.techfathers.gifstorm.data.respositories.CommonRepository
import com.techfathers.gifstorm.ui.activities.UpdateAppActivity
import com.techfathers.gifstorm.util.ApiConstants
import com.techfathers.gifstorm.util.getDatabaseReference
import com.techfathers.gifstorm.vm_factories.GifDetailsViewModelFactory
import com.techfathers.gifstorm.vm_factories.HomeViewModelFactory
import com.techfathers.gifstorm.vm_factories.SearchGifViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import timber.log.Timber
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class MainApplication : Application(), KodeinAware {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override val kodein = Kodein.lazy {

        import(androidXModule(this@MainApplication))
        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { Apis(instance()) }
        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind() from singleton { CommonRepository(instance(), instance()) }
        bind() from singleton { HomeViewModelFactory(instance()) }
        bind() from singleton { GifDetailsViewModelFactory(instance()) }
        bind() from singleton { SearchGifViewModelFactory(instance()) }
    }
}