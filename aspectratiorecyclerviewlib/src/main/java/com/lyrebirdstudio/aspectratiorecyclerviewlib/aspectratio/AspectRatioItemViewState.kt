package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import androidx.appcompat.content.res.AppCompatResources
import com.lyrebirdstudio.aspectratiorecyclerviewlib.R
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatioItem

data class AspectRatioItemViewState(
    val aspectRatioItem: AspectRatioItem,
    var isSelected: Boolean
) {

    fun getItemBackground(context: Context): Drawable? {
        val backgroundDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf()
        ).apply {
            cornerRadius =
                context.resources.getDimensionPixelSize(R.dimen.aspect_lib_shape_radius).toFloat()
            shape = RECTANGLE
        }
        when (isSelected) {
            true -> backgroundDrawable.setColor(aspectRatioItem.activeColor)
            false -> backgroundDrawable.setColor(aspectRatioItem.passiveColor)
        }

        return backgroundDrawable
    }

    fun getItemText(context: Context): String {
        return context.getString(aspectRatioItem.aspectRatioNameRes)
    }

    fun getItemTextColor(): Int {
        return when (isSelected) {
            true -> aspectRatioItem.activeColor
            false -> aspectRatioItem.passiveColor
        }
    }

    fun getAspectWidth(context: Context): Int {
        return context.resources.getDimensionPixelSize(aspectRatioItem.aspectRatioSelectedWidthRes)
    }

    fun getAspectHeight(context: Context): Int {
        return context.resources.getDimensionPixelSize(aspectRatioItem.aspectRatioUnselectedHeightRes)
    }

    fun getSocialMediaImageDrawable(context: Context): Drawable? {
        if (aspectRatioItem.socialMediaImageRes != 0) {
            return AppCompatResources.getDrawable(context, aspectRatioItem.socialMediaImageRes)
        }
        return null
    }

    fun getSocialMediaImageColorTint(): Int {
        return when (isSelected) {
            true -> aspectRatioItem.socialActiveColor
            false -> aspectRatioItem.socialPassiveColor
        }
    }
}