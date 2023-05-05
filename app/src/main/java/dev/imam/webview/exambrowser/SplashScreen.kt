package dev.imam.webview.exambrowser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashScreen : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val splashDisplayLength = 3000

        Handler(Looper.getMainLooper()).postDelayed({
        val intent = Intent(this@SplashScreen, InputAddress::class.java)
            startActivity(intent)
            finish()
        }, splashDisplayLength.toLong())
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}