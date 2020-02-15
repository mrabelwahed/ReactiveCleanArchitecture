package com.gify.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gify.domain.interactor.GetGifListUseCase
import com.gify.ui.dto.QueryDTO
import com.gify.ui.mapper.GifModelMapper
import com.gify.ui.viewstate.ServerDataState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class GifListViewModel @Inject constructor(private val getGifListUseCase: GetGifListUseCase) :
    BaseViewModel() {
    val paginator = PublishProcessor.create<Long>()
    private var offset = 0L
    private val viewState = MutableLiveData<ServerDataState>()
    val liveGifData: LiveData<ServerDataState>
        get() = viewState


    fun search(query:String , offset: Long ) {
        this.offset = offset
        val disposable = paginator
            .doOnNext { viewState.value = ServerDataState.Loading }
            .concatMap { getGifListUseCase.execute(QueryDTO(query , offset)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { res -> viewState.value = ServerDataState.Success(GifModelMapper.transform(res)) },
                { error -> viewState.value = ServerDataState.Error(error.message) }
            )

        compositeDisposable.add(disposable)
           paginator.onNext(offset)
    }


    fun nextPage(offset: Long) {
        this.offset = offset
        paginator.onNext(offset)
    }




}