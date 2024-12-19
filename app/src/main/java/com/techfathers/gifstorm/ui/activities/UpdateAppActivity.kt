package com.techfathers.gifstorm.ui.activities

import android.content.Intent
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.techfathers.gifstorm.BuildConfig
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.databinding.ActivityUpdateAppBinding
import com.techfathers.gifstorm.ui.base.BaseActivityAdvance
import com.techfathers.gifstorm.ui.base.BaseCallback
import com.techfathers.gifstorm.util.ApiConstants
import com.techfathers.gifstorm.util.getDatabaseReference
import com.techfathers.gifstorm.util.openPlayStore
import com.techfathers.gifstorm.util.setStatusBarColor
import org.kodein.di.android.kodein
import timber.log.Timber

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class UpdateAppActivity() : BaseActivityAdvance<ActivityUpdateAppBinding>() {

    override val kodein by kodein()

    override fun getContentView(): Int = R.layout.activity_update_app

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        initView()
    }

    override fun onBackPressed() {

    }

    private fun initView() {
        setBaseCallback(baseCallback)
        setStatusBarColor(R.color.white)
        setBaseCallback(baseCallback)
        getDatabaseReference().child(BuildConfig.SECRET_CODE)
            .child(ApiConstants.APP_VERSION)
            .child(ApiConstants.VERSION_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    dataSnapshot.value?.let {
                        val versionName = dataSnapshot.value as String
                        binding.currentVersion = versionName
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.message)
                }
            })
    }

    private val baseCallback = BaseCallback {
        if (it.id == R.id.btn_update_app) openPlayStore()
    }
}