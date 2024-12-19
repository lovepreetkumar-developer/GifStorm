package com.techfathers.gifstorm.util

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.widget.RelativeLayout
import timber.log.Timber
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat

class ScrollingFABBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<RelativeLayout>() {
    /*This handler is for to show the hidden layout after 1 second after scrolling*/
    private val mHandler: Handler? = null

    /*@Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RelativeLayout child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);

        if (mHandler == null)
            mHandler = new Handler();

        mHandler.postDelayed(() -> {
            //child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            Timber.d("startHandler()");
        }, 1000);
    }*/
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: RelativeLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )

        //child -> Floating Action Button
        if (dyConsumed > 0) {
            Timber.d("Up")
            val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
            val fab_bottomMargin = layoutParams.bottomMargin
            child.animate().translationY((child.height + fab_bottomMargin).toFloat())
                .setInterpolator(LinearInterpolator()).start()
        } else if (dyConsumed < 0) {
            Timber.d("down")
            child.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
        }
    }

    /*@Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RelativeLayout child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        if (mHandler != null) {
            mHandler.removeMessages(0);
            Timber.d("stopHandler()");
        }
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }*/
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: RelativeLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int
    ): Boolean {
        /*if (mHandler != null) {
            mHandler.removeMessages(0);
            Timber.d("stopHandler()");
        }*/
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }
}