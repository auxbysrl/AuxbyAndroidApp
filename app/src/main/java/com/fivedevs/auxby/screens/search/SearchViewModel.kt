package com.fivedevs.auxby.screens.search

import androidx.lifecycle.MutableLiveData
import androidx.room.rxjava3.EmptyResultSetException
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.AdvancedFiltersModel
import com.fivedevs.auxby.domain.models.CategoryResult
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.SearchSuggestion
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Constants.DELAY_SEARCH
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    userApi: UserApi,
    userRepository: UserRepository,
    offersRepository: OffersRepository,
    preferencesService: PreferencesService,
    private val dataApi: DataApi,
    private val rxSchedulers: RxSchedulers,
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

    var apiErrorMessage = MutableLiveData<String>()
    var showLoader = MutableLiveData<Boolean>()
    val searchOffers = MutableLiveData<List<OfferModel>>().apply { value = listOf() }
    var searchSuggestionResponse = MutableLiveData<List<SearchSuggestion>>()
    var showSearchNoResultMessage = MutableLiveData<Boolean>().apply { value = false }
    var showSearchDefaultTextMessage = MutableLiveData<Boolean>().apply { value = true }

    var onSearchViewAction: PublishSubject<String> = PublishSubject.create()
    var onSuggestionClickedPublishSubject: PublishSubject<SearchSuggestion> = PublishSubject.create()

    var localFilters = AdvancedFiltersModel()
    var selectedSuggestion: SearchSuggestion? = null
    var selectedCategory: String = ""

    init {
        onSuggestionClicked()
        actionSearchByKeyword()
    }

    private fun actionSearchByKeyword() {
        onSearchViewAction
            .debounce(DELAY_SEARCH, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.computation())
            .map { text -> text.trim() }
            .distinctUntilChanged()
            .observeOn(rxSchedulers.androidUI())
            .switchMap { s -> Observable.just(s) }
            .subscribe { query ->
                handleSearchResult(query)
            }
            .disposeBy(compositeDisposable)
    }

    private fun handleSearchResult(query: String) {
        if (query.length >= 3) {
            getSearchSuggestions(query)
            showSearchDefaultTextMessage.value = false
        } else {
            searchSuggestionResponse.value = listOf()
            searchOffers.value = listOf()
            showSearchDefaultTextMessage.value = true
            showSearchNoResultMessage.value = false
        }
    }

    private fun onSuggestionClicked() {
        onSuggestionClickedPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .subscribe {
                selectedSuggestion = it
                callSearchOffers(it.searchText, listOf(it.category.id))
            }
            .disposeBy(compositeDisposable)
    }

    private fun getSearchSuggestions(searchText: String) {
        localFilters.clear()
        dataApi.getSearchSuggestions(searchText)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .flatMap { response ->
                getSearchSuggestions(response, searchText)
            }
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ searchSuggestions ->
                searchOffers.value = listOf()
                searchSuggestionResponse.value = searchSuggestions
                showSearchNoResultMessage.value = searchSuggestions.isEmpty()
            }, {
                handleSearchSuggestionError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getSearchSuggestions(
        response: CategoryResult,
        searchText: String
    ): Observable<List<SearchSuggestion>> {
        val searchSuggestions = response.categoryResult.entries.map {
            try {
                SearchSuggestion(
                    searchText,
                    it.value,
                    categoryRepository.getAppCategoryById(it.key.toIntOrNull() ?: 0).blockingGet()
                )
            } catch (e: EmptyResultSetException) {
                SearchSuggestion(
                    searchText,
                    it.value,
                    categoryRepository.getAppSubcategoryById(it.key.toIntOrNull() ?: 0).blockingGet()
                )
            }
        }
        return Observable.just(searchSuggestions)
    }

    fun callSearchOffers(searchText: String, categoryId: List<Int>) {
        searchSuggestionResponse.value = listOf()
        showLoader.value = true
        localFilters.apply {
            this.categories = categoryId
            this.title = searchText
        }

        getSearchOffers()
    }

    fun callSearchOffers(filters: AdvancedFiltersModel) {
        showLoader.value = true
        searchSuggestionResponse.value = listOf()
        localFilters = filters
        getSearchOffers()
    }

    private fun getSearchOffers() {
        showSearchNoResultMessage.value = false
        showSearchDefaultTextMessage.value = false

        dataApi.getSearchOffers(localFilters)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                searchSuggestionResponse.value = listOf()
                searchOffers.value = it
                if (it.isEmpty()) {
                    showSearchNoResultMessage.value = true
                }
                showLoader.value = false
            }, {
                showLoader.value = false
                searchOffers.value = listOf()
                apiErrorMessage.value = Constants.DEFAULT_ERROR_MSG
            })
            .disposeBy(compositeDisposable)
    }

    private fun handleSearchSuggestionError(it: Throwable) {
        searchSuggestionResponse.value = listOf()
        showSearchNoResultMessage.value = true
        apiErrorMessage.value = Constants.DEFAULT_ERROR_MSG
    }

    fun areFiltersApplied() = localFilters != AdvancedFiltersModel()
}