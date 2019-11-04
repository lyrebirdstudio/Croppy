package com.lyrebirdstudio.croppylib.main

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapSaver
import com.lyrebirdstudio.croppylib.util.file.FileCreator
import com.lyrebirdstudio.croppylib.util.file.FileExtension
import com.lyrebirdstudio.croppylib.util.file.FileOperationRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CroppyActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    private val bitmapSaver = BitmapSaver()

    private val fileCreator = FileCreator(application.applicationContext)

    private val saveBitmapLiveData = MutableLiveData<Uri>()

    fun getSaveBitmapLiveData(): LiveData<Uri> = saveBitmapLiveData

    fun saveBitmap(cropRequest: CropRequest, croppedBitmapData: CroppedBitmapData) {

        when (cropRequest) {
            is CropRequest.Manual -> {
                disposable.add(bitmapSaver
                    .saveBitmap(croppedBitmapData, cropRequest.destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { saveBitmapLiveData.value = cropRequest.destinationUri })
            }
            is CropRequest.Auto -> {
                val destinationUri = fileCreator.createFile(
                    FileOperationRequest(
                        cropRequest.storageType,
                        System.currentTimeMillis().toString(),
                        FileExtension.PNG
                    )
                ).toUri()

                disposable.add(bitmapSaver
                    .saveBitmap(croppedBitmapData, destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { saveBitmapLiveData.value = destinationUri })

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