package com.trx.freshveggies.utils

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.trx.freshveggies.R

object SystemUtils {
    fun applyEdgeToEdge(activity: ComponentActivity, rootView: View) {
        val primaryColor = ContextCompat.getColor(activity, R.color.green_primary)

        // Set status bar to primary color (Green) with light icons
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(primaryColor),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
