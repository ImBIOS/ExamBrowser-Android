package dev.imam.webview.exambrowser

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.imam.webview.exambrowser.DetectConnection.isNetworkStatusAvailable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    var prefs: SharedPreferences? = null
    var wasPenaltyPromptVisible = false
    var penaltyStatus: Date? = null
    var progressBar: ProgressBar? = null
    private var myTimerTask: MyTimerTask? = null
    private var timer: Timer? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // finding progressbar by its id
        progressBar = findViewById(R.id.progressBar1)

        val s = intent.getStringExtra("url")
        val view: WebView = findViewById(R.id.activity_main_webview)
        view.settings.javaScriptEnabled = true
        view.settings.useWideViewPort = true
        view.settings.loadWithOverviewMode = true
        view.settings.setSupportZoom(true)
        view.settings.builtInZoomControls = true
        view.settings.displayZoomControls = false
        view.webViewClient = ExamWebView()
        view.loadUrl("http://$s")
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout?.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN)
        swipeRefreshLayout?.setOnRefreshListener {
            if (isNetworkStatusAvailable(this@MainActivity)) {
                view.reload()
                view.settings.domStorageEnabled = true
            } else {
                Toast.makeText(this@MainActivity, "Url tidak valid/offline", Toast.LENGTH_LONG)
                    .show()
                view.loadDataWithBaseURL(
                    null,
                    "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>",
                    "text/html",
                    "UTF-8",
                    null
                )
                progressBar?.visibility = View.GONE
                swipeRefreshLayout?.isRefreshing = false
                val i = Intent(baseContext, InputAddress::class.java)
                startActivity(i)
                finish()
            }
        }
        wasPenaltyPromptVisible = false
    }

    private inner class ExamWebView : WebViewClient() {

//        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//            super.onPageStarted(view, url, favicon)
//        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (isNetworkStatusAvailable(this@MainActivity)) {
                view.loadUrl(url)
                // display the indefinite progressbar
                progressBar?.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@MainActivity, "Url tidak valid/offline", Toast.LENGTH_LONG)
                    .show()
                view.loadDataWithBaseURL(
                    null,
                    "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>",
                    "text/html",
                    "UTF-8",
                    null
                )
                progressBar?.visibility = View.GONE
                swipeRefreshLayout!!.isRefreshing = false
                val i = Intent(baseContext, InputAddress::class.java)
                startActivity(i)
                finish()
            }
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            progressBar?.visibility = View.GONE
            swipeRefreshLayout!!.isRefreshing = false
            super.onPageFinished(view, url)
        }

//        override fun onReceivedError(
//            view: WebView?,
//            errorCode: Int,
//            description: String?,
//            failingUrl: String?
//        ) {
//            swipeRefreshLayout!!.isRefreshing = false
//            val i = Intent(baseContext, InputAddress::class.java)
//            i.putExtra("valid", "offline")
//            startActivity(i)
//            exitProcess(0)
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            MyDialogFragment().show(supportFragmentManager, "MyDialogFragmentTag")
        }
        if (id == R.id.action_exit) {
            exitProcess(0)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Keluar")
        alertDialogBuilder.setMessage("Yakin keluar dari aplikasi ?").setCancelable(false)
            .setPositiveButton("Ya") { _: DialogInterface?, _: Int -> exitProcess(0) }
            .setNegativeButton("Tidak") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialogBuilder.create().show()
    }

    override fun onResume() {
        super.onResume()
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        penaltyStatus = prefs?.getLong("penaltyStatus", 0)?.let { Date(it) }
        if (penaltyStatus!!.after(Date())) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
            Toast.makeText(
                this,
                "Hukuman Penalty sampai " + sdf.format(penaltyStatus!!),
                Toast.LENGTH_SHORT
            ).show()
            finishAffinity()
        }
        wasPenaltyPromptVisible = false
    }

    override fun onPause() {
        super.onPause()
        if (timer == null) {
            myTimerTask = MyTimerTask()
            timer = Timer()
            timer!!.schedule(myTimerTask, 100, 100)
        }

        // Disable recent-apps button
        val activityManager = applicationContext
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }

    inner class MyTimerTask : TimerTask() {
        override fun run() {
            if (penaltyStatus!!.before(Date())) {
                bringApplicationToFront()
            }
            runOnUiThread {
                if (!wasPenaltyPromptVisible && penaltyStatus!!.before(Date())) {
                    Toast.makeText(
                        applicationContext,
                        "Hukuman Penalti dalam 5 detik",
                        Toast.LENGTH_LONG
                    ).show()
                    wasPenaltyPromptVisible = true
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            Toast.makeText(
                                applicationContext,
                                "Hukuman Penalti diberikan",
                                Toast.LENGTH_LONG
                            ).show()
                            prefs!!.edit().putLong(
                                "penaltyStatus",
                                System.currentTimeMillis() + 10 * 60 * 1000
                            ).apply()
                        }
                    }, 5000)
                }
            }
        }
    }

    private fun bringApplicationToFront() {
        val myKeyManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (myKeyManager.isKeyguardLocked) return
        Log.d("TAG", "====Bringging Application to Front====")
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        try {
            pendingIntent.send()
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }
}