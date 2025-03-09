package com.rouf.saht.common.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.paperdb.Paper
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

object PaperDBHelper {

    private const val BACKUP_FOLDER = "sehat_backups"
    private const val BACKUP_FILE_NAME = "sehat_backup.json"
    private const val TAG = "PaperDBHelper"

    /**
     * Exports all PaperDB data to a JSON file.
     */
    fun exportData(context: Context): Boolean {
        return try {
            if (!checkStoragePermissions(context)) {
                Log.e(TAG, "Storage permissions not granted")
                return false
            }

            val paperDir = File(context.filesDir, "io.paperdb")
            if (!paperDir.exists()) {
                Log.e(TAG, "PaperDB directory not found")
                return false
            }

            // Use root external storage (for older Android versions)
            val externalDir = Environment.getExternalStorageDirectory()
            Log.d(TAG, "External storage directory: ${externalDir.absolutePath}")

            // First create the directory explicitly
            var backupDir = File(externalDir, BACKUP_FOLDER)
            Log.d(TAG, "Attempting to create directory at: ${backupDir.absolutePath}")

            try {
                // Check if directory already exists
                if (!backupDir.exists()) {
                    // Try to create parent directory if needed
                    val parentDir = backupDir.parentFile
                    if (parentDir != null && !parentDir.exists()) {
                        val parentDirCreated = parentDir.mkdirs()
                        Log.d(TAG, "Parent directory created: $parentDirCreated")
                    }

                    // Create the backup directory
                    val dirCreated = backupDir.mkdir() // Use mkdir() instead of mkdirs()
                    if (!dirCreated) {
                        Log.e(TAG, "Failed to create directory despite permissions: ${backupDir.absolutePath}")
                        // Fall back to app-specific directory if we can't create in root
                        val fallbackDir = File(context.getExternalFilesDir(null), BACKUP_FOLDER)
                        if (!fallbackDir.exists()) {
                            fallbackDir.mkdirs()
                        }
                        Log.d(TAG, "Falling back to: ${fallbackDir.absolutePath}")
                        backupDir = fallbackDir
                    } else {
                        Log.d(TAG, "Successfully created directory")
                    }
                } else {
                    Log.d(TAG, "Directory already exists")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating directory", e)
                // Fall back to app-specific directory
                val fallbackDir = File(context.getExternalFilesDir(null), BACKUP_FOLDER)
                if (!fallbackDir.exists()) {
                    fallbackDir.mkdirs()
                }
                Log.d(TAG, "Falling back to: ${fallbackDir.absolutePath}")
                backupDir = fallbackDir
            }

            val backupFile = File(backupDir, BACKUP_FILE_NAME)
            Log.d(TAG, "Backup file path: ${backupFile.absolutePath}")

            val allKeys = paperDir.list() ?: emptyArray()
            if (allKeys.isEmpty()) {
                Log.e(TAG, "No keys found in PaperDB")
                return false
            }

            val backupMap = mutableMapOf<String, Any?>()
            allKeys.forEach { key ->
                val value: Any? = Paper.book().read<Any>(key, null)
                backupMap[key] = value
            }

            val json = Gson().toJson(backupMap)

            try {
                // Create the file if it doesn't exist
                if (!backupFile.exists()) {
                    val fileCreated = backupFile.createNewFile()
                    Log.d(TAG, "Backup file created: $fileCreated")
                }

                // Write data to the file
                FileWriter(backupFile).use { it.write(json) }
                Log.d(TAG, "Successfully wrote data to ${backupFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to file", e)
                return false
            }

            Log.d(TAG, "Successfully exported to ${backupFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Imports data from a JSON backup file into PaperDB.
     */
    fun importData(context: Context): Boolean {
        return try {
            if (!checkStoragePermissions(context)) {
                Log.e(TAG, "Storage permissions not granted")
                return false
            }

            // First try external storage root path
            var backupFile = File(Environment.getExternalStorageDirectory(), "$BACKUP_FOLDER/$BACKUP_FILE_NAME")

            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file not found at root location: ${backupFile.absolutePath}")
                // Try fallback location
                backupFile = File(context.getExternalFilesDir(null), "$BACKUP_FOLDER/$BACKUP_FILE_NAME")
                if (!backupFile.exists()) {
                    Log.e(TAG, "Backup file not found at fallback location: ${backupFile.absolutePath}")
                    return false
                }
            }

            Log.d(TAG, "Importing from ${backupFile.absolutePath}")

            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            val backupMap: Map<String, Any> = Gson().fromJson(FileReader(backupFile), type)

            backupMap.forEach { (key, value) ->
                Paper.book().write(key, value)
            }

            Log.d(TAG, "Import completed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            e.printStackTrace()
            false
        }
    }

    private fun checkStoragePermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasWritePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            val hasReadPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            // For Android 10+ also check if legacy storage is enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "Running on Android 10+, legacy storage access required")
            }

            if (!hasWritePermission || !hasReadPermission) {
                Log.e(TAG, "Storage permissions are not granted. Write: $hasWritePermission, Read: $hasReadPermission")
                return false
            }

            return true
        }

        return true  // Prior to M, permissions are granted at install time
    }
}