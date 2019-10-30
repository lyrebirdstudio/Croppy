package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model

enum class AspectRatio(val widthRatio: Float, val heightRatio: Float) {

    ASPECT_FREE(-1f, -1f),
    ASPECT_INS_1_1(1f, 1f),
    ASPECT_INS_4_5(4f, 5f),
    ASPECT_INS_STORY(9f, 16f),
    ASPECT_5_4(5f, 4f),
    ASPECT_3_4(3f, 4f),
    ASPECT_4_3(4f, 3f),
    ASPECT_FACE_POST(1.91f, 1f),
    ASPECT_FACE_COVER(2.62f, 1f),
    ASPECT_PIN_POST(2f, 3f),
    ASPECT_3_2(3f, 2f),
    ASPECT_9_16(9f, 16f),
    ASPECT_16_9(16f, 9f),
    ASPECT_1_2(1f, 2f),
    ASPECT_YOU_COVER(1.77f, 1f),
    ASPECT_TWIT_POST(1.91f, 1f),
    ASPECT_TWIT_HEADER(3f, 1f),
    ASPECT_A_4(0.7f, 1f),
    ASPECT_A_5(0.7f, 1f)
}