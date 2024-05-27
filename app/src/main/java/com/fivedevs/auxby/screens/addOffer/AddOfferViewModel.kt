package com.fivedevs.auxby.screens.addOffer

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.application.App
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.models.OfferRequestModel
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.OfferUtils.getMultipartOfferPhotos
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.MultipartBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddOfferViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val appContext: App,
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

    val user: MutableLiveData<User> = MutableLiveData()
    val selectedPosPhoto: MutableLiveData<OfferPhoto> = MutableLiveData()
    val selectedCategory: MutableLiveData<CategoryModel> = MutableLiveData()
    val selectedSubcategory: MutableLiveData<CategoryDetailsModel> = MutableLiveData()
    val appCategoriesResponse: MutableLiveData<List<CategoryModel>> = MutableLiveData()
    var isLightStatusBar: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    val shouldShowToolbar: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = true }

    val returnToDashboard = SingleLiveEvent<Boolean>()
    val errorUploadingImagesEvent = SingleLiveEvent<Any>()
    val shouldShowWholeDescription = SingleLiveEvent<Any>()
    val automaticallyPopulateDetails = SingleLiveEvent<Any>()
    val onPreviewFragment: SingleLiveEvent<Any> = SingleLiveEvent()
    val onOfferUpdateEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val onPromoteOfferFragment: SingleLiveEvent<Any> = SingleLiveEvent()
    val multipartImages: SingleLiveEvent<Array<MultipartBody.Part>> = SingleLiveEvent()
    val categoryDetailsResponse: SingleLiveEvent<CategoryDetailsModel> = SingleLiveEvent()

    val onOfferPhotoSelected: PublishSubject<OfferPhoto> = PublishSubject.create()
    val onCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()
    val onSubCategorySelected: PublishSubject<CategoryModel> = PublishSubject.create()

    var offerRequestModel: OfferRequestModel = OfferRequestModel()
    var selectedLanguage = preferencesService.getSelectedLanguageApp()

    var isEditMode = false
    var editOfferId = 0L

    init {
        shouldShowLoader.value = true
        getCurrentUserFromDB()
        onCategorySelectedListener()
        onOfferPhotoSelectedListener()
        onSubcategorySelectedListener()
    }

    fun onCreate() {
        getAppCategories()
    }

    private fun getCurrentUserFromDB() {
        userRepository.getCurrentUser()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                user.value = it
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun onCategorySelectedListener() {
        onCategorySelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ category ->
                offerRequestModel.categoryId = category.id
                selectedCategory.value = category
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
                    selectedSubcategory.value =
                        categoryDetails.subcategories.first { it.id == category.id }
                }
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun onOfferPhotoSelectedListener() {
        onOfferPhotoSelected
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                selectedPosPhoto.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getAppCategories() {
        categoryRepository.getAppCategories()
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                appCategoriesResponse.value = it
                shouldShowLoader.value = false
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getCategoryDetails() {
        selectedCategory.value?.let {
            categoryRepository.getCategoryDetailsById(it.id)
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    categoryDetailsResponse.value = it
                }, {
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    fun getCategoryDetailsForEdit(categoryId: Int) {
        categoryRepository.getCategoryDetailsById(categoryId)
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categoryDetailsResponse.value = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun publishNewOffer(photos: Array<MultipartBody.Part>?) {
        var offerId = 0L
        shouldShowLoader.value = true
        dataApi.addOffer(offerRequestModel)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .flatMap {
                offerId = it.id
                photos?.let { images ->
                    if (images.isEmpty()) return@flatMap Observable.just(Constants.EMPTY)
                    dataApi.uploadOfferPhotos(it.id.toString(), images)
                        .doOnError { error ->
                            errorUploadingImagesEvent.call()
                        }
                } ?: kotlin.run { Observable.just(Constants.EMPTY) }
            }
            .flatMap { dataApi.getOfferById(offerId) }
            .observeOn(rxSchedulers.background())
            .doOnNext {
                offersRepository.insertOffer(it)
                getMyOffers()
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                returnToDashboard.value = true
                //onPromoteOfferFragment.call()
                shouldShowLoader.value = false
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun toggleFullDescription() {
        shouldShowWholeDescription.call()
    }

    private fun handleDoOnError(it: Throwable) {
        shouldShowLoader.value = false
        Timber.e(it)
    }

    fun updateOffer(multipartOfferPhotos: Array<MultipartBody.Part>?) {
        if (shouldShowLoader.value == false) {
            shouldShowLoader.value = true
        }
        dataApi.updateOfferById(editOfferId, offerRequestModel)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .flatMap {
                multipartOfferPhotos?.let { photos ->
                    if (photos.isEmpty()) return@flatMap Observable.just(Constants.EMPTY)
                    dataApi.uploadOfferPhotos(editOfferId.toString(), photos)
                } ?: kotlin.run { Observable.just(Constants.EMPTY) }
            }
            .flatMap { dataApi.getOfferById(editOfferId) }
            .observeOn(rxSchedulers.background())
            .doOnNext { offersRepository.updateOffer(it) }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                onOfferUpdateEvent.call()
                shouldShowLoader.value = false
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getMultipartImages() {
        var imageFiles: Array<MultipartBody.Part>? = arrayOf()
        Observable.just(Constants.EMPTY)
            .subscribeOn(rxSchedulers.background())
            .doOnNext {
                imageFiles = getMultipartOfferPhotos(
                    offerRequestModel.photos.filter { it.url.isNotEmpty() },
                    appContext
                )
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                imageFiles?.let { multipartImages.value = it }
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}