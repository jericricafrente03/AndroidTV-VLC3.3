package com.bittelasia.libvlc.presenter.control

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NoFastScrollListener(private val recyclerView: RecyclerView) : RecyclerView.OnScrollListener() {

    private var isFastScrollBlocked = false
    private var lastScrollTime: Long = 0

    private val SCROLL_THRESHOLD = 500 // Adjust this value based on your desired threshold

    init {
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_DOWN) {
                    lastScrollTime = System.currentTimeMillis()
                    isFastScrollBlocked = false
                }
                return false
            }
        })
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastScrollTime

        isFastScrollBlocked = elapsedTime < SCROLL_THRESHOLD

        lastScrollTime = currentTime
        recyclerView.layoutManager?.findViewByPosition(0)?.requestLayout()
    }

    fun setFastScrollEnabled(enabled: Boolean) {
        isFastScrollBlocked = !enabled
    }

    fun isFastScrollBlocked(): Boolean {
        return isFastScrollBlocked
    }
}