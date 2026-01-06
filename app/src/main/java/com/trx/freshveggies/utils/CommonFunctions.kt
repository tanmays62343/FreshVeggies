package com.trx.freshveggies.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

object CommonFunctions {

    fun View.applySystemBarInsets(topView : View? = null, bottomView : View? = null) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                bars.left,
                if(topView != null) 0 else bars.top,
                bars.right,
                if(bottomView != null) 0 else bars.bottom,
            )
            topView?.updateLayoutParams{
                height = bars.top
            }
            bottomView?.updateLayoutParams{
                height = bars.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }

}