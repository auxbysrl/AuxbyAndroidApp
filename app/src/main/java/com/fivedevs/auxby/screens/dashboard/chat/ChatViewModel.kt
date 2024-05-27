package com.fivedevs.auxby.screens.dashboard.chat

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.ChatApi
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.ChatRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.OfferUtils.getFullUsername
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val chatApi: ChatApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val offersRepository: OffersRepository,
    private val chatRepository: ChatRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi, dataApi, rxSchedulers, userRepository,
    offersRepository, preferencesService, compositeDisposable
) {

    var user: User = User()
    val buyerChatRooms: MutableLiveData<List<ChatRoom>> = MutableLiveData()
    val sellerChatRooms: MutableLiveData<List<ChatRoom>> = MutableLiveData()

    fun onViewCreated(localUser: User) {
        user = localUser
        getChatRooms()
    }

    fun getChatRooms() {
        if (isUserLoggedIn.value == true) {
            chatApi.getChatRooms()
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext { roomsFromBe -> insertChatRoomsInDb(roomsFromBe) }
                .flatMap { chatRepository.getUserChatRooms().toObservable() }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({ rooms ->
                    buyerChatRooms.value = rooms.filter { it.host.equals(getFullUsername(user), true) }
                    sellerChatRooms.value =
                        rooms.filter { !it.host.equals(getFullUsername(user), true) }
                }, {
                    buyerChatRooms.value = listOf()
                    sellerChatRooms.value = listOf()
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    private fun insertChatRoomsInDb(roomsFromBe: List<ChatRoom>) {
        if (roomsFromBe.isEmpty()) return
        try {
            val existingRooms: List<ChatRoom> = chatRepository.getUserChatRooms().blockingFirst()
            val mergedList = mergeChatRooms(roomsFromBe, existingRooms)
            chatRepository.insertChatRooms(mergedList)
        } catch (e: Exception) {
            chatRepository.insertChatRooms(roomsFromBe)
        }
    }

    private fun mergeChatRooms(roomsFromBe: List<ChatRoom>, localRooms: List<ChatRoom>): List<ChatRoom> {
        val mergedList = mutableListOf<ChatRoom>()
        mergedList.addAll(roomsFromBe)

        for (room in localRooms) {
            val existingRoom = mergedList.find { it.roomId == room.roomId }
            if (existingRoom == null) {
                mergedList.add(room)
            } else {
                existingRoom.lastSeenMessageTime = room.lastSeenMessageTime
            }
        }
        return mergedList
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}