package com.bittelasia.libvlc.presenter.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bittelasia.libvlc.R
import com.bittelasia.libvlc.activity.AbstractPlayerActivity
import com.bittelasia.libvlc.annotation.AttachPlayerFragment
import com.bittelasia.libvlc.annotation.PlayerActivityLayout
import com.bittelasia.libvlc.data.ProjectData
import com.bittelasia.libvlc.model.VideoInfo
import com.bittelasia.libvlc.model.presentermodel.ChannelData
import com.bittelasia.libvlc.presenter.fragment.TVChannelListFragment
import com.bittelasia.libvlc.presenter.fragment.TVLoaderFragment
import com.bittelasia.libvlc.presenter.fragment.TVPlayerFragment
import com.bittelasia.libvlc.presenter.fragment.TVStatusFragment
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

@SuppressLint("NonConstantResourceId")
@PlayerActivityLayout(value = R.layout.activity_main)
class TVPlayerActivity : AbstractPlayerActivity(), TVChannelListFragment.ChannelChangeCallBack {

    private var position = 0
    private var monitorIndexTimer: Timer? = null
    private var monitorPlayerTimer: Timer? = null
    private var monitorPlayerMessage: String? = null
    private var channelList: List<ChannelData>? = null
    private var channel: ChannelData? = null

    private var isTVOpen = false


    @AttachPlayerFragment(R.id.progress_layout)
    val progressFragment = TVLoaderFragment()

    @AttachPlayerFragment(R.id.player_layout)
    val playerFragment = TVPlayerFragment()

    @AttachPlayerFragment(R.id.list_layout)
    val channelListFragment = TVChannelListFragment()

    @AttachPlayerFragment(R.id.status_layout)
    val statusFragment = TVStatusFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var monitorPlayerIndex = 0
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    val projectData = ProjectData().getTv()
                    channelList = projectData
                    download(channelList)
                }
            }
        }
        monitorPlayerTimer = Timer()
        monitorPlayerTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    try {
                        if (monitorPlayerMessage.equals(Calendar.getInstance().time.toString()))
                            monitorPlayerIndex++
                        if (monitorPlayerIndex >= 4) {
                            onEnd()
                            monitorPlayerIndex = 0
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 100, 5000)
    }

    override fun onStart() {
        super.onStart()
        isTVOpen = true
    }

    override fun onStop() {
        super.onStop()
        isTVOpen = false
    }

    override fun onPause() {
        super.onPause()
        isTVOpen = false
        monitorIndexTimer?.cancel()
        monitorIndexTimer?.purge()
        monitorPlayerTimer?.cancel()
        monitorPlayerTimer?.purge()
    }

    override fun onDestroy() {
        super.onDestroy()
        isTVOpen = false
    }

    override fun onResume() {
        super.onResume()
        if (videoList.size > 0)
            statusFragment.updateStatus(this, videoList[position])

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        try {
            when (event.action) {
                KeyEvent.ACTION_DOWN ->
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_NUMPAD_0,
                        KeyEvent.KEYCODE_NUMPAD_1,
                        KeyEvent.KEYCODE_NUMPAD_2,
                        KeyEvent.KEYCODE_NUMPAD_3,
                        KeyEvent.KEYCODE_NUMPAD_4,
                        KeyEvent.KEYCODE_NUMPAD_5,
                        KeyEvent.KEYCODE_NUMPAD_6,
                        KeyEvent.KEYCODE_NUMPAD_7,
                        KeyEvent.KEYCODE_NUMPAD_8,
                        KeyEvent.KEYCODE_NUMPAD_9,
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_MENU,
                        KeyEvent.KEYCODE_POWER,
                        KeyEvent.KEYCODE_SETTINGS,
                        KeyEvent.META_SHIFT_LEFT_ON -> return true

                        KeyEvent.KEYCODE_PAGE_UP,
                        KeyEvent.KEYCODE_CHANNEL_UP,
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            channelListFragment.getClickedVideoItem(true)
                            return true
                        }

                        KeyEvent.KEYCODE_PAGE_DOWN,
                        KeyEvent.KEYCODE_CHANNEL_DOWN,
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            channelListFragment.getClickedVideoItem(false)
                            return true
                        }

                        else -> onChannelChanged(
                            channelNumber(
                                event,
                                playerFragment
                            )
                        )
                    }

                KeyEvent.ACTION_UP -> when (event.keyCode) {
                    KeyEvent.KEYCODE_MENU -> {
                        if (!channelListFragment.isDrawerOpen) {
                            channelListFragment.openDrawer()
                        } else {
                            channelListFragment.closeDrawer()
                        }
                        return true
                    }

                    KeyEvent.KEYCODE_PAGE_UP,
                    KeyEvent.KEYCODE_CHANNEL_UP,
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (!channelListFragment.isDrawerOpen)
                            onChannelChanged(channelTune(true))
                        return true
                    }

                    KeyEvent.KEYCODE_PAGE_DOWN,
                    KeyEvent.KEYCODE_CHANNEL_DOWN,
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (!channelListFragment.isDrawerOpen)
                            onChannelChanged(channelTune(false))
                        return true
                    }

                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                        try {
                            channelIndex = position
                            videoList[position].let { playerFragment.play(it, false) }
                            statusFragment.updateStatus(this, videoList[position])
                            channelListFragment.closeDrawer()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }

                    KeyEvent.META_SHIFT_LEFT_ON -> {
                        try {
                            val i = Intent()
                            i.action = "android.intent.action.LAUNCH.SETTING"
                            sendBroadcast(i)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }

                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_UP -> {
                try {
                    channelIndex = position
                    videoList.apply {
                        this[position].let {
                            playerFragment.play(it, false)
                            statusFragment.updateStatus(this@TVPlayerActivity, it)
                        }
                    }
                    channelListFragment.closeDrawer()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onBufferChanged(buffer: Float) {
        runOnUiThread {
            if (buffer >= 100.0) {
                setCounter(0)
                progressFragment.hideProgress(true)
            } else {
                progressFragment.hideProgress(false)
            }
        }
    }

    override fun onChannelIndex(channelIndex: Int) {
        var monitorIndex = 0
        val tempPosition = position
        position = channelIndex
        monitorIndexTimer?.purge()
        monitorIndexTimer?.cancel()
        monitorIndexTimer = Timer()
        monitorIndexTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    try {
                        if (position == tempPosition)
                            monitorIndex++
                        if (monitorIndex >= 4) {
                            monitorIndexTimer?.purge()
                            monitorIndexTimer?.cancel()
                            channelListFragment.closeDrawer()
                            monitorIndex = 0
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 1000, 5000)
    }

    override fun onChanging(seconds: Long) {
        setSeconds(seconds)
    }

    override fun onStatus(message: String?, isPlaying: Boolean) {
        try {
            if (isPlaying)
                monitorPlayerMessage = message
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChannelChanged(`object`: Any?) {
        if (`object` != null)
        if (!channelListFragment.isDrawerOpen) {
            if (`object` is VideoInfo) {
                try {
                    channel = channelList?.find {
                        it.id.toInt() == `object`.channelNo
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                playerFragment.play(`object`, false)
            }
            statusFragment.updateStatus(this, `object`)
        }

    }

    override fun onError(msg: String?) {
        progressFragment.hideProgress(false)
    }

    override fun onEnd() {
        progressFragment.hideProgress(false)
        playerFragment.stop()
        playerFragment.play(playerFragment.getPath(), true)
    }

    @MainThread
    fun download(list: List<*>?) {
        val videos = mutableListOf<VideoInfo>()
        if (list?.isEmpty() == false) {
            for (ch in list) {
                if (ch is ChannelData) {
                    val videoInfo = VideoInfo()
                    val order = ch.id
                    val title = ch.channelTitle
                    val path = ch.channelUri
                    val desc = ch.channelDescription
                    val cat = ch.channelCategoryId
                    val icon = ch.channelImage

                    videoInfo.channelNo = order.toInt()
                    videoInfo.name = title + ""
                    videoInfo.path = path + ""
                    videoInfo.description = desc + ""
                    videoInfo.categoryID = cat.toInt()
                    videoInfo.icon = icon

                    videos += videoInfo
                }
            }
            if (videos.isNotEmpty()) {
                loadFragments(ArrayList(videos))
                playerFragment.play(videos[0], false)
                statusFragment.updateStatus(this, videos[0])
                channelListFragment.videoList = videos
            }
        }
    }
}