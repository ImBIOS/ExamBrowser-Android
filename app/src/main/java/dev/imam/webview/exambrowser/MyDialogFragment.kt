package dev.imam.webview.exambrowser

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment

class MyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Tentang")
        builder.setMessage("Dikembangkan oleh: Imamuzzaki Abu Salam")
        builder.setPositiveButton("OK") { _: DialogInterface?, _: Int -> }
        return builder.create()
    }
}