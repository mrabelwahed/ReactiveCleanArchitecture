package com.gify.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.gify.AppConst.keys.GIF_IMAGE
import com.gify.BaseApp
import com.gify.R
import com.gify.ui.GifAdapterViewTypes.GIF_MAIN_ITEM
import com.gify.ui.GifAdapterViewTypes.LOADING_ITEM
import com.gify.ui.decoration.SpacesItemDecoration
import com.gify.ui.model.GifModel
import com.gify.ui.viewmodel.GifListViewModel
import com.gify.ui.viewmodel.ViewModelFactory
import com.gify.ui.viewstate.ServerDataState
import com.gify.util.EspressoIdlingResource
import kotlinx.android.synthetic.main.content_empty.*
import kotlinx.android.synthetic.main.content_error.*
import kotlinx.android.synthetic.main.fragment_gif_list.*
import javax.inject.Inject


class GifListFragment : BaseFragment(), OnClickListener {
    private val gifDetailsFragment = GifyDetailsFragment()
    private lateinit var gifListViewModel: GifListViewModel
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var loading = false
    private lateinit var gridLayoutManager: GridLayoutManager
    private val VISIBLE_THRESHOLD = 1
    private var offset = 0L
    @Inject
    lateinit var gifyListAdapter: GifyListAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    var lastQuery: String = "init"
    var resetData: Boolean = false

    private lateinit var searchView: SearchView
    private var stopLoadingMore: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.applicationContext as BaseApp).appComponent
            .newGifLisComponent().inject(this)
        gifListViewModel =
            ViewModelProvider(this, viewModelFactory)[GifListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        configureToolbar(inflater, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_search) {
            addQueryListener()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureToolbar(menuInflater: MenuInflater, menu: Menu?) {
        menuInflater.inflate(R.menu.meanu_search, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        searchView = (searchItem?.actionView as SearchView)
        searchView?.queryHint = getString(R.string.search)
    }

    private fun addQueryListener() {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // check if the user search using another query
                // so we need to reset the recycler view and adapter
                resetData = lastQuery != query
                if (resetData) {
                    //reset view
                    resetView()
                    //reset offset
                    offset =0
                    //make new search
                    query?.let {
                       searchGIF(it, offset )
                        lastQuery = it
                    }

                }
                searchView.hideKeyboard()

                return true

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }


        })
    }

    fun resetView() {

         gifyListAdapter.gifList.clear()
         gifyListAdapter.notifyDataSetChanged()
        // gifyListAdapter.addGifs(list)
    }

    private fun setupView() {
        gridLayoutManager = GridLayoutManager(context, 2)
        gifyListAdapter.setClickListener(this)
        gifList.apply {
            layoutManager = gridLayoutManager
            gridLayoutManager.setSpanSizeLookup(object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (gifyListAdapter.getItemViewType(position)) {
                        GIF_MAIN_ITEM -> 1
                        LOADING_ITEM -> 2//number of columns of the grid
                        else -> -0
                    }
                }
            })
            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelOffset(R.dimen.spacing_small)))
            adapter = gifyListAdapter
        }


    }

    private fun setData(response: ArrayList<GifModel>) {
        gifyListAdapter.addGifs(response)
    }


    override fun onClick(position: Int, view: View) {

        showGifView(gifyListAdapter.gifList[position])
    }

    private fun observeGifList() {
        gifListViewModel.liveGifData.observe(this, Observer {
            when (it) {
                is ServerDataState.Success<*> -> {
                    loading = false
                    stopLoadingMore = (it.item as ArrayList<GifModel>).size <20
                    emptyView.visibility = View.GONE
                    errorView.visibility = View.GONE
                    gifList.visibility = View.VISIBLE
                   // gifyListAdapter.removeLoadingData()
                    setData(it.item as ArrayList<GifModel>)
                    EspressoIdlingResource.decrement()
                }
                is ServerDataState.Error -> {
                    loading = false
                    errorView.visibility = View.VISIBLE
                    gifList.visibility = View.GONE
                    emptyView.visibility = View.GONE
                    //gifyListAdapter.removeLoadingData()
                    setError(it.message)
                    EspressoIdlingResource.decrement()
                }

                is ServerDataState.Loading -> {
                    loading = true
                    emptyView.visibility = View.GONE
                    errorView.visibility = View.GONE
                    //gifyListAdapter.addLoadingData()
                    EspressoIdlingResource.increment()
                }
            }

        })
    }

    private fun setError(message: String?) {
        Log.e("ERROR", message)
    }


    override fun getLayoutById(): Int {
        return R.layout.fragment_gif_list
    }

    private fun initUI() {
        setupView()
        setupLoadMoreListener()
        observeGifList()
    }

    private fun setupLoadMoreListener() {
        gifList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = gridLayoutManager.itemCount
                lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition()
                if (!loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    offset += 20
                    if (!stopLoadingMore)
                       gifListViewModel.nextPage(offset)
                    loading = true
                }
            }
        })

    }

    private fun showGifView(gif: GifModel) {
        val bundle = Bundle()
        bundle.putParcelable(GIF_IMAGE, gif)
        gifDetailsFragment.arguments = bundle

        (activity as BaseActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.container, gifDetailsFragment)
            .addToBackStack(null)
            .commit()

    }


    fun searchGIF(query: String, offset: Long ) {
        gifListViewModel.search(query, offset)
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}
