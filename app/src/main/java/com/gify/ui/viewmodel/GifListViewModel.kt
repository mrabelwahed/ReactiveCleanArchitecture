package com.gify.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gify.data.exceptions.Failure
import com.gify.domain.interactor.GetGifListUseCase
import com.gify.ui.dto.QueryDTO
import com.gify.ui.mapper.GifModelMapper
import com.gify.ui.viewstate.ServerDataState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject


class GifListViewModel @Inject constructor(private val getGifListUseCase: GetGifListUseCase) :
    BaseViewModel() {
    val paginator = PublishProcessor.create<String>()
    private var newOffset = 0L
    private var queryStr = ""
     val viewState = MutableLiveData<ServerDataState>()
    val liveGifData: LiveData<ServerDataState>
        get() = viewState

    init {
        initDisposable()
    }

    fun initDisposable() {
        val disposable = paginator.skip(1)
            .doOnNext { viewState.value = ServerDataState.Loading }
            .concatMap { getGifListUseCase.execute(QueryDTO(queryStr, newOffset)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { res -> viewState.value = ServerDataState.Success(GifModelMapper.transform(res))},
                { error -> viewState.value = setFailure(error) }
            )

        compositeDisposable.add(disposable)
    }


    private fun setFailure(throwable: Throwable) :ServerDataState{
        return when(throwable){
           is UnknownHostException -> ServerDataState.Error(Failure.NetworkConnection)
           is HttpException -> ServerDataState.Error(Failure.ServerError)
            else -> ServerDataState.Error(Failure.UnExpectedError)
       }
    }


    fun loadNextPage(query: String, offset: Long) {
        this.newOffset = offset
        this.queryStr = query
        paginator.onNext(queryStr)
    }


    fun onQueryTextChange(newText: String?,offset: Long) {
        if (newText == null || newText.isEmpty()) {
        } else {
            loadNextPage(newText,offset)
        }
    }


}