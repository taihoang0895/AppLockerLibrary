package com.example.applocker

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AppLockerTest {

    @Test
    fun testInsertAppLock() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AppLockLoader.init(appContext)
        val mainScope = MainScope()
        var listAppLockInfosRef: List<AppLockInfo>? = null
        mainScope.launch {
            AppManagerFactory.appLocker.getListApps().observeForever { listAppLockInfos ->
                listAppLockInfosRef = listAppLockInfos
                Log.d("taih", "total app size ${listAppLockInfos.size}")
                var totalLockedApp = 0
                listAppLockInfos.forEach {
                    if (it.locked) {
                        totalLockedApp++
                    }
                }
                Log.d("taih", "total locked app size ${totalLockedApp}")
            }
        }


        AppManagerFactory.appLocker.scanApps()
        runBlocking {
            delay(2000)
        }
        if (listAppLockInfosRef != null) {
            AppManagerFactory.appLocker.lock(listAppLockInfosRef!!.get(0).appInfo)
        }
        runBlocking {
            delay(2000)
        }
    }

    @Test
    fun testCheckAppLock() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AppLockLoader.init(appContext)
    }
}