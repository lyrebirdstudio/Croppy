package com.lyrebirdstudio.croppylib.ui

import android.app.Application
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.state.CropFragmentViewState
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapResizer
import com.lyrebirdstudio.croppylib.util.bitmap.ResizedBitmap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class ImageCropViewModel(app: Application) : AndroidViewModel(app) {

    private val compositeDisposable = CompositeDisposable()

    private val bitmapResizer = BitmapResizer(app.applicationContext)

    private var cropRequest: CropRequest? = null

    private val cropViewStateLiveData = MutableLiveData<CropFragmentViewState>()
        .apply {
            value = CropFragmentViewState(aspectRatio = AspectRatio.ASPECT_FREE)
        }

    private val resizedBitmapLiveData = MutableLiveData<ResizedBitmap>()

    fun setCropRequest(cropRequest: CropRequest) {
        this.cropRequest = cropRequest

        bitmapResizer
            .resize(cropRequest.sourceUri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { resizedBitmapLiveData.value = it })
            .also { compositeDisposable.add(it) }


        cropViewStateLiveData.value =
            cropViewStateLiveData.value?.onThemeChanged(croppyTheme = cropRequest.croppyTheme)
    }

    fun getCropRequest(): CropRequest? = cropRequest

    fun getCropViewStateLiveData(): LiveData<CropFragmentViewState> = cropViewStateLiveData

    fun getResizedBitmapLiveData(): LiveData<ResizedBitmap> = resizedBitmapLiveData

    fun updateCropSize(cropRect: RectF) {
        cropViewStateLiveData.value =
            cropViewStateLiveData.value?.onCropSizeChanged(cropRect = cropRect)
    }

    fun onAspectRatioChanged(aspectRatio: AspectRatio) {
        cropViewStateLiveData.value =
            cropViewStateLiveData.value?.onAspectRatioChanged(aspectRatio = aspectRatio)
    }

    override fun onCleared() {
        super.onCleared()
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
        }
    }
}