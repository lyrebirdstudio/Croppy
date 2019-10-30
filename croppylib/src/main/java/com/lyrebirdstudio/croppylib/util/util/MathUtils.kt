package com.lyrebirdstudio.croppylib.util.util

fun min(vararg numbers: Float): Float {
    var lowest = numbers[0]
    numbers.forEach {
        lowest = Math.min(lowest, it)
    }
    return lowest
}