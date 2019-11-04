package com.lyrebirdstudio.croppy

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppy.databinding.ActivityMainBinding
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.main.CroppyTheme
import com.lyrebirdstudio.croppylib.main.StorageType
import com.lyrebirdstudio.croppylib.util.file.FileCreator
import com.lyrebirdstudio.croppylib.util.file.FileOperationRequest


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.buttonChoose.setOnClickListener {
            startCroppy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CROP_IMAGE) {
            data?.data?.let {
                Log.v("TEST", it.toString())
                binding.imageViewCropped.setImageURI(it)
            }
        }
    }

    private fun startCroppy() {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.aa))
            .appendPath(resources.getResourceTypeName(R.drawable.aa))
            .appendPath(resources.getResourceEntryName(R.drawable.aa))
            .build()

        //Saves to external and return uri
        val externalCropRequest = CropRequest.Auto(
            sourceUri = uri,
            requestCode = RC_CROP_IMAGE
        )

        //Saves to cache and return uri
        val cacheCropRequest = CropRequest.Auto(
            sourceUri = uri,
            requestCode = RC_CROP_IMAGE,
            storageType = StorageType.CACHE
        )

        // Save to given destination uri.
        val destinationUri =
            FileCreator
                .createFile(FileOperationRequest.createRandom(), application.applicationContext)
                .toUri()

        val manualCropRequest = CropRequest.Manual(
            sourceUri = uri,
            destinationUri = destinationUri,
            requestCode = RC_CROP_IMAGE
        )

        val excludeAspectRatiosCropRequest = CropRequest.Manual(
            sourceUri = uri,
            destinationUri = destinationUri,
            requestCode = RC_CROP_IMAGE,
            excludedAspectRatios = arrayListOf(AspectRatio.ASPECT_FREE)
        )

        val themeCropRequest = CropRequest.Manual(
            sourceUri = uri,
            destinationUri = destinationUri,
            requestCode = RC_CROP_IMAGE,
            croppyTheme = CroppyTheme(R.color.blue)
        )

        Croppy.start(this, themeCropRequest)
    }

    companion object {
        private const val RC_CROP_IMAGE = 102

    }
}
