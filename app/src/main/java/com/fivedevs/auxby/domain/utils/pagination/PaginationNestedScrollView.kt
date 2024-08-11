package com.fivedevs.auxby.domain.utils.pagination

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaginationNestedScrollView(
    context: Context, attrs: AttributeSet
) : NestedScrollView(context, attrs) {

    private var loadMoreItems: () -> Unit = {}
    private var layoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = false

    fun initPagination(
        layoutManager: LinearLayoutManager,
        loadMoreItems: () -> Unit
    ) {
        this.loadMoreItems = loadMoreItems
        this.layoutManager = layoutManager
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        if (recyclerView!!.bottom - (this.height + this.scrollY) <= 0) {
            layoutManager?.checkForPaginationLoadingVisibility(
                isLoading,
                isLastPage,
                loadMoreItems
            )
        }
    }
}
