package com.fivedevs.auxby.domain.utils.pagination

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaginationRecyclerView(
    context: Context, attrs: AttributeSet
) : RecyclerView(context, attrs) {

    var isLastPage = false
    var isLoading = false

    private var loadMoreItems: () -> Unit = {}
    private var layoutManager: LinearLayoutManager? = null

    fun initPagination(
        layoutManager: LinearLayoutManager,
        loadMoreItems: () -> Unit
    ) {
        this.loadMoreItems = loadMoreItems
        this.layoutManager = layoutManager
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        layoutManager?.checkForPaginationLoadingVisibility(
            isLoading,
            isLastPage,
            loadMoreItems
        )
    }
}