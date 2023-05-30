package com.demo.musictrimpoc.utils


import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.OpenableColumns
import android.text.TextUtils
import java.io.*
import java.util.*

object FilePathUtils {

    fun getPath(context: Context, uri: Uri): String? {
        val fileName: String? = getFileName(uri)
        if (!TextUtils.isEmpty(fileName)) {
            val rootDataDir = context.filesDir
            val copyFile = File(rootDataDir.toString() + File.separator.toString() + fileName)
           // copy(context, uri, copyFile)
            return copyFile.absolutePath
        }
        return null
    }

    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path = uri.path
        val cut = path!!.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }

//    fun copy(context: Context, srcUri: Uri?, dstFile: File?) {
//        try {
//            val inputStream = context.contentResolver.openInputStream(srcUri!!)
//                ?: return
//            val outputStream: OutputStream = FileOutputStream(dstFile)
//            IOUtils.copyStream(inputStream, outputStream)
//            inputStream.close()
//            outputStream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
    fun getMediaFilePathFor(
        uri: Uri,
        context: Context
    ): String {
        val returnCursor =
            context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */

        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.filesDir, name)

        try {
            val inputStream =
                context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()
            //int bufferSize = 1024;
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            //  Timber.e("File Size %d", file.length())
            inputStream.close()
            outputStream.close()
            // Timber.e("File Size %s", file.path)
            //Timber.e("File Size %d", file.length())
        } catch (e: java.lang.Exception) {
            // Timber.e("File Size %s", e.message!!)
        }
        return file.path
    }
    fun Context.saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File((this.externalCacheDir).toString() + "/amaken")
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        try {
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance(Locale.ENGLISH).timeInMillis).toString() + ".jpg")
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
    }
}