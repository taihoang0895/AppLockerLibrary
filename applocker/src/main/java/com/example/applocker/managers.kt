package com.example.applocker

import android.content.Context
import androidx.lifecycle.LiveData

enum class ScanningState {
    SCANNING,
    DONE
}

data class AppInfo(val pkgName: String, val name: String, val description: String) {
    override fun equals(other: Any?): Boolean {
        if (other is AppInfo) {
            return pkgName.equals(other.pkgName)
        }
        return super.equals(other)
    }
}

enum class LockTime {
    LOCK_AFTER_SCREEN_OFF,
    LOCK_AFTER_APP_CLOSED
}

data class LockSetting(var lockTime: LockTime)

data class MyScreenInfo(val pkgName: String)
data class AppLockInfo(val appInfo: AppInfo, var locked: Boolean)

data class ScanningAppLockInfo(var scanningState: ScanningState)
data class ScanningAppInfo(var scanningState: ScanningState)

interface AppLocker {
    fun scanApps(): LiveData<ScanningAppLockInfo>
    fun getListApps(): LiveData<List<AppLockInfo>>
    fun lock(appInfo: AppInfo)
    fun unlock(appInfo: AppInfo)
    fun setLockEnable(enable: Boolean)
    fun changeSetting(setting: LockSetting)
    fun createTransactionRequestingPermission()
}

interface MyScreenLocker {
    fun lock(myScreenInfo: MyScreenInfo)
    fun unlock(myScreenInfo: MyScreenInfo)
}

interface AppManager {
    fun scanApp(): LiveData<ScanningAppInfo>
    fun getListAppInfo(): LiveData<List<AppInfo>>
    fun getTopApp(): LiveData<String>
    fun getAppInfo(pkgName: String): AppInfo?
}

object AppManagerFactory {
    lateinit var appLocker: AppLocker
    lateinit var appManager: AppManager
    fun init(applicationContext: Context) {
        AppManagerImpl.create(applicationContext)
        appManager = AppManagerImpl.getInstance()
        AppLockerImpl.create(applicationContext)
        appLocker = AppLockerImpl.getInstance()
    }
}