package com.example.careplus.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.fragment.app.Fragment
import com.example.careplus.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object BottomSheetUtils {
    
    fun setupBottomSheetDialog(fragment: Fragment, dialog: BottomSheetDialog) {
        // Set background transparent
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                it.setBackgroundResource(R.drawable.bottom_sheet_background)
//                behavior.peekHeight = fragment.resources.displayMetrics.heightPixels / 2
            }
        }
        
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
} 