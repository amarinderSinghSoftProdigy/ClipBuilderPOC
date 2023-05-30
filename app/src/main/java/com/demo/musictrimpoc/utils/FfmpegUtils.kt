package com.demo.musictrimpoc.utils


import android.app.Activity
import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.Config
//import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


object FfmpegUtils {



    fun Context.doCompressAudio(
        audioFile: File,
        returnData: (Boolean, File?) -> Unit,
    ) {

        val extension: String = audioFile.absolutePath.substring(audioFile.absolutePath.lastIndexOf("."))

        val dir = this.filesDir?.absolutePath + "/" + "Compress"
        val outputFile = File("$dir/${System.currentTimeMillis()}${".mp3"}")

        try {
            if (!outputFile.exists()) {
                File(dir).mkdirs()     // make sure to call mkdirs() when creating new directory
                outputFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//  ffmpeg -i input.wav -codec:a libmp3lame -qscale:a 2 output.mp3

        val cmd = arrayOf(
            "-y",
            "-i",
            audioFile.path,
            "-acodec",
            "libmp3lame",
//            "-qscale:a",
//            "2",
            outputFile.path
        )
        Log.e("TAG", "compress: ${cmd.toString()}")
        FFmpeg.cancel()
        FFmpeg.executeAsync(cmd) { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.e("FIle", " file ${outputFile.path}")
                    returnData(true, outputFile)
                }
                Config.RETURN_CODE_CANCEL -> {
                    returnData(false, null)
                }
                else -> {
                    returnData(false, null)
                }
            }
        }
    }
    fun Context.doTrimSong(
        audioFile: File,
        startTime: String?,
        endTime: String,
        _id: String,
        returnData: (Boolean, File?) -> Unit,
    ) {
        val extension: String = audioFile.absolutePath.substring(audioFile.absolutePath.lastIndexOf("."))

        val dir = this.filesDir?.absolutePath + "/" + "TrimmedSong"
        val outputFile = File("$dir/${System.currentTimeMillis()}${extension}")

        try {
            if (!outputFile.exists()) {
                File(dir).mkdirs()     // make sure to call mkdirs() when creating new directory
                outputFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cmd = StringBuilder().apply {
            append("-y")
            append("-i")
            append(audioFile.path)
            append("-ss")
            append(startTime)
            append("-to")
            append(endTime)
            append("-c")
            append("copy")
            append(outputFile.path)
        }
        val cmd2 = arrayOf(
            "-y",
            "-i",
            audioFile.path,
            "-ss",
            startTime,
            "-to",
            endTime,
            "-c",
            "copy",
            outputFile.path
        )
        Log.e("TAG", "doTrimSong: ${cmd.toString()}")
        FFmpeg.cancel()
        FFmpeg.executeAsync(cmd2) { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.e("FIle", " file ${outputFile.path}")
                    returnData(true, outputFile)
                }
                Config.RETURN_CODE_CANCEL -> {
                    returnData(false, null)
                }
                else -> {
                    returnData(false, null)
                }
            }
        }
    }


    fun Activity.doMergerAudios(
        audioFiles: List<String>,
        returnData: (Boolean, File?) -> Unit,
    ) {

//        setProcessDialogTitle(getString(R.string.processing))
        var extension: String = ".mp3"
        if(audioFiles.isNotEmpty())
          extension = audioFiles[0].substring(audioFiles[0].lastIndexOf("."))
        Log.e("MAIN", " extension $extension")
        val dir = this.filesDir?.absolutePath + "/" + "Merger"
        val outputFile = File("$dir/${System.currentTimeMillis()}${extension}")

        try {
            if (!outputFile.exists()) {
                File(dir).mkdirs()
                outputFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val builder = StringBuilder().apply {
            for (i in audioFiles.indices) {
                append("-y ")
                append("-i ")
                append("${File(audioFiles[i]).path} ")
            }
            append("-filter_complex ")
            for (i in audioFiles.indices) {
                append("[$i:0]")
            }
            append("concat=n=${audioFiles.size}")
            append(":v=0:a=1[out] ")
            append("-map [out] ")
            append("${outputFile.path} ")
        }

        Log.e("CMD", " cmd $builder")

        FFmpeg.cancel()
        FFmpeg.executeAsync(builder.toString()) { executionId, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.e("FIle", " audio video ${outputFile.path}")
                    returnData(true, outputFile)
                }
                Config.RETURN_CODE_CANCEL -> {
                    returnData(false, null)
                }
                else -> {
                    returnData(false, null)
                }
            }
        }

//        EpEditor.execCmd(builder.toString(), 0, object : OnEditorListener {
//            override fun onSuccess() {
//                Log.e("FIle", " file ${outputFile.path}")
//                returnData(true, outputFile)
//            }
//            override fun onFailure() {
//                returnData(false, null)
//            }
//            override fun onProgress(progress: Float) {}
//        })
    }


}