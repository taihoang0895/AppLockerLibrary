package com.example.applocker.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.applocker.AppInfo
import com.example.applocker.AppLockInfo
import com.example.applocker.db.entities.AppLockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object AppLockDBFunctions {
    fun getListLockedApps(): LiveData<List<AppLockInfo>> {
        return Transformations.map(
            DatabaseHelper.get().appLockDao().getAppLockList(),
            { listAppLockEntities ->
                val newListAppLockInfo = ArrayList<AppLockInfo>()
                listAppLockEntities.forEach {
                    val appLockInfo = AppLockInfo(AppInfo(it.packageName, "", "${it.id}"), true)
                    newListAppLockInfo.add(appLockInfo)
                }
                newListAppLockInfo
            })
    }

    fun lock(appInfo: AppInfo) {
        if (!DatabaseHelper.get().appLockDao().exist(appInfo.pkgName)) {
            val appLockEntity = AppLockEntity(packageName = appInfo.pkgName)
            DatabaseHelper.get().appLockDao().insertAppLock(appLockEntity)
        }
    }

    fun lock(appInfos: List<AppInfo>) {
        var appLockEntities = ArrayList<AppLockEntity>()
        appInfos.forEach {
            if (!DatabaseHelper.get().appLockDao().exist(it.pkgName)) {
                appLockEntities.add(AppLockEntity(packageName = it.pkgName))
            }
        }
        if (appLockEntities.size > 0) {
            DatabaseHelper.get().appLockDao().insertAppLockList(appLockEntities)
        }
    }

    suspend fun isLocked(pkgName: String): Boolean {
        var existed = false
        withContext(Dispatchers.Default) {
            existed = DatabaseHelper.get().appLockDao().exist(pkgName)
        }
        return existed
    }

    fun unlock(appInfo: AppInfo) {
        DatabaseHelper.get().appLockDao().deleteAppLock(appInfo.pkgName)
    }

    fun unlock(appInfos: List<AppInfo>) {
        var packageList = ArrayList<String>()
        appInfos.forEach {
            packageList.add(it.pkgName)
        }
        DatabaseHelper.get().appLockDao().deleteAppLocks(packageList)
    }
}