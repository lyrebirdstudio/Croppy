package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyrebirdstudio.aspectratiorecyclerviewlib.R
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio


class AspectRatioRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var selectedIndex: Int = -1

    private val aspectRatioListAdapter = AspectRatioListAdapter()

    private lateinit var aspectRatioItemViewStateList: List<AspectRatioItemViewState>

    private var onItemSelectedListener: ((AspectRatioItemViewState) -> Unit)? = null

    private var activeColor: Int = ContextCompat.getColor(context, R.color.color_aspect_active)

    private var passiveColor: Int = ContextCompat.getColor(context, R.color.color_aspect_passive)

    private var socialActiveColor: Int =
        ContextCompat.getColor(context, R.color.color_aspect_social_active)

    private var socialPassiveColor: Int =
        ContextCompat.getColor(context, R.color.color_aspect_social_passive)

    init {
        loadAttributes(attrs)
        initialize()
    }

    private fun initialize() {
        aspectRatioItemViewStateList = AspectRatioDataProvider.getAspectRatioViewStateList(
            activeColor,
            passiveColor,
            socialActiveColor,
            socialPassiveColor
        )

        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        adapter = aspectRatioListAdapter

        aspectRatioListAdapter.updateItemList(aspectRatioItemViewStateList)

        select(0)

        aspectRatioListAdapter.onItemSelected = {
            onItemSelected(it)
            onItemSelectedListener?.invoke(it)
        }
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioRecyclerView)
            activeColor =
                typedArray.getColor(R.styleable.AspectRatioRecyclerView_activeColor, activeColor)
            passiveColor =
                typedArray.getColor(R.styleable.AspectRatioRecyclerView_passiveColor, passiveColor)
            socialActiveColor = typedArray.getColor(
                R.styleable.AspectRatioRecyclerView_socialActiveColor,
                socialActiveColor
            )
            socialPassiveColor = typedArray.getColor(
                R.styleable.AspectRatioRecyclerView_socialPassiveColor,
                socialPassiveColor
            )

        } finally {
            typedArray?.recycle()
        }
    }

    fun reset() {
        select(DEFAULT_INDEX)
    }

    fun excludeAspectRatio(vararg excludedAspect: AspectRatio) {
        val includedList = arrayListOf<AspectRatioItemViewState>()
        aspectRatioItemViewStateList.forEach { existingAspect ->
            var isExcluded = false
            excludedAspect.forEach { excludedAspect ->
                if (excludedAspect == existingAspect.aspectRatioItem.aspectRatio) {
                    isExcluded = true
                }
            }

            if (isExcluded.not()) {
                includedList.add(existingAspect)
            }
        }

        aspectRatioItemViewStateList = includedList

        selectedIndex = -1
        select(DEFAULT_INDEX)

        aspectRatioListAdapter.updateItemList(aspectRatioItemViewStateList)
    }

    fun setActiveColor(colorRes: Int) {
        activeColor = ContextCompat.getColor(context, colorRes)
        aspectRatioItemViewStateList.forEach {
            it.aspectRatioItem.activeColor = activeColor
        }
        aspectRatioListAdapter.updateItemList(aspectRatioItemViewStateList)
    }

    fun setItemSelectedListener(onItemSelectedListener: (AspectRatioItemViewState) -> Unit) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthSpec),
            resources.getDimensionPixelSize(R.dimen.size_aspect_recyclerview)
        )
    }

    private fun onItemSelected(aspectRatioItemViewState: AspectRatioItemViewState) {
        aspectRatioItemViewStateList.let { viewStateList ->
            val newSelectedIndex = viewStateList.indexOf(aspectRatioItemViewState)

            select(newSelectedIndex)

            selectedIndex = newSelectedIndex

            aspectRatioItemViewStateList = viewStateList
        }

        aspectRatioListAdapter.updateItemList(aspectRatioItemViewStateList)
    }

    private fun select(index: Int) {
        if (selectedIndex == index) {
            return
        }

        if (index == -1) {
            select(DEFAULT_INDEX)
        }

        aspectRatioItemViewStateList.forEach { it.isSelected = false }

        aspectRatioItemViewStateList[index].isSelected = true

        selectedIndex = index
    }

    companion object {
        private const val DEFAULT_INDEX = 0

    }
}