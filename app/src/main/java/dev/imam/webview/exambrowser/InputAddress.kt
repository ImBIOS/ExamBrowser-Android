package dev.imam.webview.exambrowser

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import kotlin.system.exitProcess

class InputAddress : Activity() {
    private var inputAddress: EditText? = null
    private var inputLayoutAddress: TextInputLayout? = null

    private inner class MyTextWatcher(private val view: View?) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            if (view!!.id == R.id.input_address) { /*2131230805*/
                validateAddress()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_address)
        try {
            val s = intent.getStringExtra("valid")
            if (s == "offline") {
                Toast.makeText(this@InputAddress, "Input: Url tidak valid/offline", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        inputLayoutAddress = findViewById(R.id.input_layout_address)
        inputAddress = findViewById(R.id.input_address)
        inputAddress?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                inputAddress?.text?.let { inputAddress?.setSelection(it.length) }
            }
        }
        val btnLanjut = findViewById<Button>(R.id.btn_lanjut)
        inputAddress?.addTextChangedListener(MyTextWatcher(inputAddress))
        btnLanjut.setOnClickListener { submitForm() }
        if (!isNetworkAvailable) {
            AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Peringatan").setMessage("Tidak ada koneksi")
                .setPositiveButton("Close") { _: DialogInterface?, _: Int ->
                    finish()
                    exitProcess(0)
                }.show()
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        inputAddress?.setText(prefs.getString("autoSave", "192.168.0.200/pts"))
        inputAddress?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                prefs.edit().putString("autoSave", s.toString()).apply()
            }
        })
        inputAddress?.setOnKeyListener(View.OnKeyListener { _: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == 0) {
                /*23*/
                if (keyCode == R.styleable.Toolbar_titleMarginEnd || keyCode == R.styleable.AppCompatTheme_editTextColor) { /*66*/
                    submitForm()
                    return@OnKeyListener true
                }
            }
            false
        })
    }

    private fun submitForm() {
        if (validateAddress()) {
            val intent = Intent(baseContext, MainActivity::class.java)
            intent.putExtra("url", inputAddress!!.text.toString())
            startActivity(intent)
            finish()
        }
    }

    private fun validateAddress(): Boolean {
        if (inputAddress!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            inputLayoutAddress!!.error = getString(R.string.err_msg_name)
            requestFocus(inputAddress)
            return false
        }
        inputLayoutAddress!!.isErrorEnabled = false
        return true
    }

    private fun requestFocus(view: View?) {
        if (view!!.requestFocus()) {
            window.setSoftInputMode(5)
        }
    }

    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Keluar")
        alertDialogBuilder.setMessage("Yakin keluar dari aplikasi ?").setCancelable(false)
            .setPositiveButton("Ya") { _: DialogInterface?, _: Int -> exitProcess(0) }
            .setNegativeButton("Tidak") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialogBuilder.create().show()
    }
}