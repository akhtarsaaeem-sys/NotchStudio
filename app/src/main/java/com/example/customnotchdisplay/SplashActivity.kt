package com.example.customnotchdisplay

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1000 milliseconds = 1 second. Change this number to make it longer or shorter!
        Handler(Looper.getMainLooper()).postDelayed({
            // Open the Main Dashboard
            startActivity(Intent(this, MainActivity::class.java))

            // "finish()" kills the loading screen so the user can't hit the back button and return to it
            finish()
        }, 1000)
    }
}