package com.bittelasia.libvlc.annotation

import com.bittelasia.libvlc.model.ScaleType


@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class PlayerSettings(
    val scaleType: ScaleType,
    val preventDeadLock: Boolean,
    val enableDelay: Boolean,
    val showStatus: Boolean
)