package com.lyrebirdstudio.croppylib.util.util

fun min(vararg numbers: Float): Float {
    return numbers.min() ?: numbers.first()
}
