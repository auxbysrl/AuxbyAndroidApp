package com.fivedevs.auxby.data.database.repositories

import androidx.room.Transaction
import com.fivedevs.auxby.data.database.AppDatabase
import com.fivedevs.auxby.data.database.entities.Category
import com.fivedevs.auxby.data.database.entities.CategoryDetails
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class CategoryRepository @Inject constructor(private val appDatabase: AppDatabase) {

    @Transaction
    fun insertCategories(categories: List<Category>) {
        appDatabase.categoryDao().clearCategories()
        appDatabase.categoryDao().insertCategories(categories)
    }

    fun getCategories(): Flowable<List<CategoryModel>> {
        return appDatabase.categoryDao().getCategories()
    }

    fun getCategoryById(id: Int): Single<CategoryModel> {
        return appDatabase.categoryDao().getCategoryById(id)
    }

    // Category Details
    @Transaction
    fun insertCategoriesDetails(categoryDetails: List<CategoryDetails>) {
        appDatabase.categoryDao().clearCategoriesDetails()
        appDatabase.categoryDao().insertCategoriesDetails(categoryDetails)
    }

    fun getCategoriesDetails(): Single<List<CategoryDetailsModel>> {
        return appDatabase.categoryDao().getCategoriesDetails()
    }

    fun getCategoryDetailsById(id: Int): Single<CategoryDetailsModel> {
        return appDatabase.categoryDao().getCategoryDetailsById(id)
    }

    fun getCategoryDetailsBySubcategoryId(id: Int): Single<CategoryDetailsModel> {
        val category = appDatabase.categoryDao().getCategoriesDetailsList()
            .firstOrNull { cat ->
                cat.subcategories.any { it.id == id }
            }
        return Single.just(category ?: CategoryDetailsModel())
    }

    fun getAppCategories(): Flowable<List<CategoryModel>> {
        return appDatabase.categoryDao().getAppCategories()
    }

    fun getAppCategoryById(id: Int): Single<CategoryModel> {
        return appDatabase.categoryDao().getAppCategoryById(id)
    }

    fun getAppSubcategoryById(id: Int): Single<CategoryModel> {
        val categoryDetailsList = appDatabase.categoryDao().getCategoriesDetailsList()
        val subcategory = categoryDetailsList
            .flatMap { category ->
                category.subcategories.filter { it.id == id }
            }
            .first()

        return Single.just(
            CategoryModel(subcategory.id, subcategory.icon, subcategory.label)
        )
    }

}