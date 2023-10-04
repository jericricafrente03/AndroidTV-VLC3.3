package com.bittelasia.libvlc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bittelasia.libvlc.annotation.FragmentLayout
import com.bittelasia.libvlc.annotation.UpdateContents
import java.util.*

abstract class AppFragment : Fragment() {

    @Throws(RuntimeException::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(checkLayout(this), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            getView(view)
            initializeObject(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun initializeObject(`object`: Any) {
        val clazz: Class<*> = `object`.javaClass
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(UpdateContents::class.java)) {
                method.isAccessible = true
                method.invoke(`object`)
            }
        }
    }

    private fun checkLayout(`object`: Any?): Int {
        if (`object` == null) {
            throw RuntimeException(this.javaClass.simpleName + "The object to layout is null")
        }
        val clazz: Class<*> = `object`.javaClass
        return if (!clazz.isAnnotationPresent(FragmentLayout::class.java)) {
            throw RuntimeException(clazz.simpleName + " is not annotated with Layout")
        } else {
            Objects.requireNonNull(clazz.getAnnotation(FragmentLayout::class.java)).value
        }
    }

    abstract fun getView(v: View?)
}