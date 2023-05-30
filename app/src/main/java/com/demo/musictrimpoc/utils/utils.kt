package com.demo.musictrimpoc.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import com.demo.musictrimpoc.databinding.ShowprocessingdialogBinding

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


fun getStringForTime(timeMs: Long): String {
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

fun audioLength(audioPath: String): Long {
    val retriever = try {
        MediaMetadataRetriever()
            .apply { setDataSource(audioPath) }
    } catch (e: IllegalArgumentException) {
        return 0L
    }

    val length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()

    return length?.toLong() ?: 0L
}

fun camera(singleImagesResult: ActivityResultLauncher<Intent>) {
    try {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val intentAction = "android.intent.extras.CAMERA_FACING"
        when {

            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> {
                takePicture.putExtra(
                    intentAction,
                    1
                )  // Tested on API 24 Android version 7.0(Samsung S6)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                takePicture.putExtra(
                    intentAction,
                    1
                ) // Tested on API 27 Android version 8.0(Nexus 6P)
                takePicture.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            }
            else -> takePicture.putExtra(
                intentAction,
                1
            )  // Tested API 21 Android version 5.0.1(Samsung S4)
        }
        singleImagesResult.launch(takePicture)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openGallery(singleImagesResult: ActivityResultLauncher<Intent>) {
    try {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhoto.type = "image/*"
        singleImagesResult.launch(pickPhoto)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.saveImage(myBitmap: Bitmap): String {
    try {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory =
            File((this.externalCacheDir).toString() + "/amaken")
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        try {
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance().timeInMillis).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.path), arrayOf("image/jpeg"), null)
            fo.close()

            return f.absolutePath

        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}


private var processingDialog: AlertDialog? = null
private var processDialogBinding: ShowprocessingdialogBinding? = null
fun Context.showProcessingDialog() {
    try {
        hideProcessingDialog()
        val customAlertBuilder = AlertDialog.Builder(this)
        processDialogBinding =
            ShowprocessingdialogBinding.inflate(LayoutInflater.from(this), null, false)
        customAlertBuilder.setView(processDialogBinding?.root)
        customAlertBuilder.setCancelable(false)
        processingDialog = customAlertBuilder.create()
        processingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        processingDialog?.show()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun setProcessDialogTitle(title: String) {
    try {
        if (processingDialog != null && processDialogBinding != null) {
            //processDialogBinding?.tvUserName?.text = title
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun hideProcessingDialog() {
    try {
        if (processingDialog != null) {
            processingDialog!!.dismiss()
        }
    } catch (e: Exception) {
        e.printStackTrace()

    }
}


