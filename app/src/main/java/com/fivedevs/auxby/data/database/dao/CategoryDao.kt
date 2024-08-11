package com.fivedevs.auxby.data.database.dao

import androidx.room.*
import com.fivedevs.auxby.data.database.entities.Category
import com.fivedevs.auxby.data.database.entities.CategoryDetails
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<Category>)

    @Query("SELECT * FROM category")
    fun getCategories(): Flowable<List<CategoryModel>>

    @Query("SELECT * FROM category WHERE id=:id")
    fun getCategoryById(id: Int): Single<CategoryModel>

    // Category Details
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoriesDetails(categoryDetails: List<CategoryDetails>)

    @Query("SELECT * FROM category_details")
    fun getCategoriesDetails(): Single<List<CategoryDetailsModel>>

    @Query("SELECT * FROM category_details")
    fun getCategoriesDetailsList(): List<CategoryDetailsModel>

    @Query("SELECT * FROM category_details WHERE id=:id ")
    fun getCategoryDetailsById(id: Int): Single<CategoryDetailsModel>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM category_details")
    fun getAppCategories(): Flowable<List<CategoryModel>>

    @Query("SELECT * FROM category_details WHERE id=:id")
    fun getAppCategoryById(id: Int): Single<CategoryModel>

    @Query("DELETE FROM category")
    fun clearCategories()

    @Query("DELETE FROM category_details")
    fun clearCategoriesDetails()
}