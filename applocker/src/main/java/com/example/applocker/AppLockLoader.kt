package com.example.applocker

import android.content.Context
import com.example.applocker.db.DatabaseHelper
import com.example.applocker.util.PreferencesHelper

object AppLockLoader {
    fun init(appContext: Context) {
        AppManagerFactory.init(appContext)
        PreferencesHelper.start(appContext)
        DatabaseHelper.create(appContext)

    }

}