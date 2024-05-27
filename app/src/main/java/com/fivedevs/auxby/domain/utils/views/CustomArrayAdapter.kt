package com.fivedevs.auxby.domain.utils.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class CustomArrayAdapter(context: Context, textViewResourceId: Int, objects: ArrayList<String>)
    : ArrayAdapter<String>(context, textViewResourceId, objects) {

    //Can skip first n items (skip 1 as default)
    var hideFirstItemsCount = 1

    override fun getCount(): Int {
        return super.getCount() - hideFirstItemsCount
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getDropDownView(position + hideFirstItemsCount, convertView, parent)
    }
}