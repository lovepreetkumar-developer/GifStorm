package com.techfathers.gifstorm.ui.base;

import android.view.View;

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

public interface BaseCallback {
    void onClick(View view);
    default void onViewClick(View view, int position){}
}
