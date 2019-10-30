package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> ViewGroup.inflateAdapterItem(layoutRes: Int): T = DataBindingUtil.inflate(LayoutInflater.from(this.context), layoutRes, this, false)

fun <T : ViewDataBinding> ViewGroup.inflateCustomView(layoutRes: Int): T = DataBindingUtil.inflate(LayoutInflater.from(this.context), layoutRes, this, true)