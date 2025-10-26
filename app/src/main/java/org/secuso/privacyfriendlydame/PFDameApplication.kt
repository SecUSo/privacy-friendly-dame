package org.secuso.privacyfriendlydame


import android.app.Activity
import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.work.Configuration
import org.secuso.pfacore.application.PFAppBackup
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlybackup.api.backup.FileUtil
import org.secuso.privacyfriendlydame.ui.MainActivity


class PFDameApplication : PFApplication() {
    override val name: String
        get() = getString(R.string.app_name)
    override val data: PFData
        get() = PFApplicationData.instance(this).data
    override val mainActivity: Class<out Activity>
        get() = MainActivity::class.java

    private val gameFileBackupManager: PFAppBackup by lazy {
        object : PFAppBackup {
            override val key: String = "file"
            override fun backup(writer: JsonWriter): JsonWriter {
                writer.name("file")

                FileUtil.writeFile(writer, applicationContext.getFileStreamPath(MainActivity.SAVE_FILE))
                return writer
            }

            override fun restore(key: String, reader: JsonReader, context: Context): JsonReader {
                val saveFile = context.getFileStreamPath(MainActivity.SAVE_FILE)
                if (saveFile.exists()) {
                    saveFile.delete()
                }

                Log.d("PFA App Restorer", "Restoring savefile...")
                FileUtil.readFile(reader, saveFile.parentFile!!)
                Log.d("PFA App Restorer", "Savefile Restored")
                return reader
            }
        }
    }

    override val appBackup: List<PFAppBackup>
        get() = listOf(gameFileBackupManager)


    override val workManagerConfiguration = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
}