package com.bittelasia.libvlc.presenter.fragment

import com.bittelasia.libvlc.annotation.PlayerSettings
import com.bittelasia.libvlc.fragment.PlayerVLCFragment
import com.bittelasia.libvlc.model.ScaleType
import com.bittelasia.libvlc.model.VideoInfo

@PlayerSettings(
    scaleType = ScaleType.SURFACE_FILL,
    preventDeadLock = true,
    enableDelay = false,
    showStatus = true
)
class TVPlayerFragment: PlayerVLCFragment() {

    private var source: Any? = null

    override fun getPath(): String {
        var uri = "udp://@238.0.0.5:1234"
        if (source != null) {
            if (source is String)
                uri = source as String
            else if (source is VideoInfo)
                uri = (source as VideoInfo).path!!
        }
        return uri
    }
}