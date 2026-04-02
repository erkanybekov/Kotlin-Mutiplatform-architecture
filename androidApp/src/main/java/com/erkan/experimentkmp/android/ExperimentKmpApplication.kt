package com.erkan.experimentkmp.android

import android.app.Application
import com.erkan.experimentkmp.di.initKoin
import org.koin.android.ext.koin.androidContext

class ExperimentKmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            storageDirectoryPath = filesDir.absolutePath,
        ) {
            androidContext(this@ExperimentKmpApplication)
        }
    }
}
