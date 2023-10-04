package com.bittelasia.libvlc.presenter.control

import android.graphics.Color
import android.view.*
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bittelasia.libvlc.databinding.ChannelListViewItemRowBinding
import com.bittelasia.libvlc.model.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChannelAdapter(private val callback: (Int) -> Unit) :
    ListAdapter<VideoInfo, ChannelAdapter.ViewHolder>(
        ChannelComparator()
    ) {
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChannelListViewItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)
        if (current != null) {
            holder.bind(current)
        }
    }

    inner class ViewHolder(
        private val binding: ChannelListViewItemRowBinding,
        private val callback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: VideoInfo) {
            CoroutineScope(Dispatchers.Main).launch {
                launch {
                    binding.apply {
                        ivIcon.load(data.icon)
                        tvChannel.text = HtmlCompat.fromHtml(
                            "${data.channelNo}  ${data.name}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        root.setOnClickListener {
                            callback.invoke(adapterPosition)
                            it.requestFocus()
                        }
                        root.setOnHoverListener { view, motionEvent ->
                            when (motionEvent!!.actionMasked) {
                                MotionEvent.ACTION_HOVER_ENTER -> {
                                    view.isSelected = true
                                    view.requestFocus()
                                }

                                MotionEvent.ACTION_HOVER_EXIT -> {
                                    view.isSelected = false
                                }
                            }
                            false
                        }
                        root.setOnTouchListener { _, motionEvent ->
                            when (motionEvent!!.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_UP -> {
                                    root.performClick()
                                }
                            }
                            false
                        }
                        root.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
                            if (b) {
                                view.setBackgroundColor(Color.parseColor("#FA3E9301"))
                            } else {
                                view.setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                        root.setOnKeyListener { _, _, _ ->
                            false
                        }
                        if (adapterPosition == selectedPosition)
                            root.requestFocus()
                    }
                }
            }
        }
    }

    class ChannelComparator : DiffUtil.ItemCallback<VideoInfo>() {
        override fun areItemsTheSame(
            oldItem: VideoInfo,
            newItem: VideoInfo
        ) = oldItem.channelNo == newItem.channelNo

        override fun areContentsTheSame(
            oldItem: VideoInfo,
            newItem: VideoInfo
        ) = oldItem.channelNo == newItem.channelNo
    }
}