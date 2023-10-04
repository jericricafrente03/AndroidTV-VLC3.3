package com.bittelasia.libvlc.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bittelasia.libvlc.annotation.AttachPlayerFragment
import com.bittelasia.libvlc.annotation.PlayerActivityLayout
import com.bittelasia.libvlc.annotation.UpdateContents
import com.bittelasia.libvlc.fragment.PlayerFragment
import com.bittelasia.libvlc.fragment.PlayerVLCFragment
import com.bittelasia.libvlc.listener.OnChangeListener
import com.bittelasia.libvlc.listener.OnFragmentListener
import com.bittelasia.libvlc.listener.OnPermissionListener
import com.bittelasia.libvlc.model.VideoInfo
import com.bittelasia.libvlc.util.ActivityControl.getInput
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
abstract class AbstractPlayerActivity : AppCompatActivity(), OnChangeListener, OnFragmentListener, OnPermissionListener {

    var videoList: ArrayList<VideoInfo> = arrayListOf()
    var isHasControl = false
    var channelIndex = 0
    private var isRestarted = false

    private var playerFragment: PlayerFragment? = null
    private var monitor = 0
    private var timerMonitor: Timer? = null
    private var timerChannel: Timer? = null
    private var timerTaskMonitor: TimerTask? = null
    private var timerTaskChannel: TimerTask? = null
    private var counter: Long = 0
    private var seconds: Long = 0
    private val stats: Long = 0
    private var channelNo = ""

    override fun onRestart() {
        super.onRestart()
        isRestarted = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(checkLayout(this))
            updateObject(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        timerTaskMonitor?.cancel()
        timerTaskChannel?.cancel()
        timerMonitor = null
        timerChannel = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is PlayerFragment) {
            playerFragment = fragment
            if (playerFragment is PlayerVLCFragment) {
                if (videoList.size > 0)
                    (playerFragment as PlayerVLCFragment).play(videoList[0], false)
            }
        }
    }

    override fun onFragmentDetached(fragment: Fragment) {
        if (fragment is PlayerFragment) playerFragment = fragment
    }

    fun loadFragments(channels: ArrayList<VideoInfo>) {
        try {
            videoList.addAll(channels)
            channelIndex = 0
            onChannelIndex(0)
            initializeObject(this)
            playerMonitor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun attachFragment(fragment: Fragment, containerID: Int) {
        runOnUiThread {
            try {
                if (containerID == 0) throw RuntimeException(fragment.tag?.javaClass?.simpleName + " -> Must use non-zero containerViewId")
                supportFragmentManager.beginTransaction().replace(
                    containerID,
                    fragment,
                    fragment.javaClass.simpleName
                ).commitAllowingStateLoss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun <T : PlayerFragment?> attachFragment(player: T, containerID: Int) {
        if (containerID == 0) throw RuntimeException(player?.tag?.javaClass?.simpleName + " -> Must use non-zero containerViewId")
        supportFragmentManager.beginTransaction().replace(
            containerID,
            player!!,
            player.javaClass.simpleName
        ).commitAllowingStateLoss()
    }

    @Throws(Exception::class)
    private fun initializeObject(`object`: Any) {
        val clazz: Class<*> = `object`.javaClass
        for (field in clazz.declaredFields) {
            field.isAccessible = true
            if (field.isAnnotationPresent(AttachPlayerFragment::class.java)) {
                val container = Objects.requireNonNull(
                    field.getAnnotation(
                        AttachPlayerFragment::class.java
                    )
                ).containerID
                if (container == 0) {
                    val fragment = (field[`object`] as Fragment)
                    attachFragment(fragment, container)
                } else if (field[`object`] is PlayerFragment) {
                    val fragment = (field[`object`] as PlayerFragment)
                    attachFragment<PlayerFragment?>(fragment, container)
                } else {
                    val fragment = (field[`object`] as Fragment)
                    attachFragment(fragment, container)
                }
            }
        }
    }

    private fun checkLayout(`object`: Any?): Int {
        if (`object` == null) {
            throw RuntimeException(this.javaClass.simpleName + "The object to layout is null")
        }
        val clazz: Class<*> = `object`.javaClass
        return if (!clazz.isAnnotationPresent(PlayerActivityLayout::class.java)) {
            throw RuntimeException(clazz.simpleName + " is not annotated with Layout")
        } else {
            Objects.requireNonNull(clazz.getAnnotation(PlayerActivityLayout::class.java)).value
        }
    }

    @Throws(Exception::class)
    private fun updateObject(`object`: Any) {
        val clazz: Class<*> = `object`.javaClass
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(UpdateContents::class.java)) {
                method.isAccessible = true
                try {
                    method.invoke(`object`)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun channelTune(pageUp: Boolean): VideoInfo? {
        var video: VideoInfo? = null
        try {
            if (pageUp) {
                channelIndex++
                if (channelIndex < videoList.size) {
                    video = videoList[channelIndex]
                } else {
                    video = videoList[0]
                    channelIndex = 0
                }
            } else {
                channelIndex--
                if (channelIndex >= 0) {
                    video = videoList[channelIndex]
                } else {
                    video = videoList[videoList.size - 1]
                    channelIndex = videoList.size
                }
            }
            onChannelIndex(channelIndex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return video
    }

    fun channelNumber(event: KeyEvent?, player: PlayerVLCFragment): String {
        channelNo += getInput(event!!)
        timerChannel?.purge()
        timerChannel?.cancel()
        timerChannel = null
        timerChannel = Timer()
        timerChannel?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    var info: VideoInfo?
                    for (x in videoList.indices) {
                        val no = videoList[x].channelNo.toString() + ""
                        info = videoList[x]
                        if (no == channelNo) {
                            channelIndex = x
                            timerChannel?.purge()
                            timerChannel?.cancel()
                            onChannelChanged(info)
                            break
                        }
                    }
                    channelNo = ""
                }
            }
        }.also { timerTaskChannel = it }, 3000)
        return channelNo
    }

    private fun playerMonitor() {
        if (timerMonitor == null)
            timerMonitor = Timer()
        timerMonitor?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    if (playerFragment != null) {
                        if (seconds < counter - 2) {
                            if (playerFragment is PlayerVLCFragment)
                                if (!(playerFragment as PlayerVLCFragment).getmMediaPlayer()?.isPlaying!!) {
                                    monitor++
                                    onStatus(
                                        Calendar.getInstance().time.toString() + " => " + (playerFragment as PlayerVLCFragment).uriPath + "",
                                        false
                                    )
                                } else {
                                    monitor = 0
                                }
                        } else {
                            monitor = 0
                        }
                    }
                    counter++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.also { timerTaskMonitor = it }, 0, 1000)
    }

    fun setCounter(counter: Long) {
        this.counter = counter
    }

    fun setSeconds(seconds: Long) {
        this.seconds = seconds
    }

    override fun onPermissionGranted() {}

    override fun onPermissionDenied() {}

    override fun onPermissionAlreadyGranted() {}
}