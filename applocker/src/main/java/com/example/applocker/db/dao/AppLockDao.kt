package com.example.applocker.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.applocker.db.entities.AppLockEntity

@Dao
interface AppLockDao {
    @Query("SELECT * FROM app_lock_table")
    fun getAppLockList(): LiveData<List<AppLockEntity>>

    @Insert
    fun insertAppLock(appLockEntity: AppLockEntity): Long

    @Insert
    fun insertAppLockList(appLockEntities: List<AppLockEntity>)

    @Query("DELETE FROM app_lock_table WHERE packageName=:packageName")
    fun deleteAppLock(packageName: String)

    @Query("delete from app_lock_table where packageName in (:packageNameList)")
    fun deleteAppLocks(packageNameList: List<String>)

    @Query("SELECT COUNT(*) > 0 FROM app_lock_table WHERE packageName=:packageName")
    fun exist(packageName: String): Boolean

}