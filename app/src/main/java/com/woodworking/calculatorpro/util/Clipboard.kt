package com.woodworking.calculatorpro.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Minimal clipboard helper. Uses only the system ClipboardManager so the app
 * stays free of dependencies and permissions.
 */
object Clipboard {
    fun copy(context: Context, label: String, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
