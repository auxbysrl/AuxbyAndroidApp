package com.fivedevs.auxby.screens.addOffer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentSelectCategoryBinding
import com.fivedevs.auxby.screens.addOffer.AddOfferViewModel
import com.fivedevs.auxby.screens.dashboard.offers.categories.adapters.AllCategoriesAdapter

class SelectCategoryFragment : Fragment() {

    private lateinit var binding: FragmentSelectCategoryBinding
    private val parentViewModel: AddOfferViewModel by activityViewModels()
    private var adapterCategories: AllCategoriesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_category, container, false)
        binding.viewModel = parentViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCategoriesRv()
        initObservers()
    }

    private fun initObservers(){
        parentViewModel.appCategoriesResponse.observe(viewLifecycleOwner){
            adapterCategories?.updateCategoriesList(it)
        }
    }

    private fun initCategoriesRv() {
        adapterCategories = AllCategoriesAdapter(requireContext(), mutableListOf(), parentViewModel.onCategorySelected)
        binding.rvAllCategories.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = adapterCategories
        }
    }
}