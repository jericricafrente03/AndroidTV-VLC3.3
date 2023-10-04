package com.bittelasia.libvlc.listener

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment

interface OnFragmentListener {
    fun onFragmentDetached(fragment: Fragment)
}