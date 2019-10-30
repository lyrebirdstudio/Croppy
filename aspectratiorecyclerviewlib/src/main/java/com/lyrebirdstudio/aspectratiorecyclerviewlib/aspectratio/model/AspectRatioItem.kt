package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AspectRatioItem(
    @DimenRes val aspectRatioSelectedWidthRes: Int,
    @DimenRes val aspectRatioUnselectedHeightRes: Int,
    @DrawableRes val socialMediaImageRes: Int = 0,
    @StringRes val aspectRatioNameRes: Int,
    var activeColor: Int,
    var passiveColor: Int,
    var socialActiveColor: Int,
    var socialPassiveColor: Int,
    val aspectRatio: AspectRatio
)