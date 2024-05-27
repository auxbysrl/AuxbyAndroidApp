package com.fivedevs.auxby.domain.utils.pagination
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants.API_PAGE_SIZE
import timber.log.Timber

fun RecyclerView.LayoutManager.checkForPaginationLoadingVisibility(
    isLoading: Boolean,
    isLastPage: Boolean,
    loadMoreItems: () -> Unit
) {
    var firstVisibleItemPosition = when (this) {
        is GridLayoutManager -> {
            findFirstVisibleItemPosition()
        }
        is LinearLayoutManager -> {
            findFirstVisibleItemPosition()
        }
        else -> -1
    }

    val visibleItemCount = childCount
    val totalItemCount = itemCount

    Timber.i("Pagination isLoading $isLoading isLastPage $isLastPage")

    if (!isLoading && !isLastPage) {
        if (firstVisibleItemPosition == -1)
            firstVisibleItemPosition = 0

        Timber.i(
            "Pagination visibleItemCount ${
                visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= API_PAGE_SIZE - 2
            }"
        )

        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
            && firstVisibleItemPosition >= 0
            && totalItemCount >= API_PAGE_SIZE - 2
        ) {
            loadMoreItems.invoke()
        }

    }
}