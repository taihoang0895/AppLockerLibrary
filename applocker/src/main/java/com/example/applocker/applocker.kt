package com.example.applocker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.applocker.db.AppLockDBFunctions
import com.example.applocker.util.PreferencesHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.ArrayList

internal class AppLockerImpl private constructor(val appContext: Context) : AppLocker {
    companion object {
        private var INSTANCE: AppLockerImpl? = null
        fun create(appContext: Context) {
            if (INSTANCE == null) {
                INSTANCE = AppLockerImpl(appContext)
            }
        }

        fun getInstance(): AppLockerImpl {
            return INSTANCE!!
        }
    }

    private val ENABLE_APP_LOCK_KEY = "ENABLE_APP_LOCK_KEY"
    private val _listLockApps = ArrayList<AppLockInfo>()
    private val listLockApps = MutableLiveData<List<AppLockInfo>>()
    private val _scanningAppLockInfo = ScanningAppLockInfo(ScanningState.DONE);
    private val scanningAppLockInfo = MutableLiveData<ScanningAppLockInfo>()
    private val appManager = AppManagerFactory.appManager
    private val mainScope = MainScope()
    private val scanAppObserver = Observer<List<AppInfo>> { listAppInfos ->
        _scanningAppLockInfo.scanningState = ScanningState.DONE
        notifyScanningAppInfoChanged()
        setupListApp(listAppInfos)
    }

    private val lockedAppObserver = Observer<List<AppLockInfo>> { listLockedAppInfos ->
        synchronized(_listLockApps) {
            _listLockApps.forEach {
                val packetLockedSet = HashSet<String>()
                listLockedAppInfos.forEach {
                    packetLockedSet.add(it.appInfo.pkgName)
                }

                if (packetLockedSet.contains(it.appInfo.pkgName)) {
                    it.locked = true
                } else {
                    it.locked = false
                }
            }
        }
        notifyListAppInfoChanged()
    }
    init {
        registerTopAppChanged()
    }

    override fun getListApps(): LiveData<List<AppLockInfo>> {
        return listLockApps
    }

    override fun lock(appInfo: AppInfo) {
        AppLockDBFunctions.lock(appInfo)
    }

    override fun unlock(appInfo: AppInfo) {
        AppLockDBFunctions.unlock(appInfo)
    }

    override fun scanApps(): LiveData<ScanningAppLockInfo> {
        val scanningAppInfo = appManager.scanApp().value!!

        _scanningAppLockInfo.scanningState = scanningAppInfo.scanningState
        notifyScanningAppInfoChanged()
        if (_scanningAppLockInfo.scanningState == ScanningState.DONE) {
            setupListApp(appManager.getListAppInfo().value!!)
        } else {
            mainScope.launch {
                appManager.getListAppInfo().removeObserver(scanAppObserver)
                appManager.getListAppInfo().observeForever(scanAppObserver)
            }

        }

        return scanningAppLockInfo
    }

    private fun setupListApp(listAppInfos: List<AppInfo>) {
        synchronized(_listLockApps) {
            _listLockApps.clear()
            listAppInfos.forEach {
                _listLockApps.add(AppLockInfo(it, false))
            }
        }
        mainScope.launch {
            AppLockDBFunctions.getListLockedApps().removeObserver(lockedAppObserver)
            AppLockDBFunctions.getListLockedApps().observeForever(lockedAppObserver)
        }
    }

    private fun notifyListAppInfoChanged() {
        mainScope.launch {
            listLockApps.postValue(_listLockApps)
        }
    }

    private fun notifyScanningAppInfoChanged() {
        mainScope.launch {
            scanningAppLockInfo.postValue(_scanningAppLockInfo)
        }
    }

    private fun registerTopAppChanged() {
        appManager.getTopApp().observeForever {
            Log.d("taih", "topApp Changed " + it)
            if(isLockEnable()){
                mainScope.launch {
                    if(AppLockDBFunctions.isLocked(it)){
                        Log.d("taih", "locked app " + it)
                    }else{
                        Log.d("taih", "not locked app " + it)
                    }
                }

            }
        }
    }

    override fun setLockEnable(enable: Boolean) {
        PreferencesHelper.putBoolean(ENABLE_APP_LOCK_KEY, enable)
    }

    private fun isLockEnable(): Boolean {
        return PreferencesHelper.getBoolean(ENABLE_APP_LOCK_KEY, false)
    }

    override fun changeSetting(setting: LockSetting) {
        TODO("Not yet implemented")
    }

    override fun createTransactionRequestingPermission() {
        TODO("Not yet implemented")
    }
}

internal class MyScreenLockerImpl : MyScreenLocker {
    override fun lock(myScreenInfo: MyScreenInfo) {
        TODO("Not yet implemented")
    }

    override fun unlock(myScreenInfo: MyScreenInfo) {
        TODO("Not yet implemented")
    }
}