package com.lyrebirdstudio.croppylib.util.file

import android.content.Context
import android.os.Environment
import com.lyrebirdstudio.croppylib.main.StorageType.*
import java.io.File


class FileCreator(val context: Context) {

    fun createFile(fileOperationRequest: FileOperationRequest): File {
        return when (fileOperationRequest.storageType) {
            CACHE -> createCacheFile(fileOperationRequest)
            EXTERNAL -> createExternalFile(fileOperationRequest)
        }
    }

    private fun createCacheFile(fileOperationRequest: FileOperationRequest): File {
        val outputDir = context.cacheDir
        return File.createTempFile(
            "img",
            fileOperationRequest.fileName + fileOperationRequest.fileExtension.fileExtensionName,
            outputDir
        )
    }

    private fun createExternalFile(fileOperationRequest: FileOperationRequest): File {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val parentFolder = File(path, "croppy")
            .also { it.mkdirs() }

        return File(
            parentFolder,
            "${fileOperationRequest.fileName}${fileOperationRequest.fileExtension.fileExtensionName}"
        )
    }
}