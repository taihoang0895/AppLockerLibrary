package com.example.applocker

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

private object AppDescription {
    private val DESC_APP_MAP = HashMap<String, Int>()

    init {
        DESC_APP_MAP.put("com.android.contacts", R.string.app_locker_description_gmail);
        DESC_APP_MAP.put("com.android.htccontacts", R.string.app_locker_description_gmail);
        DESC_APP_MAP.put("com.sonymobile.android.contacts", R.string.app_locker_description_gmail);
        DESC_APP_MAP.put("com.android.dialer", R.string.app_locker_description_contact);
        DESC_APP_MAP.put("com.sonymobile.android.dialer", R.string.app_locker_description_contact);
        DESC_APP_MAP.put(
            "com.sonyericsson.android.socialphonebook",
            R.string.app_locker_description_contact
        );
        DESC_APP_MAP.put("com.htc.htcdialer", R.string.app_locker_description_contact);
        DESC_APP_MAP.put("com.android.browser", R.string.app_locker_description_contact);
        DESC_APP_MAP.put("com.android.settings", R.string.app_locker_description_setting);
        DESC_APP_MAP.put("com.android.vending", R.string.app_locker_description_play_store);
        DESC_APP_MAP.put("com.android.mms", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.sonyericsson.conversations", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.android.chrome", R.string.app_locker_description_browser);
        DESC_APP_MAP.put("com.instagram.android", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.facebook.katana", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.facebook.lite", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.facebook.orca", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.skype.raider", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.tencent.mm", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.kakao.talk", R.string.app_locker_description_social);
        DESC_APP_MAP.put("org.telegram.messenger", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.sec.android.gallery3d", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.alensw.PicFolder", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.google.android.apps.photos", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.android.gallery3d", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.google.android.apps.docs", R.string.app_locker_description_drive);
        DESC_APP_MAP.put("com.google.android.gm", R.string.app_locker_description_gmail);
        DESC_APP_MAP.put("com.viber.voip", R.string.app_locker_description_social);
        DESC_APP_MAP.put("jp.naver.line.android", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.zing.zalo", R.string.app_locker_description_social);
        DESC_APP_MAP.put("com.google.android.apps.inbox", R.string.app_locker_description_message);
        DESC_APP_MAP.put("com.android.email", R.string.app_locker_description_gmail);
        DESC_APP_MAP.put("com.miui.gallery", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.sonyericsson.album", R.string.app_locker_description_gallery);
        DESC_APP_MAP.put("com.google.android.contacts", R.string.app_locker_description_contact);
        DESC_APP_MAP.put(
            "com.google.android.apps.messaging",
            R.string.app_locker_description_message
        );
        DESC_APP_MAP.put("com.google.android.dialer", R.string.app_locker_description_message);
        DESC_APP_MAP.put(
            "com.mi.android.globalFileexplorer",
            R.string.app_locker_description_files
        );
        DESC_APP_MAP.put("com.android.documentsui", R.string.app_locker_description_files);

    }

    fun getDescription(context: Context, pkgName: String): String {
        if (DESC_APP_MAP.containsKey(pkgName)) {
            return context.getString(DESC_APP_MAP.get(pkgName)!!)
        }
        return ""
    }
}

private class TopAppObserver(val appContext: Context) {
    private val ownerPackageName = appContext.packageName
    private var lastPackage = ownerPackageName
    private val topAppInfoLiveData = MutableLiveData<String>()
    private var queryTopPacket: suspend (() -> String)
    private val mainScope = MainScope()
    private val topPackageNamePreLollipop: suspend () -> String = {
        withContext(Dispatchers.Default) {
            var result = ""
            try {

                val activityManager: ActivityManager =
                    appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val runningTaskInfos = activityManager.getRunningTasks(1)

                if (runningTaskInfos != null && runningTaskInfos.size > 0) {
                    val pkgName = runningTaskInfos.get(0)?.topActivity?.packageName ?: ""
                    result = pkgName

                }


            } catch (e: Exception) {

            }
            result
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val getTopPackageNameLollipop: suspend () -> String = {
        withContext(Dispatchers.Default) {
            var result = ""
            try {
                val activityManager: ActivityManager =
                    appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val appProcess = activityManager.runningAppProcesses
                if (appProcess != null && appProcess.size >= 0) {
                    val appProcess = appProcess.get(0)
                    if (appProcess != null && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        result = appProcess.processName
                    }

                }
            } catch (e: Exception) {

            }
            result
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private val getTopPackageNameLollipopMr1: suspend () -> String = {
        withContext(Dispatchers.Default) {
            var result = ""
            try {
                val usageStatsManager: UsageStatsManager =
                    appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - 60000
                val queryUsageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                if (queryUsageStats != null && queryUsageStats.size > 0) {
                    var recentTime = 0L
                    var recentPkg = ""
                    for (stat in queryUsageStats) {
                        if (stat.lastTimeUsed > recentTime) {
                            recentTime = stat.lastTimeUsed
                            recentPkg = stat.packageName
                        }
                    }
                    result = recentPkg
                }

            } catch (e: Exception) {

            }
            result
        }
    }

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            queryTopPacket = getTopPackageNameLollipopMr1
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            queryTopPacket = getTopPackageNameLollipop
        } else {
            queryTopPacket = topPackageNamePreLollipop
        }
        start()
    }

    fun getTopApp(): LiveData<String> {
        return topAppInfoLiveData
    }

    fun notifyTopAppChanged(packageName: String) {
        topAppInfoLiveData.postValue(packageName)
    }

    private fun start() {
        mainScope.launch {
            while (true) {
                delay(200)
                val currentPackage = queryTopPacket()
                if (!lastPackage.equals(currentPackage)) {
                    lastPackage = currentPackage
                    notifyTopAppChanged(currentPackage)
                }

            }
        }

    }


}

internal class AppManagerImpl private constructor(val appContext: Context) : AppManager {
    companion object {
        private var INSTANCE: AppManagerImpl? = null
        fun create(appContext: Context) {
            if (INSTANCE == null) {
                INSTANCE = AppManagerImpl(appContext)
            }
        }

        fun getInstance(): AppManagerImpl {
            return INSTANCE!!
        }
    }

    private val scanningAppInfo = MutableLiveData<ScanningAppInfo>()
    private val listAppInfo = MutableLiveData<List<AppInfo>>()
    private val topAppObserver = TopAppObserver(appContext)
    private val _listAppInfo = ArrayList<AppInfo>()
    private val _scanningAppInfo = ScanningAppInfo(ScanningState.DONE)
    private val appManagerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mainScope = MainScope()
    private val installOrUninstallAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            var pkgName: String?
            var actionName: String?
            pkgName = intent?.data?.encodedSchemeSpecificPart
            actionName = intent?.action
            if (pkgName != null) {
                if (Intent.ACTION_PACKAGE_ADDED.equals(actionName)) {
                    addAppInfo(pkgName)
                    notifyListAppInfoChanged()
                }
                if (Intent.ACTION_PACKAGE_REMOVED.equals(actionName)) {
                    removeAppInfo(pkgName)
                    notifyListAppInfoChanged()
                }
            }
        }
    }

    init {
        registerInstallOrUninstallAppEvent()
        _scanningAppInfo.scanningState = ScanningState.SCANNING
        notifyScanningAppInfoChanged()
        appManagerScope.launch {
            queryAllInstalledApps()
            _scanningAppInfo.scanningState = ScanningState.DONE
            notifyScanningAppInfoChanged()
        }

    }

    override fun getListAppInfo(): LiveData<List<AppInfo>> {
        return listAppInfo
    }

    override fun getTopApp(): LiveData<String> {
        return topAppObserver.getTopApp()
    }

    override fun scanApp(): LiveData<ScanningAppInfo> {
        return scanningAppInfo
    }


    private fun queryAllInstalledApps() {

        val packageManager = appContext.packageManager
        val intent = Intent("android.intent.action.MAIN", null)
        intent.addCategory("android.intent.category.LAUNCHER")
        val listResolveInfos = packageManager.queryIntentActivities(intent, 0)
        synchronized(_listAppInfo) {
            _listAppInfo.clear()
            val packetLockedSet = HashSet<String>()
            for (resolveInfo in listResolveInfos) {
                if (!packetLockedSet.contains(resolveInfo.activityInfo.packageName)) {
                    val appInfo = AppInfo(
                        resolveInfo.activityInfo.packageName,
                        getAppName(resolveInfo),
                        AppDescription.getDescription(
                            appContext,
                            resolveInfo.activityInfo.packageName
                        )
                    )
                    _listAppInfo.add(appInfo)
                    packetLockedSet.add(appInfo.pkgName)
                }
            }
        }
        notifyListAppInfoChanged()
    }

    override fun getAppInfo(pkgName: String): AppInfo? {
        val listIterator = _listAppInfo.iterator()
        while (listIterator.hasNext()) {
            val appInfo = listIterator.next()
            if (appInfo.pkgName.equals(pkgName)) {
                return appInfo
            }
        }
        return null
    }

    private fun notifyListAppInfoChanged() {
        mainScope.launch {
            listAppInfo.postValue(_listAppInfo)
        }
    }

    private fun notifyScanningAppInfoChanged() {
        mainScope.launch {
            scanningAppInfo.postValue(_scanningAppInfo)
        }
    }

    private fun addAppInfo(pkgName: String) {
        val appInfo = AppInfo(
            pkgName,
            getAppName(pkgName),
            AppDescription.getDescription(
                appContext,
                AppDescription.getDescription(
                    appContext,
                    pkgName
                )
            )
        )
        synchronized(_listAppInfo) {
            _listAppInfo.add(appInfo)
        }
    }

    private fun removeAppInfo(pkgName: String) {
        val listAppMatched = ArrayList<AppInfo>()
        val listIterator = _listAppInfo.iterator()
        while (listIterator.hasNext()) {
            val appInfo = listIterator.next()
            if (appInfo.pkgName.equals(pkgName)) {
                listAppMatched.add(appInfo)
            }
        }
        if (listAppMatched.size > 0) {
            synchronized(_listAppInfo) {
                for (appInfo in listAppMatched) {
                    _listAppInfo.remove(appInfo)
                }
            }
        }
    }

    private fun getAppName(pkgName: String): String {
        val packageManager = appContext.packageManager
        var appInfo: ApplicationInfo? = null
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {

        }
        try {
            if (appInfo != null) {
                return packageManager.getApplicationLabel(appInfo).toString()
            }
        } catch (e: SecurityException) {

        }
        return "Unknown";
    }

    private fun getAppName(resolveInfo: ResolveInfo): String {
        val packageManager = appContext.packageManager
        try {
            return resolveInfo.loadLabel(packageManager).toString()
        } catch (e: SecurityException) {
            return "Unknown";
        }
    }

    private fun registerInstallOrUninstallAppEvent() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        appContext.registerReceiver(installOrUninstallAppReceiver, intentFilter)
    }

}