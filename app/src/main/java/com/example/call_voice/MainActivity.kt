package com.example.call_voice


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.telephony.PhoneStateListener
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*


class MainActivity : Activity() {
    private var tts: TextToSpeech? = null
    lateinit var tts_button : Button
    var tospeak: Boolean = false
    lateinit var listener: PhoneStateListener
    var last: String = "Unknown Number"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        tts_button  = findViewById(R.id.tts_button)

        val i = Intent(this, Background::class.java)
        i.putExtra("stop","no")
        startService(i)
        tts_button.setOnClickListener {
            val i = Intent(this, Background::class.java)
            i.putExtra("stop","stop")
            startService(i)

        }

    }


    val MULTIPLE_PERMISSIONS = 10 // code you want.


    var permissions = arrayOf<String>(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_NUMBERS,


        )


    fun deleteCache(context: Context) {
        try {
            val dir: File = context.getCacheDir()
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile()) {
            dir.delete()
        } else {
            false
        }
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.size > 0) {
                    var permissionsDenied = ""
                    for (per in permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += """
                            
                            $per
                            """.trimIndent()
                        }
                    }
                    // Show permissionsDenied

                }
                return
            }
        }
    }








}