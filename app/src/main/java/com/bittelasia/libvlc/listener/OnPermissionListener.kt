package com.bittelasia.libvlc.listener

interface OnPermissionListener {
    fun onPermissionGranted()
    fun onPermissionDenied()
    fun onPermissionAlreadyGranted()
}