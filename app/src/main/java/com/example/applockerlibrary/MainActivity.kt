package com.example.applockerlibrary

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.applocker.AppManagerFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goToUsageStatsSetting(this)
        AppManagerFactory.appLocker.setLockEnable(true);

    }
    fun goToUsageStatsSetting(context: Context) {
        if (!isUsageStatsPermissionGranted(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
    }
    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        var appOps: AppOpsManager? = null
        var mode = -1
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            appOps =
                context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            if (appOps != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mode = appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.packageName
                    )
                }
            }
            mode == AppOpsManager.MODE_ALLOWED
        } else true
    }
}