package com.demo.musictrimpoc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.demo.musictrimpoc.databinding.ActivityMainBinding
import com.demo.musictrimpoc.utils.*
import com.demo.musictrimpoc.utils.FfmpegUtils.doCompressAudio
import com.demo.musictrimpoc.utils.FfmpegUtils.doMergerAudios
import com.demo.musictrimpoc.utils.FfmpegUtils.doTrimSong
import com.pro.audiotrimmer.AudioTrimmerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var originalAudioFile : File ? = null
    private var trimAudioFile : File ? = null
    private var frontTrim : File? = null
    private var rearTrim : File  ? = null
    private var mp = MediaPlayer()
    private var selectedStartTime : Long = 0L
    private var selectedEndTime : Long = 0L

    private val ADD_AUDIO = 1001
    private val btnAudioTrim: Button? = null
    private val REQUEST_ID_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.tvPickAudio.setOnClickListener {
            if(checkStoragePermission()){
                val intentUpload = Intent()
                intentUpload.type = "audio/*"
                intentUpload.action = Intent.ACTION_GET_CONTENT
                resultIndent.launch(intentUpload)
            }else{
                requestStoragePermission()
            }
        }

        binding.icPlayPause.setOnClickListener {
            if(mp.isPlaying){
                pause()
            }else
               playSong()
        }

        binding.icUndo.setOnClickListener {
            trimAudioFile = originalAudioFile
            setDataOnWave(originalAudioFile)
        }

        binding.icDelete.setOnClickListener {
           deleteAndMergeSong()
        }

        binding.icTrim.setOnClickListener {
           if(trimAudioFile != null){
               showProcessingDialog()
               doTrimSong(
                   trimAudioFile!!,
                   getStringForTime(selectedStartTime),
                   getStringForTime(selectedEndTime),
                   "new"

               ){ isSuccess ,  file ->
                   Log.e("MAIN", " trim ${file?.absolutePath}")
                   hideProcessingDialog()
                   if(isSuccess) {
                       trimAudioFile = file
                       setDataOnWave(file)
                   }else{
                       Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                   }
               }
           }
            else{
               Toast.makeText(this, "trimAudioFile null ", Toast.LENGTH_SHORT).show()
           }
        }

        Handler(Looper.myLooper()!!).postDelayed({
            binding.trimAudio.setOnSelectedRangeChangedListener(object :
                AudioTrimmerView.OnSelectedRangeChangedListener {
                override fun onSelectRangeStart() {
                    Log.e("AUDIO" ,"onSelectRangeStart")

                }

                override fun onSelectRange(startMillis: Long, endMillis: Long) {
                    Log.e("AUDIO" ,"onSelectRange ${startMillis} endMillis ${endMillis}")
                    selectedStartTime = startMillis
                    selectedEndTime = endMillis

                }

                override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
                    Log.e("AUDIO" ,"onSelectRangeEnd ${startMillis} endMillis ${endMillis}")
                }

                override fun onProgressStart() {
                    Log.e("AUDIO" ,"onProgressStart")
                }

                override fun onProgressEnd(millis: Long) {
                    Log.e("AUDIO" ,"onProgressEnd ${millis}")
                }

                override fun onDragProgressBar(millis: Long) {
                    Log.e("AUDIO" ,"onDragProgressBar ${millis}")
                }

            })

        }, 1000)
    }

    private fun deleteAndMergeSong() {
        if(trimAudioFile != null){
            showProcessingDialog()
            doTrimSong(
                trimAudioFile!!,
                "00:00:00",
                getStringForTime(selectedStartTime),
                "new"

            ){ isSuccess ,  file ->

                frontTrim = file
                val songEndTime = mp.duration?:0

                if(songEndTime.toLong() != selectedEndTime){
                    doTrimSong(
                        trimAudioFile!!,
                        getStringForTime(selectedEndTime),
                        getStringForTime(songEndTime.toLong()),
                        "new1"
                    ){ isSuccess1 ,  file1 ->
                        rearTrim = file1
                        if(selectedStartTime == 0L){
                            hideProcessingDialog()
                            if(isSuccess1) {

                                trimAudioFile = file1
                                setDataOnWave(file1)
                            }else{
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                            }

                        }else{

                            doMergerAudios(
                                listOf(
                                    frontTrim?.absolutePath?:"",
                                    rearTrim?.absolutePath?:"",
                                )
                            ){ isSucces , newFIle ->
                                hideProcessingDialog()
                                trimAudioFile = newFIle
                                if(isSuccess)
                                    setDataOnWave(newFIle)
                                else{
                                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                else{
                    hideProcessingDialog()
                    trimAudioFile = file
                    if(isSuccess)
                        setDataOnWave(file)
                    else{
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val resultIndent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it != null) {

               showProcessingDialog()
                CoroutineScope(Dispatchers.IO).launch {
                    val data = it.data
                    Log.e("MAIN", " ${data?.data}")
                    val path = FilePathUtils.getMediaFilePathFor(data?.data!!, this@MainActivity)
                    originalAudioFile = File(path)
                    trimAudioFile = originalAudioFile
                    withContext(Dispatchers.Main){
                        hideProcessingDialog()
                        binding.trimAudio.setAudio(originalAudioFile!!)
                        doCompressAudio(originalAudioFile!!){ success , file ->

                        }
//                        setDataOnWave(originalAudioFile)
//                        binding.trimAudio.visibility = View.VISIBLE
//                        binding.btnLayout.visibility = View.VISIBLE
                    }
                }


            }
    }

    private fun setDataOnWave(songFile: File?){
        if(songFile != null){
            setSongUrl(songFile)
            val dd = mp.duration
            binding.trimAudio.setMaxDuration(dd.toLong())
            binding.trimAudio.setAudio(songFile)
            binding.trimAudio.show()
        }
    }

    private fun playSong() {
        try {
             mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setSongUrl(songFile: File?){
        try {
            if(songFile != null){
                Log.e("AUDIO", " song url ${originalAudioFile}")
                mp.reset()
                mp.setDataSource(songFile.path)
                mp.prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun pause(){
        try {
            mp.pause()
            //mp.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.release()
    }


    private fun checkStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_ID_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ID_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted, Click again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}