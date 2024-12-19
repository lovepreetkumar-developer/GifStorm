package com.techfathers.gifstorm.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.data.preferences.PreferenceProvider
import com.techfathers.gifstorm.ui.activities.MainActivity
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

abstract class BaseActivityAdvance<V : ViewDataBinding> : AppCompatActivity(), KodeinAware {

    protected lateinit var binding: V
    protected var context: Context? = null
    private val mPrefProvider: PreferenceProvider by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())
        binding = DataBindingUtil.setContentView(this, getContentView())
        this.context = this
        onViewReady(savedInstanceState, intent)

        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goBackAnimation()
    }

    protected open fun setBaseCallback(baseCallback: BaseCallback?) {
        binding.setVariable(BR.callback, baseCallback)
    }

    @CallSuper
    open fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
    }

    abstract fun getContentView(): Int

    protected open fun goToHomeScreen() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                goNextAnimation()
            }
        )
    }

    protected open fun goBack() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    protected open fun finishActivityWithBackAnim() {
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    protected open fun goNextAnimation() {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
    }

    protected open fun goNextSwipeAnimation() {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
    }

    protected open fun goBackAnimation() {
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }
}