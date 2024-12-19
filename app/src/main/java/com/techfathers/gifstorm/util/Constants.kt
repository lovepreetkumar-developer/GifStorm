package com.techfathers.gifstorm.util

import android.Manifest
import com.techfathers.gifstorm.R

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

object Constants {

    const val SPLASH_TIME_OUT = 2000L

    const val DEVICE_ID = "device_id"
    const val ORDER_ID = "order_id"
    const val USER_LOGIN_TYPE = "userLoginType"
    const val SHOW_LOCK_SCREEN = "show_lock_screen"
    const val REMEMBER = "remember"
    const val FIREBASE_TOKEN = "firebase_token"
    const val NOTIFICATION = "notification"
    const val TIMEZONE = "timezone"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longtitude"
    const val USER = "user"
    const val LANGUAGE = "language"
    const val RATE_COUNT = "rate_count"
    const val CURRENT_DATE = "current_date"
    const val MEMBER_LOGGED_IN = "member_logged_in"
    const val AGREEMENT_HAS_SEEN = "agreement_has_seen"
    const val PAYMENT_INSTRUCTIONS = "payment_instructions"
    const val PAGE_LIMIT = "50"
    val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /*****Social Platform Icons******/
    val SOCIAL_ICONS = arrayOf(
        R.drawable.ic_facebook,
        R.drawable.ic_messenger,
        R.drawable.ic_whatsapp,
        R.drawable.ic_instagram,
        R.drawable.ic_twitter,
        R.drawable.ic_reddit,
        R.drawable.ic_we_chat,
        R.drawable.ic_hangout,
        R.drawable.ic_telegram,
        R.drawable.ic_snap_chat,
        R.drawable.ic_viber,
        R.drawable.ic_share_app
    )

    /*****Social Icons Labels******/
    val SOCIAL_ICONS_LABEL = arrayOf(
        "Facebook",
        "Messenger",
        "WhatsApp",
        "Instagram",
        "Twitter",
        "Reddit",
        "WeChat",
        "Hangout",
        "Telegram",
        "Snap Chat",
        "Viber",
        "Others",
    )

    /*****Gradient Backgrounds******/
    val BACKGROUND_DRAWABLES = arrayOf(
        R.drawable.bg_gradient_0,
        R.drawable.bg_gradient_1,
        R.drawable.bg_gradient_2,
        R.drawable.bg_gradient_3,
        R.drawable.bg_gradient_4,
        R.drawable.bg_gradient_5,
        R.drawable.bg_gradient_6,
        R.drawable.bg_gradient_7,
        R.drawable.bg_gradient_8,
        R.drawable.bg_gradient_9
    )
}