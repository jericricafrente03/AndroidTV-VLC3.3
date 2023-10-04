package com.bittelasia.libvlc.listener

import com.bittelasia.libvlc.model.VideoInfo

interface OnPlayerListener {
    fun playerLoad(message: String)
    fun playerStopped(videoInfo: VideoInfo?, message: String)
}