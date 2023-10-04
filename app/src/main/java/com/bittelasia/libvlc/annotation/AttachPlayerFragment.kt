package com.bittelasia.libvlc.annotation


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AttachPlayerFragment(val containerID: Int)