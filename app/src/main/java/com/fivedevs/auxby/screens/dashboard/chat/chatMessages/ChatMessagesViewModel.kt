package com.fivedevs.auxby.screens.dashboard.chat.chatMessages

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.ChatApi
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.ChatRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.ChatMessage
import com.fivedevs.auxby.domain.models.ChatMessageRequest
import com.fivedevs.auxby.domain.models.ChatRoomIdRequest
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Constants.TODAY
import com.fivedevs.auxby.domain.utils.Constants.YESTERDAY
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity.Companion.CHAT_DATE_DIVIDER
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatMessagesViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val chatApi: ChatApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val offersRepository: OffersRepository,
    private val preferencesService: PreferencesService,
    private val categoryRepository: CategoryRepository,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi, dataApi, rxSchedulers, userRepository,
    offersRepository, preferencesService, compositeDisposable
) {

    var chatRoom: ChatRoom = ChatRoom()
    val messageSentEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val sendMessageErrorEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val populateUserDetailEvent: SingleLiveEvent<ChatRoom> = SingleLiveEvent()

    val message = MutableLiveData<String>()
    val user = MutableLiveData<User>().apply { value = User() }
    val chatMessages: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    val shouldShowSendMsgIcon: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        userRepository.getCurrentUser()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                user.value = it
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    fun getRoomMessagesAtInterval() {
        Observable.interval(0, 15, TimeUnit.SECONDS)
            .flatMap { chatApi.getRoomMessages(chatRoom.roomId) }
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .doOnNext { chatRepository.updateChatRoomSeenStatus(chatRoom.roomId, it.last().messageTime) }
            .flatMap(::groupMessagesByDays)
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ messages ->
                chatMessages.value = messages
                shouldShowLoader.value = false
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getRoomMessagesOnce() {
        chatApi.getRoomMessages(chatRoom.roomId)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .flatMap(::groupMessagesByDays)
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ messages ->
                if (messages.isNotEmpty()) {
                    chatMessages.value = messages
                }
                shouldShowLoader.value = false
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    fun sendChatMessage() {
        shouldShowLoader.value = true
        message.value?.let { message ->
            chatApi.sendChatMessage(chatRoom.roomId, ChatMessageRequest(message))
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext {
                    if (it.isNotEmpty()) {
                        chatRepository.updateChatRoomSeenStatus(chatRoom.roomId, it.first().messageTime)
                    }
                }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({
                    messageSentEvent.call()
                    getRoomMessagesOnce()
                }, {
                    sendMessageErrorEvent.call()
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    fun createChatRoom(offerId: Long) {
        shouldShowLoader.value = true
        chatApi.createChatRoom(ChatRoomIdRequest(offerId))
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                chatRoom = it
                populateUserDetailEvent.value = it
                getRoomMessagesOnce()
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun groupMessagesByDays(messages: List<ChatMessage>): Observable<List<ChatMessage>> {
        if (messages.isEmpty()) {
            return Observable.just(emptyList())
        } else {
            val groupedMessages = messages.sortedBy { it.messageTime }
                .groupBy { message -> message.messageTime.substring(0, 10) }
                .flatMap { (day, messages) ->
                    val chatMessagesForDay = if (DateUtils().isToday(day)) {
                        listOf(
                            ChatMessage(
                                messageText = CHAT_DATE_DIVIDER,
                                messageTime = TODAY
                            )
                        ) + messages
                    } else if (DateUtils().isYesterday(day)) {
                        listOf(
                            ChatMessage(
                                messageText = CHAT_DATE_DIVIDER,
                                messageTime = YESTERDAY
                            )
                        ) + messages
                    } else {
                        listOf(
                            ChatMessage(
                                CHAT_DATE_DIVIDER,
                                messages[messages.size - 1].messageTime
                            )
                        ) + messages
                    }
                    chatMessagesForDay
                }
            return Observable.just(groupedMessages)
        }
    }

    fun updateChatRoomSeen() {
        Observable.just(Constants.EMPTY)
            .subscribeOn(rxSchedulers.background())
            .doOnNext {
                if (it.isNotEmpty()) {
                    chatRepository.updateChatRoomSeenStatus(chatRoom.roomId, chatRoom.lastMessageTime) }
                }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun handleDoOnError(it: Throwable) {
        shouldShowLoader.value = false
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}