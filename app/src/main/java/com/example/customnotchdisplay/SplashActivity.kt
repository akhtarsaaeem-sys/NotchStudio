package com.example.customnotchdisplay

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.widget.ImageView>(R.id.splashLogo)
        val title = findViewById<android.widget.TextView>(R.id.splashTitle)
        val subtitle = findViewById<android.widget.TextView>(R.id.splashSubtitle)

        // Entrance animation: logo scales/fades in, then title and subtitle fade+rise in shortly after.
        logo.scaleX = 0.7f
        logo.scaleY = 0.7f
        logo.alpha = 0f
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(380)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        title.translationY = 20f
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(180)
            .setDuration(320)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        subtitle.translationY = 20f
        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(260)
            .setDuration(320)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // 1000 milliseconds = 1 second. Change this number to make it longer or shorter!
        Handler(Looper.getMainLooper()).postDelayed({
            // Open the Main Dashboard
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            // "finish()" kills the loading screen so the user can't hit the back button and return to it
            finish()
        }, 1000)
    }
}