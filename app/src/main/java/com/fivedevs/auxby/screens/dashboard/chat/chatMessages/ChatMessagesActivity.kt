package com.fivedevs.auxby.screens.dashboard.chat.chatMessages

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.databinding.ActivityChatMessagesBinding
import com.fivedevs.auxby.domain.models.ChatMessage
import com.fivedevs.auxby.domain.utils.Constants.OFFER_ID
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.extensions.loadImage
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.domain.utils.views.LoaderDialog
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatPagerAdapter
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.adapters.ChatMessagesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatMessagesActivity : BaseActivity() {

    private lateinit var binding: ActivityChatMessagesBinding
    private val viewModel by viewModels<ChatMessagesViewModel>()
    private val loaderDialog: LoaderDialog by lazy { LoaderDialog(this) }

    private var chatMessagesAdapter: ChatMessagesAdapter? = null
    private var isInitialScrollToBottom = false
    private var offerId = 0L
    private var tabPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        getChatRoomDetails()
        initObservers()
        initListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_messages)
        binding.apply {
            viewModel = this@ChatMessagesActivity.viewModel
            lifecycleOwner = this@ChatMessagesActivity
        }
    }

    private fun initListeners() {
        binding.rvChat.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) binding.rvChat.scrollBy(0, oldBottom - bottom)
        }

        binding.etChatMessage.doOnTextChanged { text, _, _, _ ->
            viewModel.shouldShowSendMsgIcon.value = text.toString().isNotEmpty()
        }

        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivSendMessage.setOnClickListenerWithDelay {
            viewModel.sendChatMessage()
        }

        binding.clChatHeader.setOnClickListenerWithDelay {
            // ToDo -> open offer
        }
    }

    @Suppress("DEPRECATION")
    private fun getChatRoomDetails() {
        viewModel.shouldShowLoader.value = true
        // Retrieve chat room details
        val chatRoom: ChatRoom? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(CHAT_ROOM_DETAILS, ChatRoom::class.java)
        } else {
            intent.getParcelableExtra(CHAT_ROOM_DETAILS)
        }
        offerId = intent.getLongExtra(OFFER_ID, 0L)
        tabPosition = intent.getIntExtra(CHAT_ROOM_TYPE, 0)
        // Populate chat messages list or create new chat room
        if (offerId == 0L) {
            chatRoom?.let {
                viewModel.chatRoom = it
                viewModel.getRoomMessagesAtInterval()
                viewModel.updateChatRoomSeen()
                populateRoomDetails(it)
            }
        } else {
            viewModel.createChatRoom(offerId)
        }
    }

    private fun initObservers() {
        viewModel.chatMessages.observe(this) { messages ->
            updateMessagesList(messages)
        }

        viewModel.messageSentEvent.observe(this) {
            binding.etChatMessage.text?.clear()
        }

        viewModel.shouldShowLoader.observe(this) {
            if (it) loaderDialog.showDialog()
            else loaderDialog.dismissDialog()
        }

        viewModel.populateUserDetailEvent.observe(this) {
            populateRoomDetails(it)
        }

        viewModel.user.observe(this) {
            initMessagesAdapter()
        }

        viewModel.sendMessageErrorEvent.observe(this) {
            AlerterUtils.showErrorAlert(this, resources.getString(R.string.error_sending_message))
        }
    }

    private fun initMessagesAdapter() {
        chatMessagesAdapter = ChatMessagesAdapter(this, arrayListOf(), viewModel.user.value!!)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatMessagesActivity)
            adapter = chatMessagesAdapter
        }
    }

    private fun populateRoomDetails(chatRoom: ChatRoom) {
        with(binding) {
            tvRoomName.text = chatRoom.roomName
            tvHostName.text = getChatHostName(chatRoom)
            tvActiveTime.text = getString(
                R.string.active_time_ago,
                DateUtils().getFormattedDateForUserActive(chatRoom.lastMessageTime)
            )
            ivUserAvatar.loadImage(chatRoom.chatImage, R.drawable.ic_placeholder, true)
        }
    }

    private fun getChatHostName(chatRoom: ChatRoom): String {
        return if (tabPosition == ChatPagerAdapter.BUY_TAB_POSITION) chatRoom.guest else chatRoom.host
    }

    private fun updateMessagesList(messages: List<ChatMessage>) {
        chatMessagesAdapter?.updateChatMessagesList(messages)
        if (binding.etChatMessage.isFocused || !isInitialScrollToBottom) {
            binding.rvChat.smoothScrollToPosition(messages.size - 1)
            isInitialScrollToBottom = true
        }
    }

    companion object {
        const val CHAT_ROOM_TYPE = "CHAT_ROOM_TYPE"
        const val CHAT_ROOM_DETAILS = "CHAT_ROOM_DETAILS"
        const val CHAT_DATE_DIVIDER = "CHAT_DATE_DIVIDER"
    }
}