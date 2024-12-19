package com.techfathers.gifstorm.ui.fragments

import android.os.Bundle
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.databinding.FragmentDummyBinding
import com.techfathers.gifstorm.ui.base.BaseCallback
import com.techfathers.gifstorm.ui.base.BaseFragment

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class DummyFragment : BaseFragment<FragmentDummyBinding>() {

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_dummy
    }

    override fun onFragmentCreateView(savedInstanceState: Bundle?) {
        super.onFragmentCreateView(savedInstanceState)
        initView()
    }

    private fun initView() {
        setBaseCallback(baseCallback)
    }

    private val baseCallback = BaseCallback {

    }
}