package com.fivedevs.auxby.screens.dashboard.offers.categories.categoryOffers

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.enums.FiltersEnum
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants.paginationFilters
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CategoryOffersViewModel @Inject constructor(
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

    val categoryModel = MutableLiveData<CategoryModel>()
    private var categoryId: Int = 0

    fun receivedCategoryId(categoryId: Int) {
        this.categoryId = categoryId
        getLocalCategory(categoryId)
        getLocalOffersByCategory()
        getOffers()
    }

    private fun getLocalCategory(categoryId: Int) {
        categoryRepository
            .getCategoryById(categoryId)
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categoryModel.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getLocalOffersByCategory() {
        offersRepository
            .getOffersByCategoryId(categoryId)
            .subscribeOn(rxSchedulers.background())
            .doOnNext {
                if (shouldLoadFirstPageAgain(it.size)) {
                    loadFirstPageAgain()
                }
            }
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                offersResponse.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun shouldLoadFirstPageAgain(numLocalOffers: Int): Boolean {
        // return if the number of local offers doesn't match the number of offers from API
        return numLocalOffers < 10 && numLocalOffers < categoryModel.value?.noOffers.orZero() && currentPage == 0
    }

    private fun getOffers() {
        dataApi.getOffers(paginationFilters)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ offersResponse ->
                offersRepository.insertOffers(offersResponse.content)
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun loadMoreOffersByCategoryId() {
        currentPage += 1
        getApiOffers(paginationFilters.toMutableMap().apply {
            put(FiltersEnum.PAGE_KEY.type, currentPage.toString())
            put(FiltersEnum.CATEGORIES_KEY.type, categoryId.toString())
        })
    }

    fun loadFirstPageAgain() {
        getApiOffers(paginationFilters.toMutableMap().apply {
            put(FiltersEnum.PAGE_KEY.type, "0")
            put(FiltersEnum.CATEGORIES_KEY.type, categoryId.toString())
        })
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}