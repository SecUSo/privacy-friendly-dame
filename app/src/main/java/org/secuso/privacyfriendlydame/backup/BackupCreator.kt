package org.secuso.privacyfriendlydame.backup
import android.content.Context
import android.preference.PreferenceManager
import android.util.JsonWriter
import android.util.Log
import org.secuso.privacyfriendlybackup.api.backup.FileUtil
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil.writePreferences
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlydame.ui.MainActivity
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter

class BackupCreator : IBackupCreator {
    override fun writeBackup(context: Context, outputStream: OutputStream): Boolean {
        Log.d(TAG, "createBackup() started")
        val outputStreamWriter = OutputStreamWriter(outputStream, Charsets.UTF_8)
        val writer = JsonWriter(outputStreamWriter)
        writer.setIndent("")

        try {
            writer.beginObject()

            Log.d(TAG, "Writing file")
            writer.name("file")

            FileUtil.writeFile(writer, context.getFileStreamPath(MainActivity.SAVE_FILE));

            Log.d(TAG, "Writing preferences")
            writer.name("preferences")

            val pref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            writePreferences(writer, pref)

            writer.endObject()
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred", e)
            e.printStackTrace()
            return false
        }

        Log.d(TAG, "Backup created successfully")
        return true
    }

    companion object {
        const val TAG = "PFABackupCreator"
    }
}