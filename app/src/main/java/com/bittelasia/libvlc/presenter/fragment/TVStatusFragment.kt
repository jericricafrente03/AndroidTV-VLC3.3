package com.bittelasia.libvlc.presenter.fragment

import com.bittelasia.libvlc.fragment.PlayerStatusFragment


class TVStatusFragment: PlayerStatusFragment() {
    override fun displayChannel(): Boolean {
        return false
    }
}