package com.lyrebirdstudio.croppylib.util.file

import com.lyrebirdstudio.croppylib.main.StorageType

data class FileOperationRequest(
    val storageType: StorageType,
    val fileName: String,
    val fileExtension: FileExtension = FileExtension.PNG
) {

    companion object {
        fun createRandom(): FileOperationRequest {
            return FileOperationRequest(
                StorageType.EXTERNAL,
                System.currentTimeMillis().toString(),
                FileExtension.PNG
            )
        }
    }

}