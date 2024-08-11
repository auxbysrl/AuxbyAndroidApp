package com.fivedevs.auxby.screens.dashboard.offers

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.FiltersEnum
import com.fivedevs.auxby.domain.models.enums.OfferStateEnum
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val offersRepository: OffersRepository,
    private val preferencesService: PreferencesService,
    private val categoryRepository: CategoryRepository,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi,
    dataApi,
    rxSchedulers,
    userRepository,
    offersRepository,
    preferencesService,
    compositeDisposable
) {

    val categorySelected: MutableLiveData<CategoryModel> = MutableLiveData()
    val categoriesResponse: MutableLiveData<List<CategoryModel>> = MutableLiveData()
    val promotedOffersResponse: MutableLiveData<List<OfferModel>> = MutableLiveData()

    var seeAllCategoriesEvent = SingleLiveEvent<Any>()
    val onCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()
    val openOfferDetails: SingleLiveEvent<Long> = SingleLiveEvent()

    private val shimmerDelay = Handler(Looper.getMainLooper())

    fun onViewCreated() {
        getCategoriesFromDB()
        getPromotedOffers()
        getOffersFromDB()
        onCategorySelectedListener()
    }

    private fun getCategoriesFromDB() {
        categoryRepository.getCategories()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categoriesResponse.value = it
            }, {
                offersResponse.value = mutableListOf()
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getOffersFromDB() {
        Flowable.just(Constants.EMPTY)
            .doOnNext { showShimmerAnimation.value = true }
            .observeOn(rxSchedulers.background())
            .flatMap { offersRepository.getActiveOffers() }
            .observeOn(rxSchedulers.androidUI())
            .doOnNext { offers ->
                offersResponse.value = offers
            }
            .subscribe({
                if (showShimmerAnimation.value == true) {
                    shimmerDelay.postDelayed({
                        showShimmerAnimation.value = false
                    }, 1000)
                }
            }, {
                offersResponse.value = mutableListOf()
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getPromotedOffers() {
        offersRepository.getPromotedOffers()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                promotedOffersResponse.value = it.filter { offer ->
                    offer.status?.equals(OfferStateEnum.FINISHED.getStatusName()) != true
                            || offer.status?.equals(OfferStateEnum.INTERRUPTED.getStatusName()) != true
                }
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun loadMoreOffers() {
        getDashboardApiOffers(PaginationConstants.paginationFilters.toMutableMap().apply {
            put(FiltersEnum.PAGE_KEY.type, currentPage.toString())
        })
    }

    private fun onCategorySelectedListener() {
        onCategorySelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categorySelected.value = it
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    fun getFilteredCategory(category: CategoryModel) {
        offersRepository.getOfferByCategoryId(category.id)
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                openOfferDetails.value = it.id
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun handleDoOnError(it: Throwable) {
        showShimmerAnimation.value = false
        Timber.e(it)
    }

    fun onSeeAllCategoriesClicked() {
        seeAllCategoriesEvent.call()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        shimmerDelay.removeCallbacksAndMessages(null)
    }
}
