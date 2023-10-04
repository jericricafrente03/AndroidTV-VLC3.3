package com.bittelasia.libvlc.exception

import android.util.Log

class PlayerException(message: String) : RuntimeException(message) {
    init {
        Log.e(this.javaClass.simpleName, "@PlayerException: $message")
    }
}