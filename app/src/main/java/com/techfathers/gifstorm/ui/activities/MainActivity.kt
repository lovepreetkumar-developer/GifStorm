package com.techfathers.gifstorm.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.techfathers.gifstorm.BuildConfig
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.util.ApiConstants
import com.techfathers.gifstorm.util.getDatabaseReference
import com.techfathers.gifstorm.util.hideStatusBar
import timber.log.Timber

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideStatusBar()
        checkAppUpdate()
    }

    private fun checkAppUpdate() {

        if (!BuildConfig.DEBUG) {
            getDatabaseReference().child(BuildConfig.SECRET_CODE)
                .child(ApiConstants.APP_VERSION)
                .child(ApiConstants.VERSION_CODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        dataSnapshot.value?.let {
                            val versionCode = dataSnapshot.value as Long

                            val pInfo = packageManager.getPackageInfo(packageName, 0)

                            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    versionCode > pInfo.longVersionCode
                                } else {
                                    versionCode > BuildConfig.VERSION_CODE
                                }
                            ) {
                                startActivity(
                                    Intent(
                                        applicationContext,
                                        UpdateAppActivity::class.java
                                    )
                                )
                            }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.e(error.message)
                    }
                })
        } else {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
            val graphInflater = navHostFragment.navController.navInflater
            val navGraph = graphInflater.inflate(R.navigation.nav_graph_main)
            val navController = navHostFragment.navController
            navGraph.setStartDestination(R.id.home_fragment)
            navController.setGraph(navGraph, Bundle())
        }
    }
}