package com.fivedevs.auxby.screens.filterOffers

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.AdvancedFiltersModel
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferRequestModel
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FilterOffersViewModel @Inject constructor(
    dataApi: DataApi,
    userApi: UserApi,
    userRepository: UserRepository,
    offersRepository: OffersRepository,
    preferencesService: PreferencesService,
    private val rxSchedulers: RxSchedulers,
    private val categoryRepository: CategoryRepository,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi, dataApi, rxSchedulers, userRepository,
    offersRepository, preferencesService, compositeDisposable
) {

    val categoryDetailsResponse: SingleLiveEvent<CategoryDetailsModel> =
        SingleLiveEvent()

    val selectedCategory: MutableLiveData<CategoryModel> = MutableLiveData()
    val selectedSubcategory: MutableLiveData<CategoryDetailsModel> = MutableLiveData()
    val appCategoriesResponse: MutableLiveData<List<CategoryModel>> = MutableLiveData()

    val onCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()
    val onSubCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()

    var advancedFilterOffers = AdvancedFiltersModel()
    var offerRequestModel: OfferRequestModel = OfferRequestModel()

    init {
        getAppCategories()
        onCategorySelectedListener()
        onSubcategorySelectedListener()
    }

    private fun onCategorySelectedListener() {
        onCategorySelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ category ->
                getCategoryDetails(category.id)
                offerRequestModel.categoryId = category.id
                selectedCategory.value = category
                advancedFilterOffers.categories = listOf(category.id)
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun onSubcategorySelectedListener() {
        onSubCategorySelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ category ->
                offerRequestModel.categoryId = category.id
                categoryDetailsResponse.value?.let { categoryDetails ->
                    selectedSubcategory.value = categoryDetails.subcategories.first { it.id == category.id }
                }
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getCategoryDetails(categoryId: Int?) {
        categoryId?.let { id ->
            categoryRepository.getCategoryDetailsBySubcategoryId(id)
                .subscribeOn(rxSchedulers.background())
                .flatMap {
                    if (it.id == -1) {
                        categoryRepository.getCategoryDetailsById(categoryId)
                    } else {
                        Single.just(it)
                    }
                }
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    categoryDetailsResponse.value = it
                }, {
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    private fun getAppCategories() {
        categoryRepository.getAppCategories()
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                appCategoriesResponse.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }
}