package org.secuso.privacyfriendlydame


import android.app.Activity
import android.util.Log
import androidx.work.Configuration
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlydame.ui.MainActivity

class PFDameApplication : PFApplication() {
    override val name: String
        get() = getString(R.string.app_name)
    override val data: PFData
        get() = PFApplicationData.instance(this).data
    override val mainActivity: Class<out Activity>
        get() = MainActivity::class.java

    override val workManagerConfiguration = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
}