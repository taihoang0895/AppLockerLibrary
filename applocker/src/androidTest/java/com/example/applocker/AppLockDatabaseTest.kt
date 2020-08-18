package com.example.applocker

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.applocker.db.AppLockDBFunctions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AppLockDatabaseTest {

    @Test
    fun testInsertAppLock() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AppLockLoader.init(appContext)
        val mainScope = MainScope()
        mainScope.launch {
            AppLockDBFunctions.getListLockedApps().observeForever { listAppLocks ->

                Log.d("taih", "listAppLocks changed ${listAppLocks.size}")
                listAppLocks.forEach {
                    Log.d("taih", "id ${it.appInfo.description}")
                }
            }
        }


        var appInfoList = ArrayList<AppInfo>()
        appInfoList.add(AppInfo("package1", "name1", ""))
        appInfoList.add(AppInfo("package2", "name2", ""))
        appInfoList.add(AppInfo("package3", "name2", ""))
        appInfoList.add(AppInfo("package4", "name2", ""))
        AppLockDBFunctions.lock(appInfoList)
        runBlocking {
            delay(20000)
        }
    }
    @Test
    fun testCheckAppLock(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AppLockLoader.init(appContext)
    }
}