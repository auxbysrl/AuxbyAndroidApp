package com.fivedevs.auxby.screens.dashboard.offers

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.FiltersEnum
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
    userApi, dataApi, rxSchedulers, userRepository, offersRepository, preferencesService, compositeDisposable
) {

    val categorySelected: MutableLiveData<CategoryModel> = MutableLiveData()
    val categoriesResponse: MutableLiveData<List<CategoryModel>> = MutableLiveData()
    val promotedOffersResponse: MutableLiveData<List<OfferModel>> = MutableLiveData()

    var seeAllCategoriesEvent = SingleLiveEvent<Any>()
    val onCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()
    val openOfferDetails: SingleLiveEvent<Long> = SingleLiveEvent()

    fun onViewCreated() {
        getCategoriesAndOffersFromDB()
        onCategorySelectedListener()
    }

    private fun getCategoriesAndOffersFromDB() {
        Observable.just(Constants.EMPTY)
            .doOnNext { showShimmerAnimation.value = true }
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.background())
            .flatMap { offersRepository.getActiveOffers().toObservable() }
            .observeOn(rxSchedulers.androidUI())
            .doOnNext { offers ->
                offersResponse.value = offers
                getPromotedOffers()
            }
            .observeOn(rxSchedulers.background())
            .flatMap { categoryRepository.getCategories().toObservable() }
            .observeOn(rxSchedulers.androidUI())
            .doOnNext { categories -> categoriesResponse.value = categories }
            .doOnError { handleDoOnError(it) }
            .subscribe({
                showShimmerAnimation.value = false
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
                promotedOffersResponse.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun loadMoreOffers() {
        currentPage += 1
        getApiOffers(PaginationConstants.paginationFilters.toMutableMap().apply {
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
    }
}
