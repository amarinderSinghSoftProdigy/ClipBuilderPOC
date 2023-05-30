package com.demo.musictrimpoc.utils

//package com.mmntvideo.utils.storage

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.demo.musictrimpoc.R

import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object StorageUtils {

    fun getRootDirPath(context: Context): String? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file: File = ContextCompat.getExternalFilesDirs(
                context.applicationContext,
                null
            )[0]
            file.absolutePath
        } else {
            context.applicationContext.filesDir.absolutePath
        }
    }

    fun Context.getRootAppDirPath(eventId: String): String? {
        val dir = this.applicationContext.filesDir.absolutePath + "/DownloadVideos/$eventId"
        try {
            if (!File(dir).exists()) {
                File(dir).mkdirs()     // make sure to call mkdirs() when creating new directory
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dir
    }

    fun getFileSize(size: Long): String {
        if (size <= 0)
            return "0"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    fun Context.copyFileIntoGallery(outputFile: File, code: String): File? {

        val desFile = File(
            Environment.getExternalStorageDirectory().path + "/${
                this.getString(R.string.app_name)
            }"
        )
        desFile.mkdirs()
        val file = File(desFile.absolutePath + "/" + "merged_$code.mp4")
        try{
            if (!file.exists()) {
                file.createNewFile()
                outputFile.copyTo(file, true)
            }
        }catch (e :Exception){
            e.printStackTrace()
        }
        return file
    }

    fun Context.getRootAppMusicDir(eventId: String): String? {
        val dir = this.applicationContext.filesDir.absolutePath + "/MusicVideos/$eventId"
        try {
            if (!File(dir).exists()) {
                File(dir).mkdirs()     // make sure to call mkdirs() when creating new directory
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dir
    }

    fun Context.getMergeVideoPath(eventId: String): String {
        val dir = this.applicationContext.filesDir.absolutePath + "/MergeVideos/${
            eventId.substring(
                0,
                5
            )
        }"
        val outputFile = File("$dir/merged${eventId}.mp4")
        try {
            if (!outputFile.exists()) {
                File(dir).mkdirs()     // make sure to call mkdirs() when creating new directory
                outputFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputFile.path
    }

}