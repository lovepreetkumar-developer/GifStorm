package com.techfathers.gifstorm.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.techfathers.gifstorm.BuildConfig
import com.techfathers.gifstorm.R


/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/*fun pauseLottieAnimation(view: LottieAnimationView) {
    view.visibility = View.GONE
    view.pauseAnimation()
}

fun playLottieAnimation(view: LottieAnimationView) {
    view.visibility = View.VISIBLE
    view.playAnimation()
}*/

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackBar ->
        snackBar.setAction("Ok") {
            snackBar.dismiss()
        }
    }.show()
}

fun Context.getPlayStoreAppLink(): String {
    return "https://play.google.com/store/apps/details?id=${packageName}"
}

fun Context.openPlayStore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getPlayStoreAppLink())))
    }
}

fun getDatabaseReference(): DatabaseReference {
    return FirebaseDatabase.getInstance().reference
}

@Suppress("DEPRECATION")
fun Activity.hideStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

@Suppress("DEPRECATION")
fun Activity.showStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window?.insetsController?.show(WindowInsets.Type.statusBars())
    } else {
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

// hide keyboard
private var inputMethodManager: InputMethodManager? = null

fun hideSoftKeyboard(activity: Activity) {
    if (inputMethodManager == null) inputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (activity.currentFocus != null) inputMethodManager!!.hideSoftInputFromWindow(
        activity.currentFocus!!.windowToken,
        0
    )
}

fun showSoftKeyboard(activity: Activity) {
    if (inputMethodManager == null) inputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (activity.currentFocus != null) inputMethodManager!!.showSoftInput(activity.currentFocus, 0)
}

fun shareApplication(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_content))
        context.startActivity(Intent.createChooser(shareIntent, "choose one"))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun Activity.statusBarWork() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun Activity.setStatusBarColor(color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun getTenorApiKey(): String {
    return if (BuildConfig.DEBUG) BuildConfig.TENOR_SERVER_TEST_KEY
    else BuildConfig.TENOR_SERVER_LIVE_KEY
}

