package com.danigutiadan.mo2o_test.features.beer.search.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danigutiadan.mo2o_test.R
import com.danigutiadan.mo2o_test.api.ListSuccess
import com.danigutiadan.mo2o_test.di.injectViewModel
import com.danigutiadan.mo2o_test.features.beer.search.data.BeerResponse
import com.danigutiadan.mo2o_test.ui.BaseFragment
import com.danigutiadan.mo2o_test.data.Result
import com.danigutiadan.mo2o_test.databinding.FragmentSearchBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search) {
    private lateinit var viewModel: SearchViewModel
    var handler: Handler = Handler(Looper.getMainLooper())
    var workRunnable: Runnable? = null
    private val beersAdapter = BeersAdapter()
    var beerListener = object: BeerInterface {
        override fun onClickBeer(beer: BeerResponse) {
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToBeerDetailFragment(
                Gson().toJson(beer)))
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = injectViewModel(viewModelFactory)
        val binding = FragmentSearchBinding.bind(view)
        subscribeUi(binding)
    }

    private fun subscribeUi(binding: FragmentSearchBinding) {
        binding.apply {
            beersAdapter.setListener(beerListener)
            rvSearchBeer.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = beersAdapter
            }
            etSearchBeer.doAfterTextChanged {
                if(it.toString() == viewModel.beerName){
                    return@doAfterTextChanged
                }
                if(!it.isNullOrEmpty() && it.isNotBlank()) {
                    workRunnable?.let { it1 -> handler.removeCallbacks(it1) }
                    workRunnable = Runnable { searchBeers(it.toString()) }
                    handler.postDelayed(workRunnable!!, 1000)
                }

            }
        }
    }

    private fun searchBeers(text: String) {
        viewModel.beerName = text
        viewModel.beers.observe(viewLifecycleOwner) {
            when {
                it.status == Result.Status.SUCCESS && it.data != null && it.data is ListSuccess -> {
                    hideLoading()
                    val beerModelList = mutableListOf<BeerResponse>()
                    it.data.listElements.forEach { element ->
                        if (element is BeerResponse) beerModelList.add(element)
                    }
                    setNewElements(beerModelList)
                }
                it.status == Result.Status.SUCCESS && it.data == null -> {
                    hideLoading()
                }
                it.status == Result.Status.LOADING -> {
                    logd("LOADING")
                    showLoading()
                }
                it.status == Result.Status.ERROR -> {
                    hideLoading()
                }
            }
            viewModel.resetValues()
        }
    }

    private fun setNewElements(beers: MutableList<BeerResponse>) {
        beersAdapter.setBeers(beers)
    }
}