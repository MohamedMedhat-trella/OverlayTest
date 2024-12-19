package com.trella.overlaytest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trella.overlaytest.ui.theme.OverlayTestTheme

const val OVERLAY_ID = "overlay"

class MainActivity : ComponentActivity() {

    companion object {
        val windowManagerViews = hashMapOf<String, View>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverlayTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Button(
                            onClick = {

                                // Check for overlay permission. If not enabled, request for it. If enabled, show the overlay
                                if (!Settings.canDrawOverlays(applicationContext)) {
                                    val text: CharSequence =
                                        "Please grant the access to the application."
                                    val duration = Toast.LENGTH_SHORT
                                    val toast = Toast.makeText(applicationContext, text, duration)
                                    toast.show()
                                    startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.fromParts(
                                                "package",
                                                packageName, null
                                            )
                                        )
                                    )
                                } else {
                                    startPowerOverlay()
                                }
                            }
                        ) {
                            Text(text = "Show overlay")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startPowerOverlay() {
        if (windowManagerViews.containsKey(OVERLAY_ID)) {
            return
        }
        // Starts the button overlay.
        val overlayPowerBtn = ImageView(applicationContext)
        windowManagerViews[OVERLAY_ID] = overlayPowerBtn
        overlayPowerBtn.setImageResource(R.drawable.baseline_adb_24)
        val LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // APPLICATION_OVERLAY FOR ANDROID 26+ AS THE PREVIOUS VERSION RAISES ERRORS
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // FOR PREVIOUS VERSIONS USE TYPE_PHONE AS THE NEW VERSION IS NOT SUPPORTED
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        params.height = 110
        params.width = 110

        windowManager.addView(overlayPowerBtn, params)


        overlayPowerBtn.setOnTouchListener(object : OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var latestPressTime: Long = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Save current x/y
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        Toast.makeText(applicationContext, "Hi there", Toast.LENGTH_SHORT).show()
                        println("XYZ: onActionDown")
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        applicationContext.startActivity(
                            Intent(
                                this@MainActivity,
                                SecondActivity::class.java
                            ).apply {
                                flags = FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayPowerBtn, params)
                        return true
                    }
                }
                return false
            }
        })
    }
}
