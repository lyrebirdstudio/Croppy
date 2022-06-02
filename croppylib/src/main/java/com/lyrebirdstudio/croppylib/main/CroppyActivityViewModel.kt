package com.lyrebirdstudio.croppylib.main

import android.app.Application
import android.graphics.Rect
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapUtils
import com.lyrebirdstudio.croppylib.util.file.FileCreator
import com.lyrebirdstudio.croppylib.util.file.FileExtension
import com.lyrebirdstudio.croppylib.util.file.FileOperationRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

typealias CropUriRectPair = Pair<Uri, Rect>

class CroppyActivityViewModel(val app: Application) : AndroidViewModel(app) {

    private val disposable = CompositeDisposable()

    private val saveBitmapLiveData = MutableLiveData<CropUriRectPair>()

    fun getSaveBitmapLiveData(): LiveData<CropUriRectPair> = saveBitmapLiveData

    fun saveBitmap(cropRequest: CropRequest, croppedBitmapData: CroppedBitmapData) {

        when (cropRequest) {
            is CropRequest.Manual -> {
                disposable.add(
                    BitmapUtils
                    .saveBitmap(croppedBitmapData, cropRequest.destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { saveBitmapLiveData.value = cropRequest.destinationUri to croppedBitmapData.croppingRect })
            }
            is CropRequest.Auto -> {
                val destinationUri = FileCreator.createFile(
                    FileOperationRequest(
                        cropRequest.storageType,
                        System.currentTimeMillis().toString(),
                        FileExtension.PNG
                    ),
                    app.applicationContext
                ).toUri()

                disposable.add(
                    BitmapUtils
                    .saveBitmap(croppedBitmapData, destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { saveBitmapLiveData.value = destinationUri to croppedBitmapData.croppingRect })

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable.isDisposed.not()) {
            disposable.dispose()
        }
    }

}