package com.example.applockerlibrary

import android.app.Application
import com.example.applocker.AppLockLoader

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLockLoader.init(this)
    }
}