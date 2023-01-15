package org.secuso.privacyfriendlydame.backup

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.JsonReader
import android.util.Log
import org.secuso.privacyfriendlybackup.api.backup.FileUtil
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlydame.ui.MainActivity
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.system.exitProcess


class BackupRestorer : IBackupRestorer {
    @Throws(IOException::class)
    private fun readSaveFile(context: Context, reader: JsonReader) {

        // delete if file already exists
        val saveFile = context.getFileStreamPath(MainActivity.SAVE_FILE)
        if (saveFile.exists()) {
            saveFile.delete()
        }

        Log.d(TAG, "Restoring savefile...")
        FileUtil.readFile(reader, saveFile.parentFile!!);
        Log.d(TAG, "Savefile Restored")
    }

    @Throws(IOException::class)
    private fun readPreferences(reader: JsonReader, preferences: SharedPreferences.Editor) {
        reader.beginObject()
        while (reader.hasNext()) {
            val name: String = reader.nextName()
            when (name) {
                "pref_rule_flying_king",
                "pref_rule_white_begins" -> preferences.putBoolean(name, reader.nextBoolean())
                "lastChosenPage",
                "lastChosenDifficulty" -> preferences.putInt(name, reader.nextInt())
                else -> throw RuntimeException("Unknown preference $name")
            }
        }
        reader.endObject()
    }

    override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        return try {
            val isReader = InputStreamReader(restoreData)
            val reader = JsonReader(isReader)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()

            // START
            reader.beginObject()
            while (reader.hasNext()) {
                val type: String = reader.nextName()
                when (type) {
                    "file" -> readSaveFile(context, reader)
                    "preferences" -> readPreferences(reader, preferences)
                    else -> throw RuntimeException("Can not parse type $type")
                }
            }
            reader.endObject()
            preferences.commit()

            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val TAG = "PFABackupRestorer"
    }
}