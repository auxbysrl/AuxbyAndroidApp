package com.fivedevs.auxby.screens.dashboard.offers.categories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.application.App
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.utils.Constants.DELAY_SEARCH
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    private val appContext: App,
    private val rxSchedulers: RxSchedulers,
    private val offersRepository: OffersRepository,
    private val categoryRepository: CategoryRepository,
    private val compositeDisposable: CompositeDisposable,
) : ViewModel() {

    val searchQuery: MutableLiveData<String> = MutableLiveData()
    val categories: MutableLiveData<List<CategoryModel>> = MutableLiveData()
    val categorySelected: MutableLiveData<CategoryModel> = MutableLiveData()
    val filteredCategories: MutableLiveData<List<CategoryModel>> = MutableLiveData()
    val showNoSearchResultMessage: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    var onSearchViewAction: PublishSubject<String> = PublishSubject.create()
    val onCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()

    val openOfferDetails: SingleLiveEvent<Long> =
        SingleLiveEvent()

    fun onCreate() {
        onCategorySelectedListener()
        getAllCategories()
        actionSearchByKeyword()
    }

    private fun onCategorySelectedListener() {
        onCategorySelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categorySelected.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getAllCategories() {
        categoryRepository.getCategories()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categories.value = it
            }, {
                categories.value = listOf()
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun actionSearchByKeyword() {
        onSearchViewAction
            .debounce(DELAY_SEARCH, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.computation())
            .map { text -> text.lowercase().trim() }
            .distinctUntilChanged()
            .observeOn(rxSchedulers.androidUI())
            .switchMap { s -> Observable.just(s) }
            .subscribe { query ->
                returnFilteredCategories(query)
            }
            .disposeBy(compositeDisposable)
    }

    private fun returnFilteredCategories(query: String) {
        searchQuery.value = query
        if (query.length >= 2) {
            filteredCategories.value = categories.value
                ?.filter { it.label.getName(appContext).lowercase().contains(query) }?.toList()
        } else {
            filteredCategories.value = categories.value
        }
        showNoSearchResultMessage.value = filteredCategories.value?.isEmpty()
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
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}